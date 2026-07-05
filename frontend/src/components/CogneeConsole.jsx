import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Terminal, ChevronUp, ChevronDown, Trash2, Cpu } from 'lucide-react';

const CogneeConsole = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [logs, setLogs] = useState([]);
  const [selectedLog, setSelectedLog] = useState(null);

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const res = await axios.get('/api/cognee/logs');
        setLogs(res.data || []);
      } catch (e) {
        console.error("Failed to fetch logs", e);
      }
    };
    
    fetchLogs();
    const interval = setInterval(fetchLogs, isOpen ? 1500 : 5000);
    return () => clearInterval(interval);
  }, [isOpen]);

  const handleClearLogs = async () => {
    try {
      await axios.delete('/api/cognee/logs');
      setLogs([]);
      setSelectedLog(null);
    } catch (e) {
      console.error("Failed to clear logs", e);
    }
  };

  return (
    <div className="border-t border-slate-200 bg-slate-900 text-slate-300 font-mono text-xs select-none">
      {/* Header bar */}
      <div 
        onClick={() => setIsOpen(prev => !prev)}
        className="flex items-center justify-between px-6 py-3 cursor-pointer hover:bg-slate-800 transition-colors"
      >
        <div className="flex items-center space-x-2.5">
          <Terminal size={15} className="text-emerald-500 animate-pulse" />
          <span className="font-bold text-slate-100">Cognee Cloud API Console Logs</span>
          <span className="px-2 py-0.5 rounded-full text-[10px] bg-slate-800 border border-slate-700 text-slate-400 font-bold">
            {logs.length} calls
          </span>
        </div>
        <div className="flex items-center space-x-4">
          <button 
            onClick={(e) => {
              e.stopPropagation();
              handleClearLogs();
            }}
            className="p-1 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded transition-colors"
            title="Clear Console History"
          >
            <Trash2 size={13} />
          </button>
          {isOpen ? <ChevronDown size={16} /> : <ChevronUp size={16} />}
        </div>
      </div>

      {/* Drawer content */}
      {isOpen && (
        <div className="h-60 border-t border-slate-800 flex divide-x divide-slate-800 animate-in slide-in-from-bottom duration-200">
          {/* Logs list (Left half) */}
          <div className="w-1/2 overflow-y-auto custom-scrollbar p-2 space-y-1 bg-slate-950">
            {logs.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-slate-500 space-y-2 py-8">
                <Cpu size={24} className="stroke-[1.5]" />
                <span className="text-[10px] uppercase tracking-wider font-bold">No API calls detected yet. Ingest a branch or query the assistant.</span>
              </div>
            ) : (
              [...logs].reverse().map((log, index) => {
                const isErr = log.status >= 400;
                const isSelected = selectedLog?.timestamp === log.timestamp;
                return (
                  <div 
                    key={index}
                    onClick={() => setSelectedLog(log)}
                    className={`flex items-center justify-between px-3 py-1.5 rounded cursor-pointer transition-colors ${
                      isSelected ? 'bg-slate-800 text-slate-100' : 'hover:bg-slate-900/50'
                    }`}
                  >
                    <div className="flex items-center space-x-3 truncate">
                      <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                        isErr ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      }`}>
                        {log.method}
                      </span>
                      <span className="font-semibold text-slate-200 text-[11px]">{log.endpoint}</span>
                    </div>
                    <div className="flex items-center space-x-3 text-[10px] font-semibold text-slate-500 flex-shrink-0">
                      <span>{log.latencyMs}ms</span>
                      <span className={isErr ? 'text-rose-400' : 'text-emerald-400'}>
                        {log.status}
                      </span>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Details (Right half) */}
          <div className="w-1/2 overflow-y-auto custom-scrollbar p-4 bg-slate-900">
            {selectedLog ? (
              <div className="space-y-4">
                <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                  <span className="text-[10px] font-bold text-slate-500 font-mono">TIMESTAMP: {selectedLog.timestamp}</span>
                  <span className="text-[10px] font-bold text-slate-500 font-mono">LATENCY: {selectedLog.latencyMs}ms</span>
                </div>
                <div className="space-y-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-indigo-400 font-mono">Request Body</span>
                  <pre className="p-3 bg-slate-950 rounded-lg border border-slate-800 overflow-x-auto text-[11px] text-slate-300 leading-relaxed font-mono">
                    {selectedLog.requestPayload}
                  </pre>
                </div>
                <div className="space-y-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-400 font-mono">Response Body / Error</span>
                  <pre className="p-3 bg-slate-950 rounded-lg border border-slate-800 overflow-x-auto text-[11px] text-slate-300 leading-relaxed font-mono">
                    {selectedLog.responsePayload}
                  </pre>
                </div>
              </div>
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-slate-500 space-y-2 py-8">
                <Terminal size={20} className="stroke-[1.5]" />
                <span className="text-[10px] uppercase tracking-wider font-bold">Select an API call entry to inspect details</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CogneeConsole;
