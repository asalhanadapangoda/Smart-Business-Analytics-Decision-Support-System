import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { salesApi, productsApi, customersApi } from '@/services/api';
import { ShoppingCart, Plus, Minus, Trash2, User, Package, Save, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/store/authStore';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n || 0);

export default function NewSale() {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [cart, setCart] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [phoneSearch, setPhoneSearch] = useState('');
  const [foundCustomer, setFoundCustomer] = useState(null);
  const [productSearch, setProductSearch] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [completedSaleId, setCompletedSaleId] = useState(null);

  // Fetch products and customers
  const { data: products = [] } = useQuery({
    queryKey: ['products'],
    queryFn: () => productsApi.getAll().then(r => r.data.data),
  });

  const { data: customers = [] } = useQuery({
    queryKey: ['customers'],
    queryFn: () => customersApi.getAll().then(r => r.data.data),
  });

  const createMutation = useMutation({
    mutationFn: (data) => salesApi.create(data),
    onSuccess: (res) => {
      toast.success('Sale recorded successfully');
      setCompletedSaleId(res.data.data.id);
      // Don't navigate away immediately so they can print invoice
    },
    onError: (err) => {
      const msg = err.response?.data?.message || 'Failed to create sale';
      toast.error(msg);
    }
  });

  const handleDownloadInvoice = async (id) => {
    try {
      const res = await salesApi.downloadInvoice(id || completedSaleId);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `invoice-${id || completedSaleId}.pdf`);
      document.body.appendChild(link);
      link.click();
      toast.success('Invoice downloaded');
    } catch (err) {
      toast.error('Failed to download invoice');
    }
  };

  const handlePhoneSearch = async (val) => {
    setPhoneSearch(val);
    if (val.length >= 10) {
      try {
        const res = await customersApi.findByPhone(val);
        if (res.data.data) {
          setFoundCustomer(res.data.data);
          setSelectedCustomer(res.data.data.id);
          toast.success(`Customer found: ${res.data.data.name}`);
        } else {
          setFoundCustomer(null);
        }
      } catch (err) {
        console.error(err);
      }
    } else {
      setFoundCustomer(null);
    }
  };

  const addToCart = (product) => {
    setCart(prev => {
      const existing = prev.find(item => item.id === product.id);
      if (existing) {
        return prev.map(item => 
          item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      return [...prev, { ...product, quantity: 1 }];
    });
    toast.success(`${product.name} added to cart`, { duration: 1000 });
  };

  const updateQuantity = (id, delta) => {
    setCart(prev => prev.map(item => {
      if (item.id === id) {
        const newQty = Math.max(1, item.quantity + delta);
        return { ...item, quantity: newQty };
      }
      return item;
    }));
  };

  const removeFromCart = (id) => {
    setCart(prev => prev.filter(item => item.id !== id));
  };

  const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const filteredProducts = products.filter(p => 
    p.name.toLowerCase().includes(productSearch.toLowerCase()) || 
    (p.sku && p.sku.toLowerCase().includes(productSearch.toLowerCase()))
  );

  const handleSubmit = () => {
    if (cart.length === 0) return toast.error('Cart is empty');
    
    const payload = {
      customerId: selectedCustomer ? parseInt(selectedCustomer) : null,
      branchId: user.branchId || 1,
      paymentMethod: paymentMethod,
      items: cart.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }))
    };

    createMutation.mutate(payload);
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <button className="btn btn-secondary" onClick={() => navigate('/sales')} style={{ padding: '0.5rem' }}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title">New Sale</h1>
            <p className="page-subtitle">Create a new transaction for {user?.branchName || 'your branch'}</p>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '1.5rem', alignItems: 'start' }}>
        {/* Product Selection */}
        <div className="card">
          <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Package size={20} color="var(--color-primary)" />
              <h3 style={{ fontWeight: 700 }}>Select Products</h3>
            </div>
            <div style={{ position: 'relative' }}>
              <input 
                className="input" 
                style={{ width: '250px', paddingLeft: '2.5rem' }} 
                placeholder="Search name or code..." 
                value={productSearch}
                onChange={(e) => setProductSearch(e.target.value)}
              />
              <div style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-muted)' }}>
                <Package size={16} />
              </div>
            </div>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Product Details</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredProducts.length === 0 ? (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '2rem', color: 'var(--color-text-muted)' }}>
                      No products match your search.
                    </td>
                  </tr>
                ) : filteredProducts.map(product => (
                  <tr key={product.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <div style={{ width: 32, height: 32, borderRadius: '0.5rem', background: 'var(--color-surface-2)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid var(--color-border)' }}>
                          <Package size={16} color="var(--color-text-muted)" />
                        </div>
                        <div>
                          <div style={{ fontWeight: 600 }}>{product.name}</div>
                          <div style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)' }}>Code: {product.sku || 'N/A'}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', background: 'var(--color-bg)', borderRadius: '1rem', color: 'var(--color-text-muted)' }}>
                        {product.categoryName}
                      </span>
                    </td>
                    <td style={{ fontWeight: 700, color: 'var(--color-primary)' }}>{fmt(product.price)}</td>
                    <td>
                      <span className={`badge ${product.stockQuantity > 20 ? 'badge-success' : product.stockQuantity > 5 ? 'badge-warning' : 'badge-danger'}`}>
                        {product.stockQuantity}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button 
                        className="btn btn-primary" 
                        style={{ padding: '0.4rem 0.6rem' }} 
                        onClick={() => addToCart(product)}
                      >
                        <Plus size={14} /> Add
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Cart & Customer */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Customer Selection */}
          <div className="card">
            <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <User size={18} color="var(--color-primary)" />
              <h3 style={{ fontWeight: 700 }}>Customer</h3>
            </div>
            
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ fontSize: '0.75rem', marginBottom: '0.25rem' }}>Search by Phone</label>
              <input 
                className="input" 
                placeholder="0712345678..." 
                value={phoneSearch}
                onChange={(e) => handlePhoneSearch(e.target.value)}
              />
            </div>

            {foundCustomer && (
              <div style={{ padding: '0.75rem', background: 'rgba(99,102,241,0.1)', borderRadius: '0.5rem', marginBottom: '1rem', border: '1px solid var(--color-primary)' }}>
                <p style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--color-primary)' }}>Matched Customer:</p>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontWeight: 600 }}>{foundCustomer.name}</span>
                  <span className="badge badge-success" style={{ fontSize: '0.7rem' }}>{foundCustomer.loyaltyPoints || 0} Points</span>
                </div>
              </div>
            )}

            <label style={{ fontSize: '0.75rem', marginBottom: '0.25rem' }}>Or Select Manually</label>
            <select 
              className="input" 
              value={selectedCustomer} 
              onChange={(e) => {
                setSelectedCustomer(e.target.value);
                const c = customers.find(curr => curr.id == e.target.value);
                if(c) {
                  setPhoneSearch(c.phoneNumber);
                  setFoundCustomer(c);
                } else {
                  setPhoneSearch('');
                  setFoundCustomer(null);
                }
              }}
            >
              <option value="">Walk-in Customer</option>
              {customers.map(c => (
                <option key={c.id} value={c.id}>{c.name} ({c.phoneNumber})</option>
              ))}
            </select>
          </div>

          {/* Cart */}
          <div className="card">
            <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <ShoppingCart size={18} color="var(--color-primary)" />
              <h3 style={{ fontWeight: 700 }}>Your Cart</h3>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem', minHeight: '100px' }}>
              {cart.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--color-text-muted)' }}>
                  <p>Cart is empty</p>
                </div>
              ) : (
                cart.map(item => (
                  <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '0.75rem', borderBottom: '1px solid var(--color-border)' }}>
                    <div style={{ flex: 1 }}>
                      <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>{item.name}</p>
                      <p style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>{fmt(item.price)} × {item.quantity}</p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <button className="btn btn-secondary" style={{ padding: '0.2rem' }} onClick={() => updateQuantity(item.id, -1)}><Minus size={14} /></button>
                      <span style={{ fontWeight: 600, minWidth: '20px', textAlign: 'center' }}>{item.quantity}</span>
                      <button className="btn btn-secondary" style={{ padding: '0.2rem' }} onClick={() => updateQuantity(item.id, 1)}><Plus size={14} /></button>
                      <button className="btn" style={{ padding: '0.2rem', color: 'var(--color-danger)' }} onClick={() => removeFromCart(item.id)}><Trash2 size={14} /></button>
                    </div>
                  </div>
                ))
              )}
            </div>

            <div style={{ paddingTop: '1rem', borderTop: '2px solid var(--color-border)', marginBottom: '1rem' }}>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.75rem', marginBottom: '0.25rem' }}>Payment Method</label>
                <select className="input" value={paymentMethod} onChange={e => setPaymentMethod(e.target.value)}>
                  <option value="CASH">Cash</option>
                  <option value="CARD">Card</option>
                  <option value="BANK_TRANSFER">Bank Transfer</option>
                  {foundCustomer && <option value="LOYALTY_POINTS">Loyalty Points ({foundCustomer.loyaltyPoints || 0})</option>}
                </select>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ color: 'var(--color-text-muted)' }}>Subtotal</span>
                <span>{fmt(total)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: '1.2rem' }}>
                <span>Total</span>
                <span style={{ color: 'var(--color-primary)' }}>{fmt(total)}</span>
              </div>
            </div>

            {completedSaleId ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <button 
                  className="btn btn-primary" 
                  style={{ width: '100%', padding: '0.75rem' }}
                  onClick={() => handleDownloadInvoice()}
                >
                  <Save size={18} /> Download Invoice
                </button>
                <button 
                  className="btn btn-secondary" 
                  style={{ width: '100%', padding: '0.75rem' }}
                  onClick={() => navigate('/sales')}
                >
                  Done
                </button>
              </div>
            ) : (
              <button 
                className="btn btn-primary" 
                style={{ width: '100%', padding: '0.75rem', fontSize: '1rem' }}
                disabled={cart.length === 0 || createMutation.isPending}
                onClick={handleSubmit}
              >
                <Save size={18} /> {createMutation.isPending ? 'Processing...' : 'Complete Sale'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
        </div>
      </div>
    </div>
  );
}
