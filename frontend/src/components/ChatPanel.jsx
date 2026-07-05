import React, { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, ThumbsUp, ThumbsDown, CheckCircle2 } from 'lucide-react';
import axios from 'axios';

const MessageBubble = ({ message, onFeedback }) => {
  const [feedbackGiven, setFeedbackGiven] = useState(null);

  const handleFeedback = (isPositive) => {
    if (feedbackGiven !== null || !message.messageId) return;
    setFeedbackGiven(isPositive);
    onFeedback(message.messageId, isPositive);
  };

  const isAi = message.sender === 'ai';

  return (
    <div className={`flex w-full ${isAi ? 'justify-start' : 'justify-end'} mb-6`}>
      <div className={`flex max-w-[85%] ${isAi ? 'flex-row' : 'flex-row-reverse'}`}>
        
        <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${isAi ? 'bg-indigo-100 text-indigo-600 mr-3' : 'bg-slate-100 text-slate-600 ml-3'}`}>
          {isAi ? <Bot size={18} /> : <User size={18} />}
        </div>
        
        <div className="flex flex-col">
          <div className={`p-4 rounded-2xl ${isAi ? 'bg-slate-50 border border-slate-200 text-slate-800 rounded-tl-none' : 'bg-indigo-600 text-white rounded-tr-none'}`}>
            <p className="whitespace-pre-wrap text-sm leading-relaxed">{message.text}</p>
          </div>
          
          {isAi && (
            <div className="flex items-center space-x-4 mt-2 px-1">
              <span className="text-[10px] text-slate-400 font-medium">Cognee improve(): Help reinforce memory?</span>
              <div className="flex items-center space-x-1">
                {feedbackGiven === null ? (
                  <>
                    <button onClick={() => handleFeedback(true)} className="p-1 text-slate-400 hover:text-emerald-500 hover:bg-emerald-50 rounded transition-colors" title="Upvote (Cognee improve())">
                      <ThumbsUp size={14} />
                    </button>
                    <button onClick={() => handleFeedback(false)} className="p-1 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded transition-colors" title="Downvote (Cognee improve())">
                      <ThumbsDown size={14} />
                    </button>
                  </>
                ) : (
                  <span className="flex items-center text-[10px] font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">
                    <CheckCircle2 size={12} className="mr-1" /> Cognee improve(): Reinforced!
                  </span>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const ChatPanel = ({ datasetName, datasetList = [], onAiResponse }) => {
  const [chatsByRepo, setChatsByRepo] = useState(() => {
    try {
      const stored = localStorage.getItem("chatsByRepo");
      return stored ? JSON.parse(stored) : {};
    } catch (e) {
      return {};
    }
  });
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const scrollRef = useRef(null);

  useEffect(() => {
    localStorage.setItem("chatsByRepo", JSON.stringify(chatsByRepo));
  }, [chatsByRepo]);

  // Retrieve messages array for current datasetName (falls back to a default welcome message)
  const messages = chatsByRepo[datasetName] || [
    { id: 1, sender: 'ai', text: datasetName ? "Hello! I have loaded your repository. What would you like to know about the architecture or code?" : "Welcome to DevBrain! Please upload a new codebase or select an existing one to begin analyzing and chatting." }
  ];

  // Auto-scroll messages list
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  // Clean up cached chat histories for deleted repositories
  useEffect(() => {
    setChatsByRepo(prev => {
      const cleaned = {};
      datasetList.forEach(ds => {
        if (prev[ds]) {
          cleaned[ds] = prev[ds];
        }
      });
      return cleaned;
    });
  }, [datasetList]);

  // Clear text input box on repository context switch
  useEffect(() => {
    setInput("");
  }, [datasetName]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim() || !datasetName) return;

    const userMsg = input.trim();
    setInput("");

    // 1. Add User Message to the repository's chat array
    const userMsgObj = { id: Date.now(), sender: 'user', text: userMsg };
    setChatsByRepo(prev => {
      const current = prev[datasetName] || [
        { id: 1, sender: 'ai', text: "Hello! I have loaded your repository. What would you like to know about the architecture or code?" }
      ];
      return {
        ...prev,
        [datasetName]: [...current, userMsgObj]
      };
    });
    setLoading(true);

    try {
      const response = await axios.post('/api/chat/ask', {
        question: userMsg,
        datasetName: datasetName
      });

      if (response.data?.success !== false) {
        const aiMsgObj = { 
          id: Date.now() + 1, 
          messageId: response.data.messageId || `msg_${Date.now()}`,
          sender: 'ai', 
          text: response.data.answer || "No response provided." 
        };

        setChatsByRepo(prev => {
          const current = prev[datasetName] || [];
          return {
            ...prev,
            [datasetName]: [...current, aiMsgObj]
          };
        });

        if (response.data.referencedFiles) {
          onAiResponse({ referencedFiles: response.data.referencedFiles });
        }
      }
    } catch (err) {
      const errorMsgObj = { 
        id: Date.now() + 1, 
        sender: 'ai', 
        text: "Sorry, I encountered an error communicating with the repository memory." 
      };
      setChatsByRepo(prev => {
        const current = prev[datasetName] || [];
        return {
          ...prev,
          [datasetName]: [...current, errorMsgObj]
        };
      });
    } finally {
      setLoading(false);
    }
  };

  const submitFeedback = async (messageId, positive) => {
    try {
      await axios.post('/api/memory/improve', {
        datasetName,
        messageId,
        positive
      });
    } catch (e) {
      console.error("Failed to submit feedback", e);
    }
  };

  return (
    <div className="flex flex-col h-full bg-white">
      <div className="p-4 border-b border-slate-100 bg-slate-50 flex-shrink-0">
        <h3 className="font-semibold text-slate-800">Cognee recall() Assistant</h3>
        <p className="text-xs text-slate-500">Ask questions about {datasetName || 'the codebase'}</p>
      </div>

      <div ref={scrollRef} className="flex-1 overflow-y-auto p-4 custom-scrollbar">
        {messages.map(msg => (
          <MessageBubble key={msg.id} message={msg} onFeedback={submitFeedback} />
        ))}
        {loading && (
          <div className="flex justify-start mb-6">
            <div className="flex max-w-[85%] flex-row">
              <div className="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center bg-indigo-100 text-indigo-600 mr-3 animate-pulse">
                <Bot size={18} />
              </div>
              <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 text-slate-400 rounded-tl-none flex space-x-1.5 items-center">
                <div className="w-2 h-2 bg-slate-300 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-slate-300 rounded-full animate-bounce delay-75"></div>
                <div className="w-2 h-2 bg-slate-300 rounded-full animate-bounce delay-150"></div>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="p-4 bg-white border-t border-slate-100 flex-shrink-0">
        <form onSubmit={handleSend} className="relative">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSend(e);
              }
            }}
            placeholder="Cognee recall(): Ask about architecture, dependencies, boundaries..."
            className="w-full bg-slate-50 border border-slate-200 rounded-xl pl-4 pr-12 py-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent custom-scrollbar"
            rows="2"
            disabled={loading || !datasetName}
          />
          <button 
            type="submit" 
            disabled={loading || !input.trim() || !datasetName}
            className="absolute bottom-3 right-3 p-1.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors"
          >
            <Send size={16} />
          </button>
        </form>
        <p className="text-[10px] text-center text-slate-400 mt-2">Shift + Enter for new line</p>
      </div>
    </div>
  );
};

export default ChatPanel;
