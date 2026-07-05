import React, { useState } from 'react';
import { Trash2, AlertTriangle, Loader2 } from 'lucide-react';
import axios from 'axios';

const ResetContextButton = ({ datasetName, onResetSuccess }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!datasetName) return null;

  const handleReset = async () => {
    if (loading) return;
    setLoading(true);
    setError(null);

    try {
      const response = await axios.delete(`/api/memory/context/${datasetName}`, {
        timeout: 60000
      });

      if (response.data?.success) {
        setIsOpen(false);
        onResetSuccess();
      } else {
        throw new Error(response.data?.message || "Unknown error occurred.");
      }
    } catch (err) {
      console.error("Context reset failed:", err);
      setError("Unable to reset repository context. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button
        onClick={() => setIsOpen(true)}
        className="flex items-center space-x-2 px-4 py-2 text-sm font-medium text-red-600 bg-red-50 hover:bg-red-100 hover:text-red-700 rounded-lg transition-colors border border-red-100"
        title="Clear repository memory (Cognee forget())"
      >
        <Trash2 size={16} />
        <span>Cognee forget(): Reset</span>
      </button>

      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 animate-in zoom-in-95 duration-200">
            
            <div className="flex items-start space-x-4 mb-4">
              <div className="p-3 bg-red-100 text-red-600 rounded-full flex-shrink-0">
                <AlertTriangle size={24} />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-slate-900">Reset Context (Cognee forget())?</h3>
                <p className="text-sm text-slate-500 mt-1 leading-relaxed">
                  This permanently calls <strong className="font-semibold text-red-600">forget()</strong> on Cognee's hybrid memory layer and clears all local files and chat history for <strong className="font-medium text-slate-700">{datasetName}</strong>. This cannot be undone.
                </p>
              </div>
            </div>

            {error && (
              <div className="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg border border-red-100">
                {error}
              </div>
            )}

            <div className="flex items-center justify-end space-x-3 mt-6">
              <button
                onClick={() => setIsOpen(false)}
                disabled={loading}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={handleReset}
                disabled={loading}
                className="flex items-center px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg shadow-sm transition-all active:scale-95 disabled:opacity-50 disabled:active:scale-100"
              >
                {loading ? <Loader2 size={16} className="animate-spin mr-2" /> : <Trash2 size={16} className="mr-2" />}
                {loading ? "Resetting..." : "Reset Context"}
              </button>
            </div>
            
          </div>
        </div>
      )}
    </>
  );
};

export default ResetContextButton;
