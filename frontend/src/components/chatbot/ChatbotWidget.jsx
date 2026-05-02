import { useState, useRef, useEffect } from 'react';
import { chatbotApi } from '@/services/api';
import { Bot, Send, X, MessageCircle, Sparkles } from 'lucide-react';

const SUGGESTED = [
  "Show me this month's revenue",
  "Which product is selling the most?",
  "What is our net profit?",
  "Suggest strategies to improve sales",
];

export default function ChatbotWidget() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'bot', text: "Hi! I'm your AI Business Advisor. Ask me about revenue, products, expenses, or business strategies! 🚀" }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionId, setSessionId] = useState(null);
  const [suggestions, setSuggestions] = useState(SUGGESTED);
  const bottomRef = useRef(null);

  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const sendMessage = async (text) => {
    const msg = text || input.trim();
    if (!msg) return;
    setInput('');
    setMessages(m => [...m, { role: 'user', text: msg }]);
    setLoading(true);
    try {
      const res = await chatbotApi.query({ message: msg, sessionId });
      const data = res.data.data;
      setSessionId(data.sessionId);
      setMessages(m => [...m, { role: 'bot', text: data.message, intent: data.intent }]);
      if (data.suggestedPrompts?.length) setSuggestions(data.suggestedPrompts);
    } catch {
      setMessages(m => [...m, { role: 'bot', text: 'Sorry, I had trouble processing that. Please try again.' }]);
    } finally { setLoading(false); }
  };

  return (
    <>
      {/* Floating Button */}
      <button onClick={() => setOpen(!open)} style={{ position:'fixed',bottom:24,right:24,width:56,height:56,borderRadius:'50%',background:'linear-gradient(135deg,#6366f1,#22d3ee)',border:'none',cursor:'pointer',display:'flex',alignItems:'center',justifyContent:'center',boxShadow:'0 8px 32px rgba(99,102,241,0.4)',zIndex:1000,transition:'transform 0.2s' }}
        onMouseEnter={e=>e.target.style.transform='scale(1.1)'} onMouseLeave={e=>e.target.style.transform='scale(1)'}>
        {open ? <X size={24} color="white" /> : <MessageCircle size={24} color="white" />}
      </button>

      {/* Chat Panel */}
      {open && (
        <div style={{ position:'fixed',bottom:96,right:24,width:380,height:520,background:'var(--color-surface)',border:'1px solid var(--color-border)',borderRadius:'1rem',boxShadow:'0 24px 64px rgba(0,0,0,0.5)',zIndex:1000,display:'flex',flexDirection:'column',overflow:'hidden' }}>
          {/* Header */}
          <div style={{ padding:'1rem',background:'linear-gradient(135deg,#6366f1,#4f46e5)',display:'flex',alignItems:'center',gap:'0.75rem' }}>
            <div style={{ width:36,height:36,borderRadius:'50%',background:'rgba(255,255,255,0.2)',display:'flex',alignItems:'center',justifyContent:'center' }}><Bot size={20} color="white"/></div>
            <div><p style={{ fontWeight:700,color:'white',fontSize:'0.9rem' }}>AI Business Advisor</p><p style={{ fontSize:'0.7rem',color:'rgba(255,255,255,0.7)' }}>Powered by SBADSS Intelligence</p></div>
            <Sparkles size={16} color="rgba(255,255,255,0.7)" style={{ marginLeft:'auto' }}/>
          </div>

          {/* Messages */}
          <div style={{ flex:1,overflowY:'auto',padding:'1rem',display:'flex',flexDirection:'column',gap:'0.75rem' }}>
            {messages.map((m, i) => (
              <div key={i} style={{ display:'flex', justifyContent: m.role==='user'?'flex-end':'flex-start' }}>
                <div className={`chat-bubble chat-bubble-${m.role}`}>
                  {m.text}
                  {m.intent && m.intent !== 'UNKNOWN' && (
                    <div style={{ marginTop:'0.5rem',fontSize:'0.65rem',opacity:0.6 }}>Intent: {m.intent}</div>
                  )}
                </div>
              </div>
            ))}
            {loading && (
              <div style={{ display:'flex',justifyContent:'flex-start' }}>
                <div className="chat-bubble chat-bubble-bot" style={{ display:'flex',gap:'4px',alignItems:'center' }}>
                  {[0,1,2].map(i=><div key={i} style={{ width:6,height:6,borderRadius:'50%',background:'var(--color-primary)',animation:`bounce 0.6s ${i*0.2}s infinite alternate` }}/>)}
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Suggestions */}
          <div style={{ padding:'0 1rem 0.5rem',display:'flex',gap:'0.375rem',flexWrap:'wrap' }}>
            {suggestions.slice(0,3).map((s,i) => (
              <button key={i} onClick={() => sendMessage(s)} style={{ fontSize:'0.7rem',padding:'0.25rem 0.6rem',background:'var(--color-surface-2)',border:'1px solid var(--color-border)',borderRadius:'9999px',color:'var(--color-text-muted)',cursor:'pointer',whiteSpace:'nowrap' }}>
                {s}
              </button>
            ))}
          </div>

          {/* Input */}
          <div style={{ padding:'0.75rem 1rem',borderTop:'1px solid var(--color-border)',display:'flex',gap:'0.5rem' }}>
            <input className="input" style={{ flex:1 }} placeholder="Ask about your business..." value={input}
              onChange={e=>setInput(e.target.value)} onKeyDown={e=>e.key==='Enter'&&sendMessage()} disabled={loading} />
            <button className="btn btn-primary" style={{ padding:'0.5rem 0.75rem' }} onClick={() => sendMessage()} disabled={!input.trim()||loading}><Send size={16}/></button>
          </div>
        </div>
      )}
      <style>{`@keyframes bounce{from{transform:translateY(0)}to{transform:translateY(-6px)}}`}</style>
    </>
  );
}
