import React, { useState } from 'react';
import ChatPanel from './ChatPanel';
import GraphPanel from './GraphPanel';
import CogneeConsole from './CogneeConsole';

const Dashboard = ({ activeDataset, datasetList }) => {
  const [highlightedNodes, setHighlightedNodes] = useState(new Set());
  const [isMaximized, setIsMaximized] = useState(false);

  const handleAiResponse = (responseDto) => {
    if (responseDto.referencedFiles && responseDto.referencedFiles.length > 0) {
      setHighlightedNodes(new Set(responseDto.referencedFiles));
      
      setTimeout(() => {
        setHighlightedNodes(new Set());
      }, 8000);
    }
  };

  return (
    <div className="flex flex-col gap-6 h-[calc(100vh-120px)]">
      <div className="flex flex-1 gap-6 min-h-0 transition-all duration-300">
        {/* Left Chat Panel: collapsed when Graph is maximized */}
        {!isMaximized && (
          <div className="w-1/3 flex flex-col bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
            <ChatPanel 
              datasetName={activeDataset} 
              datasetList={datasetList}
              onAiResponse={handleAiResponse} 
            />
          </div>
        )}

        {/* Right Graph Panel: expands to w-full when maximized */}
        <div className={`${isMaximized ? 'w-full' : 'w-2/3'} h-full rounded-xl transition-all duration-300`}>
          <GraphPanel 
            datasetName={activeDataset}
            highlightedNodes={highlightedNodes}
            isMaximized={isMaximized}
            onToggleMaximize={() => setIsMaximized(prev => !prev)}
          />
        </div>
      </div>
      
      {/* Collapsible Cognee API Console Drawer */}
      <div className="flex-shrink-0 bg-slate-900 border border-slate-700 rounded-xl overflow-hidden shadow-lg">
        <CogneeConsole />
      </div>
    </div>
  );
};

export default Dashboard;
