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
    onSuccess: () => {
      toast.success('Sale recorded successfully');
      navigate('/sales');
    },
    onError: () => toast.error('Failed to create sale. Please try again.'),
  });

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

  const handleSubmit = () => {
    if (cart.length === 0) return toast.error('Cart is empty');
    
    const payload = {
      customerId: selectedCustomer ? parseInt(selectedCustomer) : null,
      branchId: user.branchId || 1, // Fallback to 1 if not set
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
          <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Package size={20} color="var(--color-primary)" />
            <h3 style={{ fontWeight: 700 }}>Select Products</h3>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '1rem' }}>
            {products.map(product => (
              <div 
                key={product.id} 
                className="card" 
                style={{ 
                  cursor: 'pointer', 
                  transition: 'all 0.2s', 
                  border: '1px solid var(--color-border)',
                  padding: '1rem'
                }}
                onClick={() => addToCart(product)}
                onMouseEnter={(e) => e.currentTarget.style.borderColor = 'var(--color-primary)'}
                onMouseLeave={(e) => e.currentTarget.style.borderColor = 'var(--color-border)'}
              >
                <p style={{ fontWeight: 600, marginBottom: '0.25rem' }}>{product.name}</p>
                <p style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', marginBottom: '0.75rem' }}>{product.categoryName}</p>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontWeight: 700, color: 'var(--color-primary)' }}>{fmt(product.price)}</span>
                  <Plus size={16} color="var(--color-text-muted)" />
                </div>
              </div>
            ))}
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
            <select 
              className="input" 
              value={selectedCustomer} 
              onChange={(e) => setSelectedCustomer(e.target.value)}
            >
              <option value="">Walk-in Customer</option>
              {customers.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
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

            <div style={{ paddingTop: '1rem', borderTop: '2px solid var(--color-border)', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ color: 'var(--color-text-muted)' }}>Subtotal</span>
                <span>{fmt(total)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: '1.2rem' }}>
                <span>Total</span>
                <span style={{ color: 'var(--color-primary)' }}>{fmt(total)}</span>
              </div>
            </div>

            <button 
              className="btn btn-primary" 
              style={{ width: '100%', padding: '0.75rem', fontSize: '1rem' }}
              disabled={cart.length === 0 || createMutation.isPending}
              onClick={handleSubmit}
            >
              <Save size={18} /> {createMutation.isPending ? 'Processing...' : 'Complete Sale'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
