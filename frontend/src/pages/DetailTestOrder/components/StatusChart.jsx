import React, { useEffect, useState } from "react";

export default function StatusChart({ orderId: propOrderId = null }) {
  // Hardcoded demo data for now
  const [counts, setCounts] = useState({ normal: 25, abnormal: 10, critical: 5, other: 3 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // ensure loading false and no fetch performed
    setLoading(false);
    setError(null);
    // If you want different demo sets per orderId you can switch here:
    // if (propOrderId === "demo2") setCounts({ normal: 40, abnormal: 5, critical: 2, other: 1 });
  }, [propOrderId]);

  const total = counts.normal + counts.abnormal + counts.critical + counts.other;
  const pct = (n) => (total === 0 ? 0 : Math.round((n / total) * 10000) / 100); // 2 decimals

  // prepare donut segments (values on 0-100 scale)
  const vNormal = total ? (counts.normal / total) * 100 : 0;
  const vAbnormal = total ? (counts.abnormal / total) * 100 : 0;
  const vCritical = total ? (counts.critical / total) * 100 : 0;
  const vOther = total ? (counts.other / total) * 100 : 0;

  // cumulative offset (SVG circle uses strokeDashoffset; rotate start by 25)
  let offset = 25;
  const segments = [];
  const pushSeg = (value, color, key) => {
    if (!value || value <= 0) return;
    const seg = { value, color, offset, key };
    offset -= value;
    segments.push(seg);
  };
  pushSeg(vNormal, "#10b981", "normal");
  pushSeg(vAbnormal, "#f59e0b", "abnormal");
  pushSeg(vCritical, "#ef4444", "critical");
  pushSeg(vOther, "#9ca3af", "other");

  return (
    <aside className="card status-card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M3 12h18M12 3v18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <h4>Status Chart (Demo)</h4>
        </div>
      </div>

      <div className="donut-wrap" style={{ display: "flex", alignItems: "center", gap: 16 }}>
        <svg width="140" height="140" viewBox="0 0 42 42" className="donut" aria-hidden>
          <circle className="donut-ring" cx="21" cy="21" r="15.9155" fill="transparent" stroke="#f3f4f6" strokeWidth="6"></circle>

          {segments.length === 0 ? (
            <circle cx="21" cy="21" r="15.9155" fill="transparent" stroke="#e5e7eb" strokeWidth="6" strokeDasharray="100 0" strokeDashoffset="0"></circle>
          ) : (
            segments.map((s) => (
              <circle
                key={s.key}
                cx="21"
                cy="21"
                r="15.9155"
                fill="transparent"
                stroke={s.color}
                strokeWidth="6"
                strokeDasharray={`${s.value} ${100 - s.value}`}
                strokeDashoffset={s.offset}
                strokeLinecap="butt"
              />
            ))
          )}
        </svg>

        <div style={{ minWidth: 120 }}>
          <div style={{ fontSize: 13, fontWeight: 700, marginBottom: 8 }}>Overview</div>
          {loading ? (
            <div style={{ color: "#6b7280" }}>Loading…</div>
          ) : error ? (
            <div style={{ color: "#ef4444", fontSize: 13 }}>{error}</div>
          ) : total === 0 ? (
            <div style={{ color: "#6b7280", fontSize: 13 }}>No results</div>
          ) : (
            <ul style={{ listStyle: "none", padding: 0, margin: 0, fontSize: 13 }}>
              <li style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                <div><span style={{ display: "inline-block", width:10, height:10, background:"#10b981", borderRadius:3, marginRight:8 }} />Normal</div>
                <div>{counts.normal} ({pct(counts.normal)}%)</div>
              </li>
              <li style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                <div><span style={{ display: "inline-block", width:10, height:10, background:"#f59e0b", borderRadius:3, marginRight:8 }} />Abnormal</div>
                <div>{counts.abnormal} ({pct(counts.abnormal)}%)</div>
              </li>
              <li style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                <div><span style={{ display: "inline-block", width:10, height:10, background:"#ef4444", borderRadius:3, marginRight:8 }} />Critical</div>
                <div>{counts.critical} ({pct(counts.critical)}%)</div>
              </li>
              {counts.other > 0 && (
                <li style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                  <div><span style={{ display: "inline-block", width:10, height:10, background:"#9ca3af", borderRadius:3, marginRight:8 }} />Other</div>
                  <div>{counts.other} ({pct(counts.other)}%)</div>
                </li>
              )}
            </ul>
          )}
        </div>
      </div>
    </aside>
  );
}