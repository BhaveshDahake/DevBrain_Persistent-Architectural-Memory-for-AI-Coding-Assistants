import React from 'react';
import { Database, Activity, Plus } from 'lucide-react';
import ResetContextButton from './ResetContextButton';

const DashboardHeader = ({ datasetName, loading, datasetList = [], onSelectDataset, onOpenUploadModal, onContextCleared }) => {
  return (
    <div className="flex items-center justify-between px-6 py-4 bg-white border-b border-slate-200 rounded-t-xl mb-6 shadow-sm select-none">
      <div className="flex items-center space-x-3">
        <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
          <Database size={20} />
        </div>
        <div>
          <h2 className="text-lg font-semibold text-slate-800 leading-tight animate-in fade-in duration-300">
            Active Repository
          </h2>
          <div className="flex items-center text-xs text-slate-500 mt-1.5 space-x-3">
            
            {/* Dropdown Branch Switcher */}
            {datasetList.length > 0 ? (
              <select
                value={datasetName || ''}
                onChange={(e) => onSelectDataset(e.target.value)}
                className="bg-slate-50 border border-slate-200 rounded-xl px-3 py-1.5 text-xs text-slate-600 font-bold focus:outline-none focus:border-indigo-500 cursor-pointer shadow-sm"
              >
                {datasetList.map(ds => (
                  <option key={ds} value={ds}>{ds}</option>
                ))}
              </select>
            ) : (
              <span className="font-bold text-slate-600">{datasetName || "None Selected"}</span>
            )}
            
            {/* Add New Branch / Context Trigger */}
            <button 
              onClick={onOpenUploadModal}
              className="flex items-center space-x-1 px-3 py-1.5 bg-indigo-50 text-indigo-600 border border-indigo-100 rounded-xl hover:bg-indigo-100 transition-colors text-xs font-bold shadow-sm"
              title="Add new repository / branch context"
            >
              <Plus size={14} className="stroke-[2.5]" />
              <span>New Branch</span>
            </button>

            {datasetName && (
              <span className="flex items-center pl-1">
                {loading ? (
                  <><Activity size={12} className="mr-1 animate-pulse text-amber-500" /> Syncing</>
                ) : (
                  <><span className="w-2 h-2 rounded-full bg-emerald-500 mr-1.5"></span> Connected</>
                )}
              </span>
            )}
          </div>
        </div>
      </div>
      
      <div className="flex items-center">
        {datasetName && (
          <ResetContextButton 
            datasetName={datasetName} 
            onResetSuccess={onContextCleared} 
          />
        )}
      </div>
    </div>
  );
};

export default DashboardHeader;
