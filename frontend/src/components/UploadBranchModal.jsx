import React, { useState, useRef } from 'react';
import axios from 'axios';
import { X, UploadCloud, Loader2, AlertCircle } from 'lucide-react';

const UploadBranchModal = ({ isOpen, onClose, onUploadSuccess }) => {
  const [branchName, setBranchName] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [diffSummary, setDiffSummary] = useState(null);
  const fileInputRef = useRef();

  if (!isOpen) return null;

  if (diffSummary) {
    return (
      <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4 select-none animate-in fade-in duration-200">
        <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-200">
          {/* Header */}
          <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
            <h3 className="text-base font-bold text-slate-800 flex items-center gap-1.5">
              <span>🔄 Context Updated: What Changed?</span>
            </h3>
            <button 
              onClick={() => {
                setDiffSummary(null);
                setBranchName('');
                setSelectedFile(null);
                onClose();
              }} 
              className="text-slate-400 hover:text-slate-600 transition-colors"
            >
              <X size={18} />
            </button>
          </div>

          {/* Body */}
          <div className="p-6 space-y-4 max-h-[300px] overflow-y-auto">
            <p className="text-xs text-slate-500">
              DevBrain scanned the previous upload cache for <span className="font-semibold text-slate-700 font-mono">"{branchName}"</span> and compared it with your new upload ZIP. The following differences were detected:
            </p>

            {diffSummary.added.length > 0 && (
              <div className="space-y-1.5">
                <h4 className="text-xs font-bold text-emerald-600 flex items-center gap-1">
                  <span>➕ Added Files ({diffSummary.added.length})</span>
                </h4>
                <ul className="text-xs font-mono bg-emerald-50/50 border border-emerald-100/50 rounded-xl p-3 max-h-[100px] overflow-y-auto space-y-1 text-emerald-800">
                  {diffSummary.added.map((f, i) => <li key={i} className="break-all">{f}</li>)}
                </ul>
              </div>
            )}

            {diffSummary.modified.length > 0 && (
              <div className="space-y-1.5">
                <h4 className="text-xs font-bold text-amber-600 flex items-center gap-1">
                  <span>✏️ Modified Files ({diffSummary.modified.length})</span>
                </h4>
                <ul className="text-xs font-mono bg-amber-50/50 border border-amber-100/50 rounded-xl p-3 max-h-[100px] overflow-y-auto space-y-1 text-amber-800">
                  {diffSummary.modified.map((f, i) => <li key={i} className="break-all">{f}</li>)}
                </ul>
              </div>
            )}

            {diffSummary.removed.length > 0 && (
              <div className="space-y-1.5">
                <h4 className="text-xs font-bold text-rose-600 flex items-center gap-1">
                  <span>🗑️ Removed Files ({diffSummary.removed.length})</span>
                </h4>
                <ul className="text-xs font-mono bg-rose-50/50 border border-rose-100/50 rounded-xl p-3 max-h-[100px] overflow-y-auto space-y-1 text-rose-800">
                  {diffSummary.removed.map((f, i) => <li key={i} className="break-all">{f}</li>)}
                </ul>
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="bg-slate-50 px-6 py-4 border-t border-slate-100 flex justify-end">
            <button
              onClick={() => {
                setDiffSummary(null);
                setBranchName('');
                setSelectedFile(null);
                onClose();
              }}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm rounded-xl transition-all shadow-sm"
            >
              Continue to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.name.endsWith('.zip')) {
        setError('Only .zip files are allowed.');
        setSelectedFile(null);
      } else {
        setError(null);
        setSelectedFile(file);
      }
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) {
      if (!file.name.endsWith('.zip')) {
        setError('Only .zip files are allowed.');
        setSelectedFile(null);
      } else {
        setError(null);
        setSelectedFile(file);
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    const sanitizedName = branchName.trim().replace(/[^a-zA-Z0-9_-]/g, '');
    if (!sanitizedName) {
      setError('Please provide a valid branch/context name.');
      return;
    }
    if (!selectedFile) {
      setError('Please select a repository ZIP archive.');
      return;
    }

    setUploading(true);
    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('datasetName', sanitizedName);

    try {
      const response = await axios.post('/api/datasets/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      if (response.data.success) {
        const ext = response.data.extractionResult;
        if (ext && (ext.addedFiles.length > 0 || ext.modifiedFiles.length > 0 || ext.removedFiles.length > 0)) {
          setDiffSummary({
            added: ext.addedFiles,
            modified: ext.modifiedFiles,
            removed: ext.removedFiles
          });
          onUploadSuccess(sanitizedName);
        } else {
          onUploadSuccess(sanitizedName);
          setBranchName('');
          setSelectedFile(null);
          onClose();
        }
      } else {
        throw new Error(response.data.message || 'Upload failed');
      }
    } catch (err) {
      console.error('Upload failed:', err);
      setError(err.response?.data?.message || err.message || 'An error occurred during context indexing.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4 select-none">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200">
        
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
          <h3 className="text-base font-bold text-slate-800">Cognee remember(): Add Context</h3>
          <button 
            onClick={onClose} 
            disabled={uploading}
            className="text-slate-400 hover:text-slate-600 transition-colors disabled:opacity-50"
          >
            <X size={18} />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="flex items-start space-x-2.5 p-3.5 bg-rose-50 text-rose-700 border border-rose-100 rounded-xl text-xs">
              <AlertCircle size={16} className="mt-0.5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Name Field */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Branch / Context Name (Cognee Dataset)</label>
            <input 
              type="text"
              placeholder="e.g. auth-fix or devbrain-v2"
              value={branchName}
              onChange={(e) => setBranchName(e.target.value)}
              disabled={uploading}
              maxLength={30}
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-500 focus:bg-white transition-all font-medium"
            />
          </div>

          {/* Dropzone File Upload */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-500 uppercase tracking-wider">Repository ZIP File</label>
            <div 
              onDragOver={handleDragOver}
              onDrop={handleDrop}
              onClick={() => !uploading && fileInputRef.current.click()}
              className={`border-2 border-dashed rounded-2xl p-6 flex flex-col items-center justify-center cursor-pointer transition-all ${
                selectedFile 
                  ? 'border-indigo-500 bg-indigo-50/20' 
                  : 'border-slate-200 hover:border-slate-300 hover:bg-slate-50'
              } ${uploading ? 'pointer-events-none opacity-60' : ''}`}
            >
              <input 
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                accept=".zip"
                className="hidden"
              />
              <UploadCloud size={32} className={`mb-2.5 ${selectedFile ? 'text-indigo-500' : 'text-slate-400'}`} />
              {selectedFile ? (
                <div className="text-center">
                  <span className="text-sm font-semibold text-slate-800 block break-all px-4">{selectedFile.name}</span>
                  <span className="text-xs text-slate-400 font-mono mt-1 block">{(selectedFile.size / 1024 / 1024).toFixed(2)} MB</span>
                </div>
              ) : (
                <div className="text-center">
                  <span className="text-sm font-semibold text-slate-700 block">Click to upload or drag & drop</span>
                  <span className="text-xs text-slate-400 mt-1 block">Only ZIP folder context archives are supported</span>
                </div>
              )}
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center space-x-3 pt-2">
            <button 
              type="button"
              onClick={onClose}
              disabled={uploading}
              className="flex-1 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-sm rounded-xl transition-all disabled:opacity-50"
            >
              Cancel
            </button>
            <button 
              type="submit"
              disabled={uploading}
              className="flex-1 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm rounded-xl transition-all shadow-sm flex items-center justify-center space-x-1.5 disabled:opacity-50"
            >
              {uploading ? (
                <>
                  <Loader2 size={16} className="animate-spin" />
                  <span>Cognee remember(): Ingesting...</span>
                </>
              ) : (
                <span>Ingest & remember()</span>
              )}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};

export default UploadBranchModal;
