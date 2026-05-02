import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productsApi, categoriesApi } from '@/services/api';
import { Plus, Package, FolderPlus, X, Save } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/store/authStore';

const fmt = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n || 0);

export default function Products() {
  const { user } = useAuthStore();
  const qc = useQueryClient();
  const [form, setForm] = useState({ name: '', sku: '', description: '', price: '', stockQuantity: '', categoryId: '' });
  const [showForm, setShowForm] = useState(false);
  const [showCatForm, setShowCatForm] = useState(false);
  const [newCat, setNewCat] = useState({ name: '', description: '' });
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');

  const { data, isLoading } = useQuery({ 
    queryKey: ['products'], 
    queryFn: () => productsApi.getAll().then(r => r.data.data) 
  });
  
  const { data: cats } = useQuery({ 
    queryKey: ['categories'], 
    queryFn: () => categoriesApi.getAll().then(r => r.data.data) 
  });

  const createMutation = useMutation({
    mutationFn: (d) => productsApi.create(d),
    onSuccess: () => { 
      toast.success('Product created'); 
      qc.invalidateQueries(['products']); 
      setShowForm(false); 
      setForm({ name: '', sku: '', description: '', price: '', stockQuantity: '', categoryId: '' });
    },
    onError: (err) => {
      const msg = err.response?.data?.message || 'Check your inputs';
      toast.error('Validation failed: ' + msg);
    }
  });

  const catMutation = useMutation({
    mutationFn: (d) => categoriesApi.create(d),
    onSuccess: () => {
      toast.success('Category created');
      qc.invalidateQueries(['categories']);
      setShowCatForm(false);
      setNewCat({ name: '', description: '' });
    },
  });

  const importMutation = useMutation({
    mutationFn: (formData) => productsApi.import(formData),
    onSuccess: () => {
      toast.success('Products imported successfully');
      qc.invalidateQueries(['products']);
    },
    onError: (err) => toast.error('Import failed: ' + (err.response?.data?.message || 'Check CSV format'))
  });

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    formData.append('branchId', user?.branchId || 1);
    importMutation.mutate(formData);
  };

  const handleSaveProduct = () => {
    if (!form.name || !form.price || !form.stockQuantity || !form.categoryId) {
      return toast.error('Please fill in all required fields');
    }

    const payload = {
      ...form,
      price: parseFloat(form.price),
      stockQuantity: parseInt(form.stockQuantity),
      minThreshold: parseInt(form.minThreshold) || 5,
      categoryId: parseInt(form.categoryId),
      branchId: user?.branchId || 1
    };

    createMutation.mutate(payload);
  };

  const products = data || [];
  const categories = cats || [];

  const filteredProducts = products.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || p.categoryName === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const groupedProducts = filteredProducts.reduce((acc, p) => {
    const cat = p.categoryName || 'Uncategorized';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(p);
    return acc;
  }, {});

  return (
    <div>
      <div className="page-header">
        <div><h1 className="page-title">Inventory</h1><p className="page-subtitle">{products.length} products total</p></div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <div style={{ position: 'relative' }}>
            <input 
              className="input" 
              style={{ width: '240px', paddingLeft: '2.5rem' }} 
              placeholder="Search products..." 
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
            <div style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-muted)' }}>
              <Package size={16} />
            </div>
          </div>
          <select 
            className="input" 
            style={{ width: '160px' }}
            value={selectedCategory}
            onChange={e => setSelectedCategory(e.target.value)}
          >
            <option value="all">All Categories</option>
            {categories.map(c => <option key={c.id} value={c.name}>{c.name}</option>)}
          </select>
          <div style={{ width: '1px', height: '24px', background: 'var(--color-border)', margin: '0 0.5rem' }} />
          
          <label className="btn btn-secondary" style={{ cursor: 'pointer' }}>
            <FolderPlus size={16} /> Import CSV
            <input type="file" accept=".csv" style={{ display: 'none' }} onChange={handleFileUpload} />
          </label>
          
          <button className="btn btn-secondary" onClick={() => setShowCatForm(true)}><FolderPlus size={16} /> New Category</button>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}><Plus size={16} /> Add Product</button>
        </div>
      </div>

      {/* Category Creation Modal-ish form */}
      {showCatForm && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100, backdropFilter: 'blur(4px)' }}>
          <div className="card" style={{ width: '100%', maxWidth: '400px', margin: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
              <h3 style={{ fontWeight: 700 }}>New Category</h3>
              <button onClick={() => setShowCatForm(false)} style={{ background: 'none', border: 'none', color: 'var(--color-text-muted)', cursor: 'pointer' }}><X size={20}/></button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' }}>
              <div><label>Category Name</label><input className="input" value={newCat.name} onChange={e => setNewCat({...newCat, name: e.target.value})} placeholder="e.g. Beverages" /></div>
              <div><label>Description</label><textarea className="input" value={newCat.description} onChange={e => setNewCat({...newCat, description: e.target.value})} placeholder="Brief details..." rows={3} /></div>
            </div>
            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowCatForm(false)}>Cancel</button>
              <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => catMutation.mutate(newCat)} disabled={!newCat.name || catMutation.isPending}>
                {catMutation.isPending ? 'Saving...' : 'Create Category'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showForm && (
        <div className="card" style={{ marginBottom: '1.5rem', border: '1px solid var(--color-primary)' }}>
          <h3 style={{ fontWeight: 700, marginBottom: '1rem' }}>New Product</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(200px,1fr))', gap: '1.25rem', marginBottom: '1.5rem' }}>
            <div><label>Product Name</label><input className="input" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></div>
            <div><label>Product Code (SKU)</label><input className="input" value={form.sku} onChange={e => setForm({ ...form, sku: e.target.value })} placeholder="e.g. BEV-001" /></div>
            <div><label>Price ($)</label><input type="number" className="input" value={form.price} onChange={e => setForm({ ...form, price: e.target.value })} /></div>
            <div><label>Initial Stock</label><input type="number" className="input" value={form.stockQuantity} onChange={e => setForm({ ...form, stockQuantity: e.target.value })} /></div>
            <div><label>Low Stock Alert Threshold</label><input type="number" className="input" value={form.minThreshold} onChange={e => setForm({ ...form, minThreshold: e.target.value })} placeholder="Default is 5" /></div>
            <div>
              <label>Category</label>
              <select className="input" value={form.categoryId} onChange={e => setForm({ ...form, categoryId: e.target.value })}>
                <option value="">Select Category...</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
            <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSaveProduct} disabled={createMutation.isPending}>
              <Save size={16} /> {createMutation.isPending ? 'Saving...' : 'Save Product'}
            </button>
          </div>
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div className="table-wrapper">
          {isLoading ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>Loading inventory...</div>
          ) : products.length === 0 ? (
            <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>
              <Package size={64} style={{ margin: '0 auto 1.5rem', opacity: 0.2 }} />
              <p style={{ fontSize: '1.1rem' }}>No products found. Add your first item to get started!</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th style={{ width: '40%' }}>Product Details</th>
                  <th>Price</th>
                  <th>Stock Level</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {Object.keys(groupedProducts).length === 0 ? (
                   <tr>
                     <td colSpan="4" style={{ textAlign: 'center', padding: '2rem', color: 'var(--color-text-muted)' }}>
                       No products match your search.
                     </td>
                   </tr>
                ) : Object.entries(groupedProducts).map(([catName, items]) => (
                  <React.Fragment key={catName}>
                    <tr style={{ background: 'rgba(99,102,241,0.05)' }}>
                      <td colSpan="4" style={{ padding: '0.5rem 1rem', fontSize: '0.75rem', fontWeight: 700, color: 'var(--color-primary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                        {catName}
                      </td>
                    </tr>
                    {items.map(p => (
                      <tr key={p.id}>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                            <div style={{ width: 32, height: 32, borderRadius: '0.5rem', background: 'var(--color-surface-2)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid var(--color-border)' }}>
                              <Package size={16} color="var(--color-text-muted)" />
                            </div>
                            <div>
                              <div style={{ fontWeight: 600 }}>{p.name}</div>
                              <div style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)' }}>Code: {p.sku || 'N/A'} | SKU: PRD-{p.id} | Alert at: {p.minThreshold || 5}</div>
                            </div>
                          </div>
                        </td>
                        <td style={{ fontWeight: 700, color: 'var(--color-primary)' }}>{fmt(p.price)}</td>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <div style={{ width: '80px', height: '4px', background: 'var(--color-surface-2)', borderRadius: '2px', overflow: 'hidden' }}>
                              <div style={{ 
                                width: `${Math.min((p.stockQuantity / 100) * 100, 100)}%`, 
                                height: '100%', 
                                background: p.stockQuantity > 20 ? 'var(--color-success)' : p.stockQuantity > 5 ? 'var(--color-warning)' : 'var(--color-danger)' 
                              }} />
                            </div>
                            <span style={{ fontWeight: 600, fontSize: '0.75rem' }}>{p.stockQuantity}</span>
                          </div>
                        </td>
                        <td>
                          <span className={`badge ${p.stockQuantity > 20 ? 'badge-success' : p.stockQuantity > 5 ? 'badge-warning' : 'badge-danger'}`}>
                            {p.stockQuantity > 20 ? 'In Stock' : p.stockQuantity > 0 ? 'Low' : 'Empty'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

