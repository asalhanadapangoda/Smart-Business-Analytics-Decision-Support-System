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
  const [form, setForm] = useState({ name: '', description: '', price: '', stockQuantity: '', categoryId: '' });
  const [showForm, setShowForm] = useState(false);
  const [showCatForm, setShowCatForm] = useState(false);
  const [newCat, setNewCat] = useState({ name: '', description: '' });

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
      setForm({ name: '', description: '', price: '', stockQuantity: '', categoryId: '' });
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

  const handleSaveProduct = () => {
    if (!form.name || !form.price || !form.stockQuantity || !form.categoryId) {
      return toast.error('Please fill in all required fields');
    }

    const payload = {
      ...form,
      price: parseFloat(form.price),
      stockQuantity: parseInt(form.stockQuantity),
      categoryId: parseInt(form.categoryId),
      branchId: user?.branchId || 1 // Important fix for 422
    };

    createMutation.mutate(payload);
  };

  const products = data || [];
  const categories = cats || [];

  return (
    <div>
      <div className="page-header">
        <div><h1 className="page-title">Products</h1><p className="page-subtitle">{products.length} products in inventory</p></div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
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
            <div><label>Price ($)</label><input type="number" className="input" value={form.price} onChange={e => setForm({ ...form, price: e.target.value })} /></div>
            <div><label>Initial Stock</label><input type="number" className="input" value={form.stockQuantity} onChange={e => setForm({ ...form, stockQuantity: e.target.value })} /></div>
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

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(280px,1fr))', gap: '1.25rem' }}>
        {isLoading ? [1,2,3,4].map(i=><div key={i} className="skeleton" style={{ height:180 }}/>)
          : products.length === 0 ? (
            <div className="card" style={{ gridColumn:'1/-1',textAlign:'center',padding:'4rem',color:'var(--color-text-muted)' }}>
              <Package size={64} style={{ margin:'0 auto 1.5rem',opacity:0.2 }}/>
              <p style={{ fontSize: '1.1rem' }}>No products found. Add your first item to get started!</p>
            </div>
          ) : products.map(p => (
              <div key={p.id} className="card" style={{ padding:'1.5rem', transition: 'transform 0.2s', cursor: 'default' }} onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-4px)'} onMouseLeave={e => e.currentTarget.style.transform = 'none'}>
                <div style={{ display:'flex',justify8Content:'space-between',alignItems:'flex-start',marginBottom:'1rem' }}>
                  <div style={{ width:44,height:44,borderRadius:'0.75rem',background:'rgba(99,102,241,0.1)',display:'flex',alignItems:'center',justifyContent:'center' }}><Package size={22} color="var(--color-primary)"/></div>
                  <span className={`badge ${p.stockQuantity > 20 ? 'badge-success' : p.stockQuantity > 5 ? 'badge-warning' : 'badge-danger'}`}>
                    {p.stockQuantity} in stock
                  </span>
                </div>
                <h4 style={{ fontWeight:700,fontSize:'1.1rem',marginBottom:'0.25rem' }}>{p.name}</h4>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                  <span style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', background: 'var(--color-bg)', borderRadius: '1rem', color: 'var(--color-text-muted)' }}>{p.categoryName}</span>
                </div>
                <p style={{ fontWeight:800,fontSize:'1.25rem',color:'var(--color-primary)' }}>{fmt(p.price)}</p>
              </div>
            ))}
      </div>
    </div>
  );
}

