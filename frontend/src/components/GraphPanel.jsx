import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Network, Plus, Minus, Target, Maximize2, Minimize2, Loader2 } from 'lucide-react';
import RepositoryGraph from './RepositoryGraph';
import NodeInspector from './NodeInspector';

const GraphPanel = ({ datasetName, highlightedNodes, isMaximized, onToggleMaximize }) => {
  const [graphData, setGraphData] = useState({ nodes: [], links: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedNode, setSelectedNode] = useState(null);
  const [selectedNodeCoords, setSelectedNodeCoords] = useState(null);
  const [layoutMode, setLayoutMode] = useState('force');
  const [isOrbiting, setIsOrbiting] = useState(false);
  const graphRef = useRef();

  const handleNodeSelect = (node, coords) => {
    setSelectedNode(node);
    setSelectedNodeCoords(coords);
  };

  // Listen to space bar keydowns to toggle orbiting rotation
  useEffect(() => {
    const handleKeyDown = (e) => {
      // Toggle orbit if Space is pressed, and not inside an input/textarea
      if (e.code === 'Space' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
        e.preventDefault();
        setIsOrbiting(prev => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  useEffect(() => {
    // Clear inspector on repository context switch
    setSelectedNode(null);
    setSelectedNodeCoords(null);

    const fetchGraph = async () => {
      if (!datasetName) return;
      setLoading(true);
      setError(null);

      try {
        const response = await axios.get(`/api/graph/${datasetName}`);
        if (response.data.success || response.data.nodes) {
          setGraphData({
            nodes: response.data.nodes || [],
            links: response.data.links || []
          });
        } else {
          throw new Error(response.data.message);
        }
      } catch (err) {
        console.error("Failed to fetch graph:", err);
        setError("Unable to inspect repository graph.");
      } finally {
        setLoading(false);
      }
    };

    fetchGraph();
  }, [datasetName]);

  if (loading) {
    return (
      <div className="h-full w-full flex flex-col items-center justify-center bg-white rounded-xl border border-slate-200 shadow-sm">
        <Loader2 className="animate-spin text-indigo-600 mb-4" size={32} />
        <span className="text-slate-500 font-medium tracking-wide">Mapping Codebase Architecture...</span>
      </div>
    );
  }

  if (error || graphData.nodes.length === 0) {
    return (
      <div className="h-full w-full flex flex-col items-center justify-center bg-white rounded-xl border border-slate-200 shadow-sm text-slate-400">
        <Network size={48} className="mb-4 opacity-55 text-slate-300" />
        <p>{error || "No repository graph available."}</p>
      </div>
    );
  }

  return (
    <div className="relative h-full w-full rounded-xl overflow-hidden border border-slate-200 bg-white shadow-sm flex flex-col group font-sans-original">
      
      {/* Header Overlay Controls with absolute positioning */}
      <div className="absolute inset-x-4 top-4 z-10 pointer-events-none">
        
        {/* Top-Left: Layout Switcher Capsule */}
        <div className="absolute top-0 left-0 bg-slate-100/90 backdrop-blur border border-slate-200 rounded-full p-1 flex items-center shadow-md pointer-events-auto space-x-1">
          <button 
            onClick={() => setLayoutMode('force')} 
            className={`px-4 py-1.5 text-xs font-bold tracking-wider rounded-full transition-all ${layoutMode === 'force' ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
          >
            FORCE
          </button>
          <button 
            onClick={() => setLayoutMode('dag-td')} 
            className={`px-4 py-1.5 text-xs font-bold tracking-wider rounded-full transition-all ${layoutMode === 'dag-td' ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
          >
            TREE
          </button>
          <button 
            onClick={() => setLayoutMode('radial')} 
            className={`px-4 py-1.5 text-xs font-bold tracking-wider rounded-full transition-all ${layoutMode === 'radial' ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
          >
            RADIAL
          </button>
        </div>

        {/* Below Switcher: Vertical Zoom Controls */}
        <div className="absolute top-16 left-0 flex flex-col bg-white border border-slate-200 rounded-xl shadow-md pointer-events-auto p-1 space-y-1">
          <button 
            onClick={() => graphRef.current?.zoomIn()} 
            className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors" 
            title="Zoom In"
          >
            <Plus size={16} />
          </button>
          <div className="h-px bg-slate-200 mx-2"></div>
          <button 
            onClick={() => graphRef.current?.zoomOut()} 
            className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors" 
            title="Zoom Out"
          >
            <Minus size={16} />
          </button>
          <div className="h-px bg-slate-200 mx-2"></div>
          <button 
            onClick={() => graphRef.current?.recenter()} 
            className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors" 
            title="Recenter"
          >
            <Target size={16} />
          </button>
        </div>

        {/* Top-Right: Indexing Status Card & Maximize Button */}
        <div className="absolute top-0 right-0 flex items-center space-x-3 pointer-events-auto">
          <div className="bg-white border border-slate-200 rounded-xl px-4 py-1.5 shadow-md flex flex-col items-center min-w-32">
            <span className="text-[9px] font-bold text-slate-400 uppercase tracking-widest leading-none mb-1">INDEXING STATUS</span>
            <span className="text-xs font-bold text-emerald-600 leading-none">Complete</span>
          </div>
          <button 
            onClick={onToggleMaximize}
            className={`p-3 border rounded-xl shadow-md transition-colors ${isMaximized ? 'bg-blue-50 border-blue-200 text-blue-600 hover:bg-blue-100' : 'bg-white border-slate-200 text-slate-500 hover:text-slate-800 hover:bg-slate-100'}`} 
            title={isMaximized ? "Restore Split View" : "Maximize View"}
          >
            {isMaximized ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
          </button>
        </div>

      </div>

      {/* Main Canvas Component */}
      <div className="flex-grow w-full h-[calc(100%-40px)]">
        <RepositoryGraph 
          ref={graphRef}
          graphData={graphData}
          highlightedNodes={highlightedNodes}
          selectedNode={selectedNode}
          onNodeSelect={handleNodeSelect}
          layoutMode={layoutMode}
          isOrbiting={isOrbiting}
        />
      </div>

      {/* Bottom Legend & Camera Prompt Bar */}
      <div className="h-10 bg-slate-50 border-t border-slate-200 flex items-center justify-between px-6 text-xs text-slate-500 font-medium select-none z-10">
        <div className="flex items-center space-x-5">
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-slate-400/80"></span>
            <span className="font-semibold tracking-wider text-[10px] text-slate-600">FOLDERS</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-blue-500"></span>
            <span className="font-semibold tracking-wider text-[10px] text-slate-600">FILES</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
            <span className="font-semibold tracking-wider text-[10px] text-slate-600">SERVICES</span>
          </div>
        </div>
        <div className="text-slate-400 font-normal">
          Press <span className="px-1.5 py-0.5 bg-white border border-slate-200 rounded font-semibold text-[10px] text-slate-500 shadow-sm">Space</span> to {isOrbiting ? 'stop' : 'toggle'} camera
        </div>
      </div>

      {/* Node Inspector Floating Popover Card */}
      <NodeInspector 
        node={selectedNode} 
        graphData={graphData} 
        onClose={() => handleNodeSelect(null, null)} 
        position={selectedNodeCoords}
      />
    </div>
  );
};

export default GraphPanel;
