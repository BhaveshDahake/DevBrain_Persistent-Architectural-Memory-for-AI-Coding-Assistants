import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AuthPage = () => {
  const { signUp, signIn, signInWithOAuth, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const isSignUpPath = location.pathname === '/signup';
  const [activeTab, setActiveTab] = useState(isSignUpPath ? 'signup' : 'signin');
  
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Sync tab active state if route changes
  useEffect(() => {
    setActiveTab(location.pathname === '/signup' ? 'signup' : 'signin');
    setError(null);
  }, [location.pathname]);

  // Redirect to dashboard if user is already signed in
  useEffect(() => {
    if (user) {
      navigate('/dashboard', { replace: true });
    }
  }, [user, navigate]);

  const handleTabToggle = (tab) => {
    setActiveTab(tab);
    setError(null);
    if (tab === 'signup') {
      navigate('/signup');
    } else {
      navigate('/login');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!email || !password) {
      setError('Please provide email and password.');
      return;
    }

    if (activeTab === 'signup' && !fullName) {
      setError('Please provide your full name.');
      return;
    }

    setLoading(true);

    try {
      if (activeTab === 'signup') {
        const { error: signUpError } = await signUp(email, password, fullName);
        if (signUpError) throw signUpError;
        setError('Signup successful! Please check your email for verification links or sign in.');
      } else {
        const { error: signInError } = await signIn(email, password);
        if (signInError) throw signInError;
        navigate('/dashboard');
      }
    } catch (err) {
      console.error('Auth action failed:', err);
      setError(err.message || 'An authentication error occurred.');
    } finally {
      setLoading(false);
    }
  };

  const handleOAuth = async (provider) => {
    setError(null);
    try {
      const { error: oAuthError } = await signInWithOAuth(provider);
      if (oAuthError) throw oAuthError;
    } catch (err) {
      console.error('OAuth sign in failed:', err);
      setError(err.message || `Failed to sign in with ${provider}`);
    }
  };

  return (
    <div className="bg-slate-50 min-h-screen flex items-center justify-center p-6 font-sans text-slate-800 relative overflow-hidden select-none">
      
      {/* Node-graph structural background */}
      <style>{`
        .node-pattern {
          background-color: #f8fafc;
          background-image: radial-gradient(#cbd5e1 0.7px, transparent 0.7px);
          background-size: 24px 24px;
          mask-image: radial-gradient(circle at center, black 30%, transparent 90%);
        }
      `}</style>
      <div className="absolute inset-0 node-pattern pointer-events-none opacity-40"></div>

      <div className="relative w-full max-w-[440px] z-10">
        
        {/* Branding */}
        <div className="flex flex-col items-center mb-8 cursor-pointer" onClick={() => navigate('/')}>
          <img src="/logo_stacked.png?v=2" alt="DevBrain Logo" className="h-24 w-auto object-contain" />
          <p className="text-slate-400 text-[10px] mt-2 font-bold tracking-widest uppercase">The Intelligent Architect</p>
        </div>

        {/* Card */}
        <main className="bg-white border border-slate-200 rounded-2xl shadow-xl p-8">
          
          {/* Segmented Control Header */}
          <div className="relative flex bg-slate-100 rounded-xl p-1.5 mb-6 overflow-hidden border border-slate-200/50">
            <div 
              className={`absolute h-[calc(100%-12px)] w-[calc(50%-6px)] bg-white rounded-lg shadow-sm transition-transform duration-200 ${
                activeTab === 'signup' ? 'translate-x-full' : 'translate-x-0'
              }`} 
            />
            <button 
              type="button"
              onClick={() => handleTabToggle('signin')}
              className={`relative z-10 w-1/2 py-2 text-xs font-bold uppercase tracking-wider transition-colors focus:outline-none cursor-pointer ${
                activeTab === 'signin' ? 'text-indigo-600' : 'text-slate-500'
              }`}
            >
              Sign In
            </button>
            <button 
              type="button"
              onClick={() => handleTabToggle('signup')}
              className={`relative z-10 w-1/2 py-2 text-xs font-bold uppercase tracking-wider transition-colors focus:outline-none cursor-pointer ${
                activeTab === 'signup' ? 'text-indigo-600' : 'text-slate-500'
              }`}
            >
              Sign Up
            </button>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            
            {error && (
              <div className={`p-4 rounded-xl text-xs font-medium border ${
                error.includes('successful')
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                  : 'bg-rose-50 text-rose-700 border-rose-100'
              }`}>
                {error}
              </div>
            )}

            {/* Name Field (Sign Up Only) */}
            {activeTab === 'signup' && (
              <div className="space-y-1.5 animate-in fade-in duration-200">
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider" htmlFor="fullName">Full Name</label>
                <input 
                  id="fullName" 
                  type="text" 
                  placeholder="John Doe" 
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  required={activeTab === 'signup'}
                  disabled={loading}
                  className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500/10 focus:border-indigo-600 focus:bg-white transition-all outline-none text-sm text-slate-800 placeholder-slate-400 font-medium"
                />
              </div>
            )}

            {/* Email Field */}
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider" htmlFor="email">Email Address</label>
              <input 
                id="email" 
                type="email" 
                placeholder="dev@example.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                disabled={loading}
                className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500/10 focus:border-indigo-600 focus:bg-white transition-all outline-none text-sm text-slate-800 placeholder-slate-400 font-medium"
              />
            </div>

            {/* Password Field */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center">
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider" htmlFor="password">Password</label>
                {activeTab === 'signin' && (
                  <a className="text-[11px] text-slate-400 hover:text-indigo-600 font-semibold transition-colors" href="#">Forgot password?</a>
                )}
              </div>
              <div className="relative">
                <input 
                  id="password" 
                  type={showPassword ? 'text' : 'password'} 
                  placeholder="••••••••" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  disabled={loading}
                  className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500/10 focus:border-indigo-600 focus:bg-white pr-10 transition-all outline-none text-sm text-slate-800 placeholder-slate-400 font-medium font-mono"
                />
                <button 
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-indigo-600 transition-colors focus:outline-none cursor-pointer"
                >
                  <span className="text-xs font-bold select-none">{showPassword ? 'Hide' : 'Show'}</span>
                </button>
              </div>
            </div>

            {/* Action Submit Button */}
            <button 
              type="submit" 
              disabled={loading}
              className="w-full bg-indigo-600 hover:bg-indigo-700 hover:scale-[1.01] text-white font-bold text-xs uppercase tracking-wider py-3.5 rounded-xl shadow-md active:scale-95 transition-all flex items-center justify-center gap-1.5 cursor-pointer mt-2 disabled:opacity-60"
            >
              {loading ? (
                <>
                  <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span>Loading...</span>
                </>
              ) : (
                <>
                  <span>{activeTab === 'signin' ? 'Sign In' : 'Sign Up'}</span>
                </>
              )}
            </button>
          </form>

          {/* Social Divider */}
          <div className="relative my-6 select-none">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200"></div>
            </div>
            <div className="relative flex justify-center text-[10px] uppercase font-bold tracking-wider">
              <span className="bg-white px-3 text-slate-400">or continue with</span>
            </div>
          </div>

          {/* OAuth Buttons */}
          <div className="grid grid-cols-2 gap-4">
            <button 
              type="button"
              onClick={() => handleOAuth('github')}
              disabled={loading}
              className="flex items-center justify-center gap-2 px-4 py-2.5 border border-slate-200 rounded-xl hover:bg-slate-50 transition-all active:scale-95 text-slate-600 font-bold text-xs uppercase tracking-wider cursor-pointer shadow-sm disabled:opacity-50"
            >
              <svg className="w-4 h-4 fill-current text-slate-600" viewBox="0 0 24 24"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"></path></svg>
              <span>GitHub</span>
            </button>
            <button 
              type="button"
              onClick={() => handleOAuth('google')}
              disabled={loading}
              className="flex items-center justify-center gap-2 px-4 py-2.5 border border-slate-200 rounded-xl hover:bg-slate-50 transition-all active:scale-95 text-slate-600 font-bold text-xs uppercase tracking-wider cursor-pointer shadow-sm disabled:opacity-50"
            >
              <svg className="w-4 h-4 text-slate-600" viewBox="0 0 24 24"><path d="M5.266 9.765A7.077 7.077 0 0 1 12 4.909c1.69 0 3.218.6 4.418 1.582L19.91 3C17.782 1.145 15.055 0 12 0 7.27 0 3.198 2.698 1.24 6.65l4.026 3.115z" fill="#EA4335"></path><path d="M16.04 18.013c-1.09.693-2.459 1.078-4.04 1.078a7.077 7.077 0 0 1-6.723-4.823l-4.04 3.067C3.186 21.352 7.283 24 12 24c3.11 0 5.927-1.03 8.11-2.808l-4.07-3.179z" fill="#34A853"></path><path d="M23.605 12.24c0-.82-.07-1.61-.19-2.37H12v4.51h6.47c-.29 1.48-1.15 2.73-2.43 3.58l4.07 3.18c2.38-2.19 3.495-5.42 3.495-8.9z" fill="#4285F4"></path><path d="M5.277 14.268A7.12 7.12 0 0 1 4.909 12c0-.782.125-1.533.357-2.235L1.24 6.65A11.934 11.934 0 0 0 0 12c0 1.92.445 3.73 1.237 5.335l4.04-3.067z" fill="#FBBC05"></path></svg>
              <span>Google</span>
            </button>
          </div>

          {/* Sign Up Terms Check */}
          {activeTab === 'signup' && (
            <div className="mt-6 animate-in fade-in duration-300">
              <p className="text-slate-400 text-center text-[11px] leading-relaxed">
                By signing up, you agree to our{' '}
                <a className="underline hover:text-indigo-600 transition-colors" href="#">Terms of Service</a> and{' '}
                <a className="underline hover:text-indigo-600 transition-colors" href="#">Privacy Policy</a>.
              </p>
            </div>
          )}
        </main>

        {/* Footer Navigation Switch */}
        <div className="mt-8 text-center">
          <p className="text-slate-400 text-sm font-semibold select-none">
            {activeTab === 'signin' ? (
              <>
                Don't have an account?{' '}
                <button 
                  onClick={() => handleTabToggle('signup')} 
                  className="text-indigo-600 hover:underline cursor-pointer font-bold transition-all"
                >
                  Sign up
                </button>
              </>
            ) : (
              <>
                Already have an account?{' '}
                <button 
                  onClick={() => handleTabToggle('signin')} 
                  className="text-indigo-600 hover:underline cursor-pointer font-bold transition-all"
                >
                  Sign in
                </button>
              </>
            )}
          </p>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
