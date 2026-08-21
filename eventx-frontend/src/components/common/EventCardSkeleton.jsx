import React from 'react';

const EventCardSkeleton = () => {
  return (
    <>
      <style>{`
        .skeleton-card {
          background-color: var(--bg-surface);
          border: 1px solid var(--border);
          border-radius: 12px;
          overflow: hidden;
          display: flex;
          flex-direction: column;
          height: 100%;
          min-height: 420px;
        }
        
        .skeleton-shimmer {
          background: linear-gradient(
            90deg,
            var(--bg-surface-hover) 0%,
            rgba(255, 255, 255, 0.05) 50%,
            var(--bg-surface-hover) 100%
          );
          background-size: 200% 100%;
          animation: shimmer 1.5s infinite linear;
        }
        
        @keyframes shimmer {
          0% { background-position: -200% 0; }
          100% { background-position: 200% 0; }
        }
        
        .skeleton-image {
          height: 180px;
          width: 100%;
        }
        
        .skeleton-content {
          padding: 20px;
          display: flex;
          flex-direction: column;
          flex-grow: 1;
        }
        
        .skeleton-title {
          height: 24px;
          width: 80%;
          border-radius: 4px;
          margin-bottom: 24px;
        }
        
        .skeleton-line {
          height: 16px;
          width: 60%;
          border-radius: 4px;
          margin-bottom: 12px;
        }
        
        .skeleton-line.short {
          width: 40%;
        }
        
        .skeleton-footer {
          margin-top: auto;
          padding-top: 16px;
          border-top: 1px solid var(--border);
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .skeleton-price {
          height: 20px;
          width: 60px;
          border-radius: 4px;
        }
        
        .skeleton-button {
          height: 36px;
          width: 100px;
          border-radius: 6px;
        }
      `}</style>
      <div className="skeleton-card">
        <div className="skeleton-image skeleton-shimmer"></div>
        <div className="skeleton-content">
          <div className="skeleton-title skeleton-shimmer"></div>
          <div className="skeleton-line skeleton-shimmer"></div>
          <div className="skeleton-line skeleton-shimmer"></div>
          <div className="skeleton-line short skeleton-shimmer"></div>
          
          <div className="skeleton-footer">
            <div className="skeleton-price skeleton-shimmer"></div>
            <div className="skeleton-button skeleton-shimmer"></div>
          </div>
        </div>
      </div>
    </>
  );
};

export default EventCardSkeleton;
