import React from 'react';
import { X, FileCode, Box, Layers, PlayCircle, Folder } from 'lucide-react';

const ICONS = {
  FILE: <FileCode size={18} className="text-emerald-600" />,
  CLASS: <Box size={18} className="text-blue-600" />,
  MODULE: <Layers size={18} className="text-red-600" />,
  METHOD: <PlayCircle size={18} className="text-pink-600" />,
  DIRECTORY: <Folder size={18} className="text-slate-600" />
};

const NodeInspector = ({ node, graphData, onClose, position }) => {
  if (!node) return null;

  const dependencies = graphData.links.filter(l => l.source.id === node.id || l.source === node.id);
  const dependents = graphData.links.filter(l => l.target.id === node.id || l.target === node.id);

  const getPositionStyles = () => {
    if (!position) return { top: '1rem', right: '1rem', position: 'absolute', zIndex: 30 };
    // Offset card horizontally: left side if right-aligned, otherwise right side
    const offsetLeft = position.x > 450 ? position.x - 340 : position.x + 20;
    // Offset card vertically: clamp to min 10px from top
    const offsetTop = Math.max(10, position.y - 120);
    return {
      position: 'absolute',
      left: `${offsetLeft}px`,
      top: `${offsetTop}px`,
      zIndex: 30
    };
  };

  return (
    <div 
      style={getPositionStyles()}
      className="w-80 bg-white text-slate-700 shadow-2xl rounded-xl border border-slate-200 p-5 flex flex-col animate-in zoom-in-95 duration-200"
    >
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center space-x-3">
          {ICONS[node.type] || <Box size={18} className="text-slate-500" />}
          <div>
            <h3 className="font-semibold text-slate-800 break-all leading-tight">{node.label}</h3>
            <span className="text-xs text-slate-500 font-mono mt-1">{node.type}</span>
          </div>
        </div>
        <button onClick={onClose} className="text-slate-400 hover:text-slate-700 transition-colors">
          <X size={18} />
        </button>
      </div>

      <div className="space-y-4 overflow-y-auto max-h-[60vh] pr-2 custom-scrollbar">
        {node.path && (
          <div className="bg-slate-50 p-3 rounded-lg border border-slate-200/50">
            <span className="text-xs text-slate-400 uppercase tracking-wider block mb-1">Location</span>
            <span className="text-sm font-mono text-slate-600 break-all">{node.path}</span>
          </div>
        )}

        <div className="grid grid-cols-2 gap-3">
          <div className="bg-slate-50 p-3 rounded-lg border border-slate-200/50 text-center">
            <span className="block text-xl font-semibold text-blue-600">{dependencies.length}</span>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider">Dependencies</span>
          </div>
          <div className="bg-slate-50 p-3 rounded-lg border border-slate-200/50 text-center">
            <span className="block text-xl font-semibold text-purple-600">{dependents.length}</span>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider">Used By</span>
          </div>
        </div>

        {node.metadata && Object.keys(node.metadata).length > 0 && (
          <div>
            <span className="text-xs text-slate-400 uppercase tracking-wider block mb-2 mt-4">Raw Properties</span>
            <pre className="text-[10px] bg-slate-50 p-3 rounded-lg overflow-x-auto text-slate-600 border border-slate-200/50">
              {JSON.stringify(node.metadata, null, 2)}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
};

export default NodeInspector;
