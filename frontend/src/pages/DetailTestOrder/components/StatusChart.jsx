import React from "react";

export default function StatusChart() {
  return (
    <aside className="card status-card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M3 12h18M12 3v18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <h4>Status Chart</h4>
        </div>
      </div>

      <div className="donut-wrap">
        <svg width="200" height="200" viewBox="0 0 42 42" className="donut">
          <circle className="donut-ring" cx="21" cy="21" r="15.9155" fill="transparent" stroke="#f3f4f6" strokeWidth="6"></circle>
          <circle className="donut-seg-normal" cx="21" cy="21" r="15.9155" fill="transparent" stroke="#10b981" strokeWidth="6" strokeDasharray="62.5 37.5" strokeDashoffset="25"></circle>
          <circle className="donut-seg-abnormal" cx="21" cy="21" r="15.9155" fill="transparent" stroke="#f59e0b" strokeWidth="6" strokeDasharray="25 75" strokeDashoffset="-12.5"></circle>
          <circle className="donut-seg-critical" cx="21" cy="21" r="15.9155" fill="transparent" stroke="#ef4444" strokeWidth="6" strokeDasharray="12.5 87.5" strokeDashoffset="-37.5"></circle>
        </svg>
      </div>
    </aside>
  );
}