import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronDown, FileText, GitCommit, Notebook, MessageSquare, HelpCircle, Sparkles, FolderArchive } from 'lucide-react';

const ScrollReveal = ({ children, delay = 0 }) => {
  const [isVisible, setIsVisible] = useState(false);
  const domRef = useRef();

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.unobserve(domRef.current);
        }
      },
      { threshold: 0.1 }
    );

    const currentRef = domRef.current;
    if (currentRef) {
      observer.observe(currentRef);
    }

    return () => {
      if (currentRef) {
        observer.unobserve(currentRef);
      }
    };
  }, []);

  return (
    <div
      ref={domRef}
      className={`transition-all duration-700 transform ${
        isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'
      }`}
      style={{ transitionDelay: `${delay}ms` }}
    >
      {children}
    </div>
  );
};

const LandingPage = () => {
  const navigate = useNavigate();
  const [heroLoaded, setHeroLoaded] = useState(false);

  useEffect(() => {
    // Slight delay to trigger clean animation on page mount
    const timer = setTimeout(() => {
      setHeroLoaded(true);
    }, 50);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans antialiased overflow-x-hidden selection:bg-indigo-500 selection:text-white">
      {/* Dynamic Keyframes for Signature Graph Animation */}
      <style>{`
        @keyframes float-node-1 {
          0%, 100% { transform: translate(0px, 0px); }
          50% { transform: translate(-8px, 6px); }
        }
        @keyframes float-node-2 {
          0%, 100% { transform: translate(0px, 0px); }
          50% { transform: translate(7px, -8px); }
        }
        @keyframes float-node-3 {
          0%, 100% { transform: translate(0px, 0px); }
          50% { transform: translate(-5px, -7px); }
        }
        @keyframes pulse-gentle {
          0%, 100% { transform: scale(1); filter: drop-shadow(0 4px 6px rgba(99, 102, 241, 0.15)); }
          50% { transform: scale(1.03); filter: drop-shadow(0 10px 15px rgba(99, 102, 241, 0.3)); }
        }
        .animate-float-1 {
          animation: float-node-1 6s ease-in-out infinite;
        }
        .animate-float-2 {
          animation: float-node-2 8s ease-in-out infinite;
        }
        .animate-float-3 {
          animation: float-node-3 7s ease-in-out infinite;
        }
        .animate-pulse-central {
          animation: pulse-gentle 4s ease-in-out infinite;
        }
        @keyframes dash {
          to {
            stroke-dashoffset: -20;
          }
        }
        .animate-svg-dash {
          stroke-dasharray: 6 6;
          animation: dash 20s linear infinite;
        }
        .tech-logo-scroll {
          display: flex;
          width: fit-content;
          animation: scroll 30s linear infinite;
        }
        @keyframes scroll {
          from { transform: translateX(0); }
          to { transform: translateX(-50%); }
        }
      `}</style>

      {/* TopNavBar */}
      <nav className="bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-50 transition-all select-none">
        <div className="flex justify-between items-center w-full px-6 md:px-8 max-w-7xl mx-auto h-16">
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}>
            <img src="/logo_normal.svg?v=3" alt="DevBrain Logo" className="h-14 w-auto object-contain" />
          </div>
          <div className="hidden md:flex items-center gap-8">
            <a className="text-slate-500 hover:text-indigo-600 transition-colors text-sm font-semibold" href="#features">Features</a>
            <a className="text-slate-500 hover:text-indigo-600 transition-colors text-sm font-semibold" href="#how-it-works">How it Works</a>
            <a className="text-slate-500 hover:text-indigo-600 transition-colors text-sm font-semibold" href="#faq">FAQ</a>
            <a className="text-slate-500 hover:text-indigo-600 transition-colors text-sm font-semibold" target="_blank" rel="noreferrer" href="https://github.com/BhaveshDahake/DevBrain_Persistent-Architectural-Memory-for-AI-Coding-Assistants">GitHub</a>
          </div>
          <div className="flex items-center gap-4">
            <button 
              onClick={() => navigate('/login')} 
              className="text-slate-600 hover:text-indigo-600 text-sm font-bold px-4 py-2 transition-colors cursor-pointer"
            >
              Sign In
            </button>
            <button 
              onClick={() => navigate('/signup')} 
              className="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2 rounded-xl text-sm font-bold shadow-sm active:scale-95 transition-all cursor-pointer"
            >
              Get Started
            </button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-16 pb-20 md:pt-32 md:pb-24 px-6 select-none">
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          <div className="lg:col-span-6 space-y-6">
            <div 
              className={`inline-flex items-center gap-1.5 bg-indigo-50 text-indigo-700 px-3.5 py-1 rounded-full border border-indigo-100/50 transition-all duration-700 transform ${
                heroLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
              }`}
              style={{ transitionDelay: '100ms' }}
            >
              <span className="text-xs font-bold uppercase tracking-wider">Introducing Architectural Memory</span>
            </div>
            
            <h1 
              className={`text-4xl md:text-6xl font-black text-slate-900 tracking-tight leading-tight transition-all duration-700 transform ${
                heroLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
              }`}
              style={{ transitionDelay: '200ms' }}
            >
              End the AI <span className="text-indigo-600">Hangover</span>
            </h1>

            <p 
              className={`text-lg text-slate-500 max-w-[540px] leading-relaxed transition-all duration-700 transform ${
                heroLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
              }`}
              style={{ transitionDelay: '300ms' }}
            >
              DevBrain converts your codebase into a persistent knowledge graph, giving AI assistants an architectural memory that never expires.
            </p>

            <div 
              className={`flex flex-col sm:flex-row gap-4 pt-4 transition-all duration-700 transform ${
                heroLoaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
              }`}
              style={{ transitionDelay: '400ms' }}
            >
              <button 
                onClick={() => navigate('/signup')} 
                className="bg-indigo-600 hover:bg-indigo-700 hover:scale-[1.02] text-white px-8 py-4 rounded-xl text-md font-bold hover:shadow-lg active:scale-98 transition-all cursor-pointer"
              >
                Get Started Free
              </button>
              <button 
                onClick={() => window.open('https://github.com/BhaveshDahake/DevBrain_Persistent-Architectural-Memory-for-AI-Coding-Assistants', '_blank')}
                className="border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 hover:scale-[1.02] px-8 py-4 rounded-xl text-md font-bold transition-all flex items-center justify-center gap-2 cursor-pointer shadow-sm"
              >
                View on GitHub
              </button>
            </div>
          </div>

          <div className="lg:col-span-6 relative">
            <div 
              className={`relative w-full aspect-[4/3] rounded-2xl bg-slate-100/95 border border-slate-200 shadow-2xl overflow-hidden transition-all duration-1000 transform flex flex-col ${
                heroLoaded ? 'opacity-100 translate-x-0' : 'opacity-0 translate-x-8'
              }`}
            >
              {/* Window Header */}
              <div className="bg-slate-200/60 px-4 py-3 flex items-center justify-between border-b border-slate-200/80 select-none">
                <div className="flex gap-2">
                  <span className="w-3 h-3 rounded-full bg-rose-500/80"></span>
                  <span className="w-3 h-3 rounded-full bg-amber-500/80"></span>
                  <span className="w-3 h-3 rounded-full bg-emerald-500/80"></span>
                </div>
                <div className="text-[11px] text-slate-600 font-mono tracking-wider font-bold">devbrain-workspace (active)</div>
                <div className="w-12"></div>
              </div>

              {/* Window Body */}
              <div className="flex-1 flex flex-col p-6 justify-between select-none relative overflow-hidden bg-slate-100/20">
                
                {/* SVG Connecting Flow Lines running background-vertically */}
                <svg className="absolute inset-0 w-full h-full pointer-events-none" viewBox="0 0 400 300" preserveAspectRatio="none">
                  {/* Dynamic Vertical Streams */}
                  <line x1="80" y1="40" x2="80" y2="220" stroke="#818cf8" strokeWidth="1" strokeDasharray="3 3" opacity="0.25" className="animate-svg-dash" />
                  <line x1="200" y1="40" x2="200" y2="220" stroke="#06b6d4" strokeWidth="1" strokeDasharray="3 3" opacity="0.25" className="animate-svg-dash" />
                  <line x1="320" y1="40" x2="320" y2="220" stroke="#818cf8" strokeWidth="1" strokeDasharray="3 3" opacity="0.25" className="animate-svg-dash" />
                </svg>

                <div className="space-y-4 relative z-10">
                  {/* Layer 3: Application Code Layer */}
                  <div className="bg-white/95 border border-slate-200 rounded-xl p-3 shadow-sm hover:border-indigo-400/50 transition-colors flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <span className="w-8 h-8 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-600">
                        <FolderArchive size={16} />
                      </span>
                      <div>
                        <div className="text-[10px] font-bold text-slate-800">1. APPLICATION CODE LAYER</div>
                        <div className="text-[9px] text-slate-500 font-mono mt-0.5">Controllers, Services, Configs & Filters</div>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <span className="text-[8px] bg-slate-100 text-slate-500 px-2 py-1 rounded font-mono">24 Classes</span>
                      <span className="text-[8px] bg-emerald-50 text-emerald-600 font-bold px-2 py-1 rounded">Parsed</span>
                    </div>
                  </div>

                  {/* Down indicator */}
                  <div className="flex justify-center -my-2">
                    <span className="text-[10px] text-slate-400 animate-bounce">↓</span>
                  </div>

                  {/* Layer 2: Cognee Semantic Graph layer */}
                  <div className="bg-white/95 border border-slate-200 rounded-xl p-3 shadow-sm hover:border-cyan-400/50 transition-colors flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <span className="w-8 h-8 rounded-lg bg-cyan-500/10 flex items-center justify-center text-cyan-600">
                        <Sparkles size={16} />
                      </span>
                      <div>
                        <div className="text-[10px] font-bold text-slate-800">2. COGNEE SEMANTIC LAYER</div>
                        <div className="text-[9px] text-slate-500 font-mono mt-0.5">Relation Extraction & Entity Mappings</div>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <span className="text-[8px] bg-slate-100 text-slate-500 px-2 py-1 rounded font-mono">85 Connections</span>
                      <span className="text-[8px] bg-cyan-50 text-cyan-600 font-bold px-2 py-1 rounded">Active</span>
                    </div>
                  </div>

                  {/* Down indicator */}
                  <div className="flex justify-center -my-2">
                    <span className="text-[10px] text-slate-400 animate-bounce">↓</span>
                  </div>

                  {/* Layer 1: Storage Layer */}
                  <div className="bg-white/95 border border-slate-200 rounded-xl p-3 shadow-sm hover:border-purple-400/50 transition-colors flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <span className="w-8 h-8 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-600">
                        <FileText size={16} />
                      </span>
                      <div>
                        <div className="text-[10px] font-bold text-slate-800">3. PERSISTENT STORAGE LAYER</div>
                        <div className="text-[9px] text-slate-500 font-mono mt-0.5">Qdrant Vector database & Neo4j Knowledge Graph</div>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <span className="text-[8px] bg-slate-100 text-slate-500 px-2 py-1 rounded font-mono">Dual-Engine</span>
                      <span className="text-[8px] bg-purple-50 text-purple-600 font-bold px-2 py-1 rounded">Synced</span>
                    </div>
                  </div>
                </div>

                {/* Simulated system console log strip */}
                <div className="bg-slate-200/50 border border-slate-200/80 rounded-lg p-2 flex items-center justify-between font-mono text-[9px] text-slate-600 mt-4 relative z-10">
                  <div className="flex items-center gap-1.5 truncate">
                    <span className="text-emerald-600 font-bold">✓</span>
                    <span className="text-slate-700 font-bold">System Status:</span>
                    <span className="truncate">devbrain indexer ➔ mapped context successfully in 420ms</span>
                  </div>
                  <span className="text-slate-500 hidden sm:inline">v1.0.0</span>
                </div>

              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Tech Strip */}
      <div className="w-full bg-white border-y border-slate-200 overflow-hidden py-4 select-none">
        <div className="tech-logo-scroll flex gap-0 items-center">
          <div className="flex gap-16 items-center flex-shrink-0 pr-16">
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="-11.5 -10.23 23 20.46" className="w-5 h-5 fill-none stroke-[#00d8ff] stroke-[1.5] animate-[spin_20s_linear_infinite]"><ellipse rx="11" ry="4.2"/><ellipse rx="11" ry="4.2" transform="rotate(60)"/><ellipse rx="11" ry="4.2" transform="rotate(120)"/><circle r="2" fill="#00d8ff"/></svg>
              <span>React</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 256 256" className="w-5 h-5"><defs><linearGradient id="vite-grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stopColor="#bd34fe"/><stop offset="100%" stopColor="#41b883"/></linearGradient></defs><path fill="url(#vite-grad)" d="M128 0L24 180h80l24 76 24-76h80z"/></svg>
              <span>Vite</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5 fill-[#38bdf8]"><path d="M12.001 4.8c-3.2 0-5.2 1.6-6 4.8 1.2-1.6 2.6-2.2 4.2-1.8.913.228 1.565.89 2.288 1.624C13.666 10.618 15.027 12 18.002 12c3.2 0 5.2-1.6 6-4.8-1.2 1.6-2.6 2.2-4.2 1.8-.913-.228-1.565-.89-2.288-1.624C16.337 6.182 14.976 4.8 12.001 4.8zm-6 7.2c-3.2 0-5.2 1.6-6 4.8 1.2-1.6 2.6-2.2 4.2-1.8.913.228 1.565.89 2.288 1.624 1.177 1.194 2.538 2.576 5.512 2.576 3.2 0 5.2-1.6 6-4.8-1.2 1.6-2.6 2.2-4.2 1.8-.913-.228-1.565-.89-2.288-1.624C10.336 13.382 8.975 12 6.001 12z"/></svg>
              <span>Tailwind CSS</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5 stroke-[#6366f1] stroke-[2] fill-none"><circle cx="12" cy="12" r="3" fill="#6366f1"/><circle cx="5" cy="5" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><circle cx="19" cy="5" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><circle cx="19" cy="19" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><circle cx="5" cy="19" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><line x1="5" y1="5" x2="9.8" y2="9.8"/><line x1="19" y1="5" x2="14.2" y2="9.8"/><line x1="19" y1="19" x2="14.2" y2="14.2"/><line x1="5" y1="19" x2="9.8" y2="14.2"/></svg>
              <span>React Force Graph</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><circle cx="12" cy="12" r="10" fill="#6db33f"/><path d="M12 6c-2.5 2.5-3.5 5.5-1.5 8s5.5 1.5 6.5-1c1-2.5-2.5-4.5-5-7zM9.5 14.5c-.5-.5-.5-1.2 0-1.7s1.2-.5 1.7 0" fill="#ffffff" opacity="0.95"/></svg>
              <span>Spring Boot</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><circle cx="12" cy="12" r="10" fill="#10b981"/><path d="M12 6c-2 2-3 4-1.5 6s4.5 1 5.5-.8c.8-1.8-2-3.2-4-5.2z" fill="#ffffff"/><circle cx="12" cy="12" r="1" fill="#10b981"/><path d="M8 12h8M12 8v8" stroke="#ffffff" strokeWidth="1.5" strokeDasharray="1 1"/></svg>
              <span>Spring AI</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><rect x="2" y="2" width="20" height="20" rx="4" fill="#8b5cf6"/><path d="M8 8a2 2 0 1 1 4 0 2 2 0 0 1-4 0zm6 8a2 2 0 1 1 4 0 2 2 0 0 1-4 0z" fill="#ffffff"/><path d="M12 9l2 5M10 11l4 2" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round"/></svg>
              <span>Cognee Cloud</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5 fill-[#3ecf8e]"><path d="M14 2L5 12.5h6.5v9.5l9-10.5h-6.5z"/></svg>
              <span>Supabase</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><circle cx="12" cy="12" r="10" fill="#f55036"/><path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5c1.66 0 3.13-.81 4.05-2.05l-1.42-1.42C14.05 14.18 13.1 14.5 12 14.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5c1.1 0 2.05.72 2.38 1.72h-2.38V12.5h3.9c-.1-2.5-2.1-4.5-4.9-4.5z" fill="#ffffff"/></svg>
              <span>Groq</span>
            </span>
          </div>
          <div className="flex gap-16 items-center flex-shrink-0 pr-16">
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="-11.5 -10.23 23 20.46" className="w-5 h-5 fill-none stroke-[#00d8ff] stroke-[1.5] animate-[spin_20s_linear_infinite]"><ellipse rx="11" ry="4.2"/><ellipse rx="11" ry="4.2" transform="rotate(60)"/><ellipse rx="11" ry="4.2" transform="rotate(120)"/><circle r="2" fill="#00d8ff"/></svg>
              <span>React</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 256 256" className="w-5 h-5"><defs><linearGradient id="vite-grad-2" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stopColor="#bd34fe"/><stop offset="100%" stopColor="#41b883"/></linearGradient></defs><path fill="url(#vite-grad-2)" d="M128 0L24 180h80l24 76 24-76h80z"/></svg>
              <span>Vite</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5 fill-[#38bdf8]"><path d="M12.001 4.8c-3.2 0-5.2 1.6-6 4.8 1.2-1.6 2.6-2.2 4.2-1.8.913.228 1.565.89 2.288 1.624C13.666 10.618 15.027 12 18.002 12c3.2 0 5.2-1.6 6-4.8-1.2 1.6-2.6 2.2-4.2 1.8-.913-.228-1.565-.89-2.288-1.624C16.337 6.182 14.976 4.8 12.001 4.8zm-6 7.2c-3.2 0-5.2 1.6-6 4.8 1.2-1.6 2.6-2.2 4.2-1.8.913.228 1.565.89 2.288 1.624 1.177 1.194 2.538 2.576 5.512 2.576 3.2 0 5.2-1.6 6-4.8-1.2 1.6-2.6 2.2-4.2 1.8-.913-.228-1.565-.89-2.288-1.624C10.336 13.382 8.975 12 6.001 12z"/></svg>
              <span>Tailwind CSS</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5 stroke-[#6366f1] stroke-[2] fill-none"><circle cx="12" cy="12" r="3" fill="#6366f1"/><circle cx="5" cy="5" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><circle cx="19" cy="5" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><circle cx="19" cy="19" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><circle cx="5" cy="19" r="2.5" fill="#6366f1" stroke="#ffffff" strokeWidth="1"/><line x1="5" y1="5" x2="9.8" y2="9.8"/><line x1="19" y1="5" x2="14.2" y2="9.8"/><line x1="19" y1="19" x2="14.2" y2="14.2"/><line x1="5" y1="19" x2="9.8" y2="14.2"/></svg>
              <span>React Force Graph</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><circle cx="12" cy="12" r="10" fill="#6db33f"/><path d="M12 6c-2.5 2.5-3.5 5.5-1.5 8s5.5 1.5 6.5-1c1-2.5-2.5-4.5-5-7zM9.5 14.5c-.5-.5-.5-1.2 0-1.7s1.2-.5 1.7 0" fill="#ffffff" opacity="0.95"/></svg>
              <span>Spring Boot</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><circle cx="12" cy="12" r="10" fill="#10b981"/><path d="M12 6c-2 2-3 4-1.5 6s4.5 1 5.5-.8c.8-1.8-2-3.2-4-5.2z" fill="#ffffff"/><circle cx="12" cy="12" r="1" fill="#10b981"/><path d="M8 12h8M12 8v8" stroke="#ffffff" strokeWidth="1.5" strokeDasharray="1 1"/></svg>
              <span>Spring AI</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><rect x="2" y="2" width="20" height="20" rx="4" fill="#8b5cf6"/><path d="M8 8a2 2 0 1 1 4 0 2 2 0 0 1-4 0zm6 8a2 2 0 1 1 4 0 2 2 0 0 1-4 0z" fill="#ffffff"/><path d="M12 9l2 5M10 11l4 2" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round"/></svg>
              <span>Cognee Cloud</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5 fill-[#3ecf8e]"><path d="M14 2L5 12.5h6.5v9.5l9-10.5h-6.5z"/></svg>
              <span>Supabase</span>
            </span>
            <span className="text-xs font-bold tracking-wider text-slate-500 uppercase flex items-center gap-2 select-none shrink-0 whitespace-nowrap">
              <svg viewBox="0 0 24 24" className="w-5 h-5"><circle cx="12" cy="12" r="10" fill="#f55036"/><path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5c1.66 0 3.13-.81 4.05-2.05l-1.42-1.42C14.05 14.18 13.1 14.5 12 14.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5c1.1 0 2.05.72 2.38 1.72h-2.38V12.5h3.9c-.1-2.5-2.1-4.5-4.9-4.5z" fill="#ffffff"/></svg>
              <span>Groq</span>
            </span>
          </div>
        </div>
      </div>

      {/* Problem Statement Section */}
      <section className="py-20 px-6 bg-slate-50 border-b border-slate-200 select-none">
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-16 items-center">
          <div className="lg:col-span-7 space-y-6">
            <div className="flex items-center gap-2 text-indigo-600 font-bold text-xs uppercase tracking-wider">
              <span className="w-2 h-2 rounded-full bg-indigo-600"></span>
              The Problem
            </div>
            <h2 className="text-3xl md:text-4xl font-extrabold text-slate-800 max-w-xl tracking-tight leading-tight">
              Software intelligence is broken by a fundamental lack of architectural context.
            </h2>
            <p className="text-md text-slate-500 max-w-xl leading-relaxed">
              Every time you start a new AI session, your assistant enters with amnesia. It doesn't know your patterns, your technical debt, or the subtle dependencies that hold your systems together. This leads to generic suggestions that ignore the unique reality of your codebase.
            </p>
            <hr className="border-slate-200 w-full my-6" />
            <div className="flex gap-12">
              <div>
                <div className="text-4xl font-black text-indigo-600">0</div>
                <div className="text-xs font-bold tracking-wider text-slate-400 uppercase mt-1">Persistent Context</div>
              </div>
              <div>
                <div className="text-4xl font-black text-slate-800">12+</div>
                <div className="text-xs font-bold tracking-wider text-slate-400 uppercase mt-1">Disjointed Tools</div>
              </div>
            </div>
          </div>
          
          {/* Signature Animation Node Box */}
          <div className="lg:col-span-5 flex justify-center">
            <div className="bg-white border border-slate-200 rounded-3xl p-8 aspect-square w-full max-w-sm flex items-center justify-center relative overflow-hidden shadow-md">
              
              {/* Concentric Dotted Rings in background */}
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="w-[280px] h-[280px] rounded-full border border-dashed border-slate-200/60 animate-[spin_60s_linear_infinite]"></div>
              </div>
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="w-[180px] h-[180px] rounded-full border border-dashed border-slate-200/80 animate-[spin_40s_linear_infinite_reverse]"></div>
              </div>

              {/* Central Core Nodes */}
              <div className="relative z-10 w-24 h-24 rounded-full bg-indigo-600 flex flex-col items-center justify-center text-white font-extrabold shadow-lg shadow-indigo-600/30 animate-pulse-central select-none border-2 border-indigo-400">
                <span className="text-sm tracking-wide">DevBrain</span>
                <span className="text-[9px] text-indigo-200 font-bold uppercase mt-0.5 tracking-wider">Memory</span>
              </div>

              {/* Dotted data streams feeding into the core */}
              <svg className="absolute inset-0 w-full h-full pointer-events-none" viewBox="0 0 400 400">
                {/* SVG path animation using dasharray offset */}
                <path d="M75,65 Q130,130 200,200" stroke="#818cf8" strokeWidth="1.5" fill="none" className="animate-svg-dash" opacity="0.6"></path>
                <path d="M325,65 Q270,130 200,200" stroke="#818cf8" strokeWidth="1.5" fill="none" className="animate-svg-dash" opacity="0.6"></path>
                <path d="M340,200 L200,200" stroke="#818cf8" strokeWidth="1.5" fill="none" className="animate-svg-dash" opacity="0.6"></path>
                <path d="M325,335 Q270,270 200,200" stroke="#818cf8" strokeWidth="1.5" fill="none" className="animate-svg-dash" opacity="0.6"></path>
                <path d="M75,335 Q130,270 200,200" stroke="#818cf8" strokeWidth="1.5" fill="none" className="animate-svg-dash" opacity="0.6"></path>
                <path d="M60,200 L200,200" stroke="#818cf8" strokeWidth="1.5" fill="none" className="animate-svg-dash" opacity="0.6"></path>
              </svg>

              {/* Custom Satellites with custom Lucide icons */}
              {/* Satellite 1: Repository files */}
              <div className="absolute top-[40px] left-[15px] bg-white border border-slate-200 rounded-2xl px-3 py-1.5 flex items-center gap-1.5 shadow-sm text-xs font-bold text-slate-700 animate-float-1">
                <span className="w-5 h-5 rounded-lg bg-indigo-50 flex items-center justify-center text-indigo-600">
                  <FolderArchive size={12} />
                </span>
                <span>Workspaces</span>
              </div>

              {/* Satellite 2: commits */}
              <div className="absolute top-[40px] right-[15px] bg-white border border-slate-200 rounded-2xl px-3 py-1.5 flex items-center gap-1.5 shadow-sm text-xs font-bold text-slate-700 animate-float-2">
                <span className="w-5 h-5 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600">
                  <GitCommit size={12} />
                </span>
                <span>Git Commits</span>
              </div>

              {/* Satellite 3: specifications */}
              <div className="absolute top-[185px] right-[5px] bg-white border border-slate-200 rounded-2xl px-3 py-1.5 flex items-center gap-1.5 shadow-sm text-xs font-bold text-slate-700 animate-float-3">
                <span className="w-5 h-5 rounded-lg bg-rose-50 flex items-center justify-center text-rose-600">
                  <FileText size={12} />
                </span>
                <span>specs.pdf</span>
              </div>

              {/* Satellite 4: Wiki doc files */}
              <div className="absolute bottom-[40px] right-[15px] bg-white border border-slate-200 rounded-2xl px-3 py-1.5 flex items-center gap-1.5 shadow-sm text-xs font-bold text-slate-700 animate-float-1">
                <span className="w-5 h-5 rounded-lg bg-purple-50 flex items-center justify-center text-purple-600">
                  <HelpCircle size={12} />
                </span>
                <span>Wiki Docs</span>
              </div>

              {/* Satellite 5: Notion details */}
              <div className="absolute bottom-[40px] left-[15px] bg-white border border-slate-200 rounded-2xl px-3 py-1.5 flex items-center gap-1.5 shadow-sm text-xs font-bold text-slate-700 animate-float-2">
                <span className="w-5 h-5 rounded-lg bg-amber-50 flex items-center justify-center text-amber-600">
                  <Notebook size={12} />
                </span>
                <span>Notion Notes</span>
              </div>

              {/* Satellite 6: AST and Graph elements */}
              <div className="absolute top-[185px] left-[5px] bg-white border border-slate-200 rounded-2xl px-3 py-1.5 flex items-center gap-1.5 shadow-sm text-xs font-bold text-slate-700 animate-float-3">
                <span className="w-5 h-5 rounded-lg bg-emerald-50 flex items-center justify-center text-emerald-600">
                  <Sparkles size={12} />
                </span>
                <span>ChatGPT Chats</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* The Pipeline Section */}
      <section id="features" className="py-20 px-6 select-none">
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <div className="space-y-6">
            <div className="flex items-center gap-2 text-indigo-600 font-bold text-xs uppercase tracking-wider">
              <span className="w-2 h-2 rounded-full bg-indigo-600"></span>
              The Pipeline
            </div>
            <h2 className="text-3xl font-extrabold text-slate-800 tracking-tight leading-tight">
              Automated structured memory for your engineering brain.
            </h2>
            <div className="space-y-8 pt-4">
              {/* Step 1 */}
              <ScrollReveal delay={100}>
                <div className="relative flex gap-4">
                  <div className="flex-shrink-0 w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center text-white font-extrabold z-10 text-sm">1</div>
                  <div>
                    <h4 className="text-lg font-bold text-slate-800">Ingest (remember)</h4>
                    <p className="text-sm text-slate-500 mt-1">Continuous syncing of your project context from Git, directories, or direct uploads.</p>
                  </div>
                </div>
              </ScrollReveal>
              {/* Step 2 */}
              <ScrollReveal delay={200}>
                <div className="relative flex gap-4">
                  <div className="flex-shrink-0 w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center text-white font-extrabold z-10 text-sm">2</div>
                  <div>
                    <h4 className="text-lg font-bold text-slate-800">Analyze (AST mapping)</h4>
                    <p className="text-sm text-slate-500 mt-1">Extraction of deep relationships between modules, classes, interfaces, and business logic.</p>
                  </div>
                </div>
              </ScrollReveal>
              {/* Step 3 */}
              <ScrollReveal delay={300}>
                <div className="relative flex gap-4">
                  <div className="flex-shrink-0 w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center text-white font-extrabold z-10 text-sm">3</div>
                  <div>
                    <h4 className="text-lg font-bold text-slate-800">Recall</h4>
                    <p className="text-sm text-slate-500 mt-1">Secure, persistent query routing of your architectural graph for grounded chat context.</p>
                  </div>
                </div>
              </ScrollReveal>
              {/* Step 4 */}
              <ScrollReveal delay={400}>
                <div className="relative flex gap-4">
                  <div className="flex-shrink-0 w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center text-white font-extrabold z-10 text-sm">4</div>
                  <div>
                    <h4 className="text-lg font-bold text-slate-800">Reinforce (improve)</h4>
                    <p className="text-sm text-slate-500 mt-1">Real-time feedback loop as you rate suggestions, updating graph weights on the fly.</p>
                  </div>
                </div>
              </ScrollReveal>
            </div>
          </div>

          <div className="flex justify-center">
            {/* Visual illustration box */}
            <div className="bg-slate-100 rounded-2xl border border-slate-200 p-8 w-full max-w-md aspect-square flex flex-col justify-center space-y-4">
              <div className="flex justify-between items-center bg-white px-4 py-3 rounded-xl border border-slate-200 shadow-sm">
                <span className="text-xs font-bold text-slate-700 font-mono">cognee.remember()</span>
                <span className="text-[10px] bg-indigo-50 text-indigo-700 font-bold px-2 py-0.5 rounded-full">AST Parser</span>
              </div>
              <div className="flex justify-between items-center bg-white px-4 py-3 rounded-xl border border-slate-200 shadow-sm ml-6">
                <span className="text-xs font-bold text-slate-700 font-mono">cognee.recall()</span>
                <span className="text-[10px] bg-indigo-50 text-indigo-700 font-bold px-2 py-0.5 rounded-full">Vector Search</span>
              </div>
              <div className="flex justify-between items-center bg-white px-4 py-3 rounded-xl border border-slate-200 shadow-sm ml-12">
                <span className="text-xs font-bold text-slate-700 font-mono">cognee.improve()</span>
                <span className="text-[10px] bg-indigo-50 text-indigo-700 font-bold px-2 py-0.5 rounded-full">Reinforced Weights</span>
              </div>
              <div className="flex justify-between items-center bg-white px-4 py-3 rounded-xl border border-slate-200 shadow-sm">
                <span className="text-xs font-bold text-slate-700 font-mono">cognee.forget()</span>
                <span className="text-[10px] bg-rose-50 text-rose-700 font-bold px-2 py-0.5 rounded-full">Flush Context</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How it Works Flow */}
      <section id="how-it-works" className="py-20 px-6 bg-slate-100 select-none">
        <div className="max-w-7xl mx-auto">
          <h2 className="text-3xl font-extrabold text-center text-slate-800 mb-16 tracking-tight">Code to Graph in Seconds</h2>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="flex flex-col items-center text-center">
              <div className="w-14 h-14 bg-indigo-600 text-white rounded-full flex items-center justify-center font-black mb-4 shadow-md text-lg">1</div>
              <h4 className="text-md font-bold text-slate-800 mb-1">Upload</h4>
              <p className="text-xs text-slate-500 leading-relaxed px-4">Upload a ZIP file of your codebase. We automatically clean it up.</p>
            </div>
            <div className="flex flex-col items-center text-center">
              <div className="w-14 h-14 bg-indigo-600 text-white rounded-full flex items-center justify-center font-black mb-4 shadow-md text-lg">2</div>
              <h4 className="text-md font-bold text-slate-800 mb-1">Build</h4>
              <p className="text-xs text-slate-500 leading-relaxed px-4">Our pipeline builds an entity-relationship graph of class hierarchies.</p>
            </div>
            <div className="flex flex-col items-center text-center">
              <div className="w-14 h-14 bg-indigo-600 text-white rounded-full flex items-center justify-center font-black mb-4 shadow-md text-lg">3</div>
              <h4 className="text-md font-bold text-slate-800 mb-1">Chat</h4>
              <p className="text-xs text-slate-500 leading-relaxed px-4">Ask any question grounded directly in the visual architecture tree.</p>
            </div>
            <div className="flex flex-col items-center text-center">
              <div className="w-14 h-14 bg-indigo-600 text-white rounded-full flex items-center justify-center font-black mb-4 shadow-md text-lg">4</div>
              <h4 className="text-md font-bold text-slate-800 mb-1">Improve</h4>
              <p className="text-xs text-slate-500 leading-relaxed px-4">Reinforce weights by upvoting outputs, refining future retrievals.</p>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section id="faq" className="py-20 px-6 select-none">
        <div className="max-w-[700px] mx-auto">
          <div className="text-center mb-16">
            <div className="flex items-center justify-center gap-2 text-indigo-600 font-bold text-xs uppercase tracking-wider mb-2">
              <span className="w-2 h-2 rounded-full bg-indigo-600"></span>
              FAQ
            </div>
            <h2 className="text-3xl font-extrabold text-slate-800 tracking-tight">Frequently Asked Questions</h2>
          </div>
          <div className="space-y-4">
            <details className="group bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
              <summary className="flex justify-between items-center p-5 cursor-pointer list-none text-md font-bold text-slate-800">
                How is this different from standard RAG?
                <ChevronDown className="w-5 h-5 text-slate-500 group-open:rotate-180 transition-transform duration-200" />
              </summary>
              <div className="px-5 pb-5 text-sm text-slate-500 leading-relaxed">
                Standard RAG treats your code as flat chunks. DevBrain constructs a semantic graph of logic, mapping classes, interfaces, and methods as nodes so it understands context-rich structural associations.
              </div>
            </details>
            <details className="group bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
              <summary className="flex justify-between items-center p-5 cursor-pointer list-none text-md font-bold text-slate-800">
                Does it support live file difference scanning?
                <ChevronDown className="w-5 h-5 text-slate-500 group-open:rotate-180 transition-transform duration-200" />
              </summary>
              <div className="px-5 pb-5 text-sm text-slate-500 leading-relaxed">
                Yes! When re-uploading branches or zips, DevBrain runs a file difference scan. It tells you exactly what files were added, modified, or removed compared to the last cache.
              </div>
            </details>
            <details className="group bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
              <summary className="flex justify-between items-center p-5 cursor-pointer list-none text-md font-bold text-slate-800">
                Is my API key and code safe?
                <ChevronDown className="w-5 h-5 text-slate-500 group-open:rotate-180 transition-transform duration-200" />
              </summary>
              <div className="px-5 pb-5 text-sm text-slate-500 leading-relaxed">
                Absolutely. DevBrain leverages secure client-side env files and a stateless Spring Boot gateway. Your data is isolated per-user via Supabase RLS.
              </div>
            </details>
          </div>
        </div>
      </section>

      {/* CTA Footer Wrapper */}
      <section className="py-16 px-6 select-none bg-slate-50 border-t border-slate-200">
        <div className="max-w-7xl mx-auto">
          <div className="bg-indigo-600 rounded-3xl p-10 md:p-16 text-center overflow-hidden relative shadow-lg">
            <div className="relative z-10 space-y-6">
              <h2 className="text-3xl md:text-5xl font-black text-white leading-tight">Ready to give your AI a brain?</h2>
              <p className="text-md text-indigo-100 max-w-xl mx-auto">Join thousands of developers who have stopped repeating codebase context to their assistants.</p>
              <div className="pt-4">
                <button 
                  onClick={() => navigate('/signup')} 
                  className="bg-white hover:bg-slate-50 text-indigo-600 hover:scale-[1.03] px-8 py-4 rounded-xl text-md font-bold transition-all shadow-md active:scale-95 cursor-pointer"
                >
                  Get Started Free
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Universal Footer */}
      <footer className="bg-slate-100 border-t border-slate-200 py-12 px-6 select-none">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="text-center md:text-left space-y-2 flex flex-col items-center md:items-start">
            <img src="/logo_normal.svg?v=3" alt="DevBrain Logo" className="h-12 w-auto object-contain mb-1" />
            <p className="text-xs text-slate-400">© 2026 DevBrain AI. All rights reserved.</p>
          </div>
          <div className="flex flex-wrap justify-center gap-6 text-xs text-slate-400 font-semibold">
            <a className="hover:text-indigo-600 transition-colors" href="#features">Features</a>
            <a className="hover:text-indigo-600 transition-colors" href="#how-it-works">How it Works</a>
            <a className="hover:text-indigo-600 transition-colors" href="#faq">FAQ</a>
            <a className="hover:text-indigo-600 transition-colors" target="_blank" rel="noreferrer" href="https://github.com/BhaveshDahake/DevBrain_Persistent-Architectural-Memory-for-AI-Coding-Assistants">GitHub</a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
