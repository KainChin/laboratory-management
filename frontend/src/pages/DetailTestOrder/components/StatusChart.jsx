import React, { useEffect, useState } from "react";
import axios from "../../../api/axios";
import { calculateTestResultStats, getFlagMeaning } from "../../../utils/flagUtils";

export default function StatusChart({ orderId: propOrderId = null }) {
  const [stats, setStats] = useState({
    normal: 0,
    abnormal: 0,
    critical: 0,
    other: 0,
    hasResults: false
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;

    async function fetchData() {
      if (!propOrderId) {
        const parts = window.location.pathname.split("/").filter(Boolean);
        propOrderId = parts[parts.length - 1];
      }

      setLoading(true);
      setError(null);

      try {
        const res = await axios.get(`/test-orders/${propOrderId}`);
        const data = res.data;
        const testResults = data?.result?.testResults;
        const resultStats = calculateTestResultStats(testResults);

        if (!mounted) return;

        if (!resultStats.hasResults) {
          setStats({
            normal: 0,
            abnormal: 0,
            critical: 0,
            other: 0,
            hasResults: false
          });
          return;
        }

        // Calculate counts based on flags
        const counts = {
          normal: 0,
          high: 0,
          low: 0,
          other: 0,
          hasResults: true
        };

        Object.entries(resultStats.flagCounts).forEach(([flag, count]) => {
          const upperFlag = flag.toUpperCase();
          if (upperFlag === 'N') {
            counts.normal += count;
          } else if (upperFlag === 'H' || upperFlag === 'HH' || upperFlag === '>') {
            counts.high += count;
          } else if (upperFlag === 'L' || upperFlag === 'LL' || upperFlag === '<') {
            counts.low += count;
          } else {
            counts.other += count;
          }
        });

        setStats(counts);
      } catch (err) {
        if (!mounted) return;
        setError(err.message || 'Failed to load test results');
        console.error('StatusChart fetch error:', err);
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    fetchData();
    return () => {
      mounted = false;
    };
  }, [propOrderId]);

  const total = stats.normal + stats.high + stats.low + stats.other;
  const pct = (n) => (total === 0 ? "NaN" : Math.round((n / total) * 100)); // Remove decimals, show NaN if no data

  // prepare donut segments (values on 0-100 scale)
  const vNormal = total ? (stats.normal / total) * 100 : 0;
  const vHigh = total ? (stats.high / total) * 100 : 0;
  const vLow = total ? (stats.low / total) * 100 : 0;
  const vOther = total ? (stats.other / total) * 100 : 0;

  // cumulative offset (SVG circle uses strokeDashoffset; rotate start by 25)
  let offset = 25;
  const segments = [];
  const pushSeg = (value, color, key) => {
    if (!value || value <= 0) return;
    const seg = { value, color, offset, key };
    offset -= value;
    segments.push(seg);
  };
  pushSeg(vNormal, "#10b981", "N"); // Normal - xanh lá
  pushSeg(vHigh, "#ef4444", "H");   // High - đỏ
  pushSeg(vLow, "#f59e0b", "L");    // Low - cam
  pushSeg(vOther, "#9ca3af", "other");

  return (
    <aside className="card status-card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M3 12h18M12 3v18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>
          <h4>Status Chart</h4>
        </div>
      </div>

      <div className="donut-wrap" style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 16 }}>
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

        <div>
          <div style={{ fontSize: 14, fontWeight: 700, marginBottom: 8, textAlign: 'center' }}>Overview</div>
          {loading ? (
            <div style={{ color: "#6b7280", textAlign: 'center' }}>Loading…</div>
          ) : error ? (
            <div style={{ color: "#ef4444", fontSize: 14, textAlign: 'center' }}>{error}</div>
          ) : total === 0 ? (
            <div style={{ color: "#6b7280", fontSize: 14, textAlign: 'center' }}>No results</div>
          ) : (
            <ul style={{ listStyle: "none", padding: 0, margin: 0, fontSize: 14, display: 'flex', gap: 16, flexWrap: 'wrap', justifyContent: 'center' }}>
              {stats.normal > 0 && (
                <li style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <span style={{ display: "inline-block", width:10, height:10, background:"#10b981", borderRadius:3 }} />
                  <span>Normal {pct(stats.normal)}%</span>
                </li>
              )}
              {stats.high > 0 && (
                <li style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <span style={{ display: "inline-block", width:10, height:10, background:"#ef4444", borderRadius:3 }} />
                  <span>High {pct(stats.high)}%</span>
                </li>
              )}
              {stats.low > 0 && (
                <li style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <span style={{ display: "inline-block", width:10, height:10, background:"#f59e0b", borderRadius:3 }} />
                  <span>Low {pct(stats.low)}%</span>
                </li>
              )}
              {stats.other > 0 && (
                <li style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <span style={{ display: "inline-block", width:10, height:10, background:"#9ca3af", borderRadius:3 }} />
                  <span>Other {pct(stats.other)}%</span>
                </li>
              )}
            </ul>
          )}
        </div>
      </div>
    </aside>
  );
}