import React, { useState, useEffect } from 'react';
import DashboardHeader from '../components/DashboardHeader';
import Dashboard from '../components/Dashboard';
import UploadBranchModal from '../components/UploadBranchModal';
import { useAuth } from '../context/AuthContext';

const DashboardPage = () => {
  const { signOut, user } = useAuth();
  const userId = user?.id || 'anon';
  const [activeDataset, setActiveDataset] = useState("");
  const [uploadedDatasets, setUploadedDatasets] = useState([]);
  const [showSuccessToast, setShowSuccessToast] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);

  // Load user-scoped settings
  useEffect(() => {
    const activeKey = `activeDataset_${userId}`;
    const storedActive = localStorage.getItem(activeKey);
    if (storedActive) {
      setActiveDataset(storedActive);
    } else {
      setActiveDataset("");
    }

    const listKey = `uploadedDatasets_${userId}`;
    try {
      const storedList = localStorage.getItem(listKey);
      setUploadedDatasets(storedList ? JSON.parse(storedList) : []);
    } catch (e) {
      setUploadedDatasets([]);
    }
  }, [userId]);

  // Save user-scoped settings
  useEffect(() => {
    if (activeDataset) {
      localStorage.setItem(`activeDataset_${userId}`, activeDataset);
    }
  }, [activeDataset, userId]);

  useEffect(() => {
    localStorage.setItem(`uploadedDatasets_${userId}`, JSON.stringify(uploadedDatasets));
  }, [uploadedDatasets, userId]);

  const handleContextCleared = () => {
    const clearedDataset = activeDataset;
    setUploadedDatasets(prev => {
      return prev.filter(ds => ds !== clearedDataset);
    });
    setActiveDataset("");
    setShowSuccessToast(true);
    setTimeout(() => setShowSuccessToast(false), 4000);
  };

  const handleUploadSuccess = (newBranch) => {
    setUploadedDatasets(prev => {
      if (prev.includes(newBranch)) return prev;
      return [...prev, newBranch];
    });
    setActiveDataset(newBranch);
  };

  return (
    <div className="min-h-screen bg-slate-100 p-8 select-none relative">
      {/* Logout button header integration */}
      <div className="max-w-[95%] mx-auto mb-4 flex justify-between items-center bg-white border border-slate-200 shadow-sm p-4 rounded-2xl">
        <div className="flex flex-col">
          <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Logged In User</span>
          <span className="text-sm font-semibold text-slate-800">{user?.email}</span>
        </div>
        <button 
          onClick={signOut} 
          className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl active:scale-95 transition-all cursor-pointer border border-slate-200"
        >
          Sign Out
        </button>
      </div>

      {showSuccessToast && (
        <div className="fixed top-6 right-6 z-50 flex items-center p-4 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-xl shadow-lg animate-in slide-in-from-top-4 duration-300">
          <span className="font-semibold text-sm">Repository context cleared. Ready for a fresh upload.</span>
        </div>
      )}

      {activeDataset ? (
        <div className="max-w-[95%] mx-auto">
          <DashboardHeader 
            datasetName={activeDataset} 
            datasetList={uploadedDatasets}
            onSelectDataset={setActiveDataset}
            onOpenUploadModal={() => setShowUploadModal(true)}
            loading={false} 
            onContextCleared={handleContextCleared} 
          />
          <Dashboard activeDataset={activeDataset} datasetList={uploadedDatasets} />
        </div>
      ) : (
        <div className="max-w-xl mx-auto mt-20 text-center bg-white border border-slate-200 shadow-xl rounded-2xl p-10 animate-in zoom-in-95 duration-200">
          <h2 className="text-2xl font-bold text-slate-800 mb-2">No Active Context</h2>
          <p className="text-slate-500 mb-8 text-sm">Please upload a new repository ZIP archive to start parsing and inspecting codebase boundaries.</p>
          <button 
            onClick={() => setShowUploadModal(true)}
            className="px-6 py-3 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 transition-colors shadow-sm font-bold text-sm"
          >
            Upload Repository ZIP
          </button>
        </div>
      )}

      {/* Upload Branch / Context Modal */}
      <UploadBranchModal 
        isOpen={showUploadModal}
        onClose={() => setShowUploadModal(false)}
        onUploadSuccess={handleUploadSuccess}
      />
    </div>
  );
};

export default DashboardPage;
