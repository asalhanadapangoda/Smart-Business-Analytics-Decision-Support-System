import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { reportsApi } from '@/services/api';
import { FileText, Download, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { format } from 'date-fns';

export default function Reports() {
  const qc = useQueryClient();
  const [form, setForm] = useState({ reportType: 'SALES', format: 'PDF', startDate: '', endDate: '' });
  const [showForm, setShowForm] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ['reports'],
    queryFn: () => reportsApi.getHistory().then(r => r.data.data),
  });

  const generateMutation = useMutation({
    mutationFn: (data) => reportsApi.generate(data),
    onSuccess: () => { toast.success('Report generated!'); qc.invalidateQueries(['reports']); setShowForm(false); },
  });

  const handleDownload = async (id, fileName) => {
    try {
      const res = await reportsApi.download(id);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a');
      a.href = url; a.download = fileName || 'report'; a.click();
      window.URL.revokeObjectURL(url);
    } catch { toast.error('Download failed'); }
  };

  const reports = data || [];

  return (
    <div>
      <div className="page-header">
        <div><h1 className="page-title">Reports</h1><p className="page-subtitle">Generate and download business reports</p></div>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}><Plus size={16} /> Generate Report</button>
      </div>
      {showForm && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <h3 style={{ fontWeight: 700, marginBottom: '1rem' }}>New Report</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(170px,1fr))', gap: '1rem', marginBottom: '1rem' }}>
            <div><label>Type</label><select className="input" value={form.reportType} onChange={e=>setForm({...form,reportType:e.target.value})}>{['SALES','EXPENSES','CUSTOMERS','PRODUCTS'].map(t=><option key={t}>{t}</option>)}</select></div>
            <div><label>Format</label><select className="input" value={form.format} onChange={e=>setForm({...form,format:e.target.value})}><option>PDF</option><option>EXCEL</option></select></div>
            <div><label>Start Date</label><input type="date" className="input" value={form.startDate} onChange={e=>setForm({...form,startDate:e.target.value})} /></div>
            <div><label>End Date</label><input type="date" className="input" value={form.endDate} onChange={e=>setForm({...form,endDate:e.target.value})} /></div>
          </div>
          <button className="btn btn-primary" onClick={() => generateMutation.mutate(form)} disabled={generateMutation.isPending}>{generateMutation.isPending ? 'Generating...' : 'Generate'}</button>
        </div>
      )}
      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrapper">
          {isLoading ? <div style={{ padding:'2rem',textAlign:'center',color:'var(--color-text-muted)' }}>Loading...</div>
            : reports.length === 0 ? <div style={{ padding:'3rem',textAlign:'center',color:'var(--color-text-muted)' }}><FileText size={48} style={{ margin:'0 auto 1rem',opacity:0.3 }} /><p>No reports yet.</p></div>
            : <table>
                <thead><tr><th>Type</th><th>Format</th><th>Branch</th><th>Period</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>{reports.map(r=>(
                  <tr key={r.id}>
                    <td style={{ fontWeight:600 }}>{r.reportType}</td>
                    <td><span className="badge badge-info">{r.format}</span></td>
                    <td>{r.branchName}</td>
                    <td style={{ fontSize:'0.8rem',color:'var(--color-text-muted)' }}>{r.dateRangeStart} → {r.dateRangeEnd}</td>
                    <td><span className={`badge badge-${r.status==='COMPLETED'?'success':r.status==='FAILED'?'danger':'warning'}`}>{r.status}</span></td>
                    <td>{r.status==='COMPLETED'&&<button className="btn btn-secondary" style={{ padding:'0.3rem 0.6rem',fontSize:'0.75rem' }} onClick={()=>handleDownload(r.id,r.fileName)}><Download size={14}/> Download</button>}</td>
                  </tr>
                ))}</tbody>
              </table>}
        </div>
      </div>
    </div>
  );
}
