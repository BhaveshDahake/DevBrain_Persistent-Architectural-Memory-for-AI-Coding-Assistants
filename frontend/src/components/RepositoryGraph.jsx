import React, { useRef, useEffect, useState, useCallback, forwardRef, useImperativeHandle } from 'react';
import ForceGraph2D from 'react-force-graph-2d';

// Color palette aligned to user reference design: FOLDERS (gray/light-blue), FILES (blue), SERVICES (orange)
const TYPE_COLORS = {
  DIRECTORY: '#cbd5e1', // Folders
  MODULE: '#cbd5e1',    // Modules
  FILE: '#3b82f6',      // Files (Blue)
  CLASS: '#3b82f6',
  INTERFACE: '#3b82f6',
  METHOD: '#3b82f6',
  SERVICE: '#f59e0b',   // Services (Orange)
  UNKNOWN: '#cbd5e1'
};

const NODE_SIZES = {
  MODULE: 8, CLASS: 6, INTERFACE: 6, SERVICE: 6,
  FILE: 5, METHOD: 3, DIRECTORY: 7, UNKNOWN: 4
};

// Custom rounded rect helper for directory nodes and label backgrounds
const drawRoundedRect = (ctx, x, y, width, height, radius) => {
  ctx.beginPath();
  ctx.moveTo(x + radius, y);
  ctx.lineTo(x + width - radius, y);
  ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
  ctx.lineTo(x + width, y + height - radius);
  ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
  ctx.lineTo(x + radius, y + height);
  ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
  ctx.lineTo(x, y + radius);
  ctx.quadraticCurveTo(x, y, x + radius, y);
  ctx.closePath();
};

const RepositoryGraph = forwardRef(({ graphData, highlightedNodes, selectedNode, onNodeSelect, layoutMode, isOrbiting }, ref) => {
  const fgRef = useRef();
  const [hoverNode, setHoverNode] = useState(null);
  const angleRef = useRef(0);

  // Expose Zoom and Recenter controls to the parent component ref
  useImperativeHandle(ref, () => ({
    zoomIn: () => {
      if (fgRef.current) {
        const currentZoom = fgRef.current.zoom();
        fgRef.current.zoom(currentZoom * 1.3, 300);
      }
    },
    zoomOut: () => {
      if (fgRef.current) {
        const currentZoom = fgRef.current.zoom();
        fgRef.current.zoom(currentZoom / 1.3, 300);
      }
    },
    recenter: () => {
      if (fgRef.current) {
        fgRef.current.zoomToFit(400, 50);
      }
    }
  }));

  // Handle auto-orbiting animation
  useEffect(() => {
    let animationFrameId;
    
    const orbit = () => {
      if (isOrbiting && fgRef.current && graphData.nodes.length > 0) {
        angleRef.current += 0.0035; // slow speed of rotation
        const radius = 110; // orbit radius
        const x = radius * Math.cos(angleRef.current);
        const y = radius * Math.sin(angleRef.current);
        
        fgRef.current.centerAt(x, y);
      }
      animationFrameId = requestAnimationFrame(orbit);
    };

    if (isOrbiting) {
      orbit();
    }

    return () => cancelAnimationFrame(animationFrameId);
  }, [isOrbiting, graphData]);

  // Adjust graph layout and fit on layout toggles
  useEffect(() => {
    if (fgRef.current) {
      fgRef.current.d3ReheatSimulation();
      setTimeout(() => fgRef.current?.zoomToFit(400, 50), 250);
    }
  }, [layoutMode]);

  // Auto zoom-to-highlight elements when queried
  useEffect(() => {
    if (highlightedNodes?.size > 0 && fgRef.current) {
      const targetNodes = graphData.nodes.filter(n => highlightedNodes.has(n.path) || highlightedNodes.has(n.id));
      if (targetNodes.length > 0) {
        const xSum = targetNodes.reduce((acc, n) => acc + (n.x || 0), 0);
        const ySum = targetNodes.reduce((acc, n) => acc + (n.y || 0), 0);
        fgRef.current.centerAt(xSum / targetNodes.length, ySum / targetNodes.length, 1000);
        fgRef.current.zoom(4, 1000);
      }
    }
  }, [highlightedNodes, graphData.nodes]);

  // Initialize fitting on mount/data change
  useEffect(() => {
    if (graphData.nodes.length > 0) {
      setTimeout(() => fgRef.current?.zoomToFit(400, 50), 500);
    }
  }, [graphData]);

  const renderNode = useCallback((node, ctx, globalScale) => {
    const isHighlighted = highlightedNodes?.has(node.path) || highlightedNodes?.has(node.id);
    const isSelected = selectedNode?.id === node.id;
    const isHovered = hoverNode?.id === node.id;
    
    const baseColor = TYPE_COLORS[node.type] || TYPE_COLORS.UNKNOWN;
    const radius = NODE_SIZES[node.type] || NODE_SIZES.UNKNOWN;

    ctx.save();

    // 1. Draw glowing outer halo when active/hovered
    if (isHighlighted || isSelected || isHovered) {
      const pulseRadius = radius * (1.6 + 0.4 * Math.sin(Date.now() / 200));
      ctx.beginPath();
      ctx.arc(node.x, node.y, pulseRadius, 0, 2 * Math.PI, false);
      if (isHighlighted) {
        ctx.fillStyle = 'rgba(245, 158, 11, 0.15)';
      } else if (isSelected) {
        ctx.fillStyle = 'rgba(59, 130, 246, 0.15)';
      } else {
        ctx.fillStyle = 'rgba(99, 102, 241, 0.12)';
      }
      ctx.fill();

      // Dashed outer boundary ring
      ctx.beginPath();
      ctx.arc(node.x, node.y, radius * 1.9, 0, 2 * Math.PI, false);
      ctx.strokeStyle = isHighlighted ? 'rgba(245, 158, 11, 0.35)' : (isSelected ? 'rgba(59, 130, 246, 0.35)' : 'rgba(99, 102, 241, 0.25)');
      ctx.lineWidth = 0.8;
      ctx.setLineDash([2, 2]);
      ctx.stroke();
      ctx.setLineDash([]);
    }

    // 2. Draw soft drop shadow under the node
    ctx.shadowColor = 'rgba(15, 23, 42, 0.15)';
    ctx.shadowBlur = isHovered ? 8 : 4;
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = isHovered ? 3 : 1.5;

    // 3. Draw custom shapes based on node type
    if (node.type === 'DIRECTORY') {
      const size = radius * 2;
      ctx.fillStyle = baseColor;
      drawRoundedRect(ctx, node.x - radius, node.y - radius, size, size, 2.5);
      ctx.fill();
      
      ctx.shadowColor = 'transparent'; // Reset shadow for stroke
      ctx.lineWidth = isHovered ? 1.5 : 1.0;
      ctx.strokeStyle = isHovered ? '#0f172a' : '#ffffff';
      ctx.stroke();
    } else {
      ctx.beginPath();
      ctx.arc(node.x, node.y, radius, 0, 2 * Math.PI, false);
      ctx.fillStyle = baseColor;
      ctx.fill();
      
      ctx.shadowColor = 'transparent'; // Reset shadow for stroke
      ctx.lineWidth = isHovered ? 1.5 : 1.0;
      ctx.strokeStyle = isHovered ? '#0f172a' : '#ffffff';
      ctx.stroke();
    }

    // 4. Render clean tag labels with backdrop cards on hover/focus
    const showLabel = globalScale > 1.5 || isHovered || isHighlighted || isSelected;
    if (showLabel) {
      const labelText = node.label || node.id;
      const fontSize = Math.max(3.5, 10 / globalScale);
      ctx.font = `${isHovered || isSelected ? '600' : '400'} ${fontSize}px Inter, sans-serif`;
      
      const textWidth = ctx.measureText(labelText).width;
      const padX = 4;
      const padY = 2;
      const boxW = textWidth + padX * 2;
      const boxH = fontSize + padY * 2;
      const boxX = node.x - boxW / 2;
      const boxY = node.y + radius + 3;

      if (isHovered || isSelected || isHighlighted) {
        ctx.beginPath();
        drawRoundedRect(ctx, boxX, boxY, boxW, boxH, 3);
        ctx.fillStyle = 'rgba(255, 255, 255, 0.95)';
        ctx.shadowColor = 'rgba(15, 23, 42, 0.08)';
        ctx.shadowBlur = 4;
        ctx.shadowOffsetY = 1;
        ctx.fill();
        
        ctx.strokeStyle = isHighlighted ? 'rgba(245, 158, 11, 0.3)' : (isSelected ? 'rgba(59, 130, 246, 0.3)' : 'rgba(148, 163, 184, 0.2)');
        ctx.lineWidth = 0.5;
        ctx.stroke();
        
        ctx.fillStyle = '#0f172a'; // dark text
      } else {
        ctx.fillStyle = '#475569'; // slate-600 text
      }

      ctx.shadowColor = 'transparent';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'top';
      ctx.fillText(labelText, node.x, boxY + padY);
    }

    ctx.restore();
  }, [highlightedNodes, selectedNode, hoverNode]);

  return (
    <div className="w-full h-full bg-white rounded-lg overflow-hidden">
      <ForceGraph2D
        ref={fgRef}
        graphData={graphData}
        nodeLabel={() => ''}
        nodeColor={n => TYPE_COLORS[n.type] || TYPE_COLORS.UNKNOWN}
        nodeCanvasObject={renderNode}
        dagMode={layoutMode === 'force' ? null : (layoutMode === 'dag-td' ? 'td' : 'radialout')}
        dagLevelDistance={45}
        linkColor={link => {
          const isSourceHovered = hoverNode?.id === link.source.id || hoverNode?.id === link.source;
          const isTargetHovered = hoverNode?.id === link.target.id || hoverNode?.id === link.target;
          return (isSourceHovered || isTargetHovered) ? 'rgba(99, 102, 241, 0.4)' : 'rgba(148, 163, 184, 0.25)';
        }}
        linkWidth={link => {
          const isSourceHovered = hoverNode?.id === link.source.id || hoverNode?.id === link.source;
          const isTargetHovered = hoverNode?.id === link.target.id || hoverNode?.id === link.target;
          return (isSourceHovered || isTargetHovered) ? 2 : 1;
        }}
        linkDirectionalParticles={link => {
          const isSourceHovered = hoverNode?.id === link.source.id || hoverNode?.id === link.source;
          const isTargetHovered = hoverNode?.id === link.target.id || hoverNode?.id === link.target;
          const isSourceHighlighted = highlightedNodes?.has(link.source.path) || highlightedNodes?.has(link.source.id);
          const isTargetHighlighted = highlightedNodes?.has(link.target.path) || highlightedNodes?.has(link.target.id);
          const isSourceSelected = selectedNode?.id === link.source.id || selectedNode?.id === link.source;
          const isTargetSelected = selectedNode?.id === link.target.id || selectedNode?.id === link.target;
          
          return (isSourceHovered || isTargetHovered || isSourceSelected || isTargetSelected || isSourceHighlighted || isTargetHighlighted) ? 3 : 0;
        }}
        linkDirectionalParticleWidth={2}
        linkDirectionalParticleSpeed={0.008}
        linkDirectionalParticleColor={() => '#6366f1'}
        linkDirectionalArrowLength={3}
        linkDirectionalArrowRelPos={1}
        onNodeClick={(node) => {
          if (fgRef.current) {
            const screenCoords = fgRef.current.graph2ScreenCoords(node.x, node.y);
            onNodeSelect(node, screenCoords);
          } else {
            onNodeSelect(node, null);
          }
        }}
        onNodeHover={setHoverNode}
        d3VelocityDecay={0.3}
        cooldownTicks={100}
      />
    </div>
  );
});

export default RepositoryGraph;
