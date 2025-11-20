import React, { useEffect, useState } from "react";
import axios from "../../../api/axios";
import { getFlagColor, getFlagMeaning } from "../../../utils/flagUtils";

export default function FlagChart({ orderId: propOrderId = null, testResults = null }) {
  const [topFlags, setTopFlags] = useState([]);
  const [otherCount, setOtherCount] = useState(0);
  const [total, setTotal] = useState(0);
  const [hasResults, setHasResults] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const processTestResults = (results) => {
    if (!results) {
      setTopFlags([]);
      setOtherCount(0);
      setTotal(0);
      setHasResults(false);
      return;
    }

    // Get test result parameters
    let parameters = [];
    if (Array.isArray(results.testResultParameter)) {
      parameters = results.testResultParameter;
    } else if (Array.isArray(results)) {
      parameters = results;
    }

    if (parameters.length === 0) {
      setTopFlags([]);
      setOtherCount(0);
      setTotal(0);
      setHasResults(false);
      return;
    }

    // Count each unique flag
    const flagCounts = {};
    parameters.forEach(param => {
      const flag = param.flag ? param.flag.toString().toUpperCase() : 'N';
      flagCounts[flag] = (flagCounts[flag] || 0) + 1;
    });

    // Convert to array and sort by count (descending)
    const flagArray = Object.entries(flagCounts).map(([flag, count]) => ({
      flag,
      count,
      percentage: 0 // will calculate after we know total
    }));

    flagArray.sort((a, b) => b.count - a.count);

    const totalCount = parameters.length;
    const topN = 5;

    // Take top 5 (or all if fewer than 5)
    const topFlagsList = flagArray.slice(0, topN).map(item => ({
      ...item,
      percentage: Math.round((item.count / totalCount) * 100)
    }));

    // Calculate "Other" count for remaining flags
    const otherCountValue = flagArray.slice(topN).reduce((sum, item) => sum + item.count, 0);

    setTopFlags(topFlagsList);
    setOtherCount(otherCountValue);
    setTotal(totalCount);
    setHasResults(true);
  };

  useEffect(() => {
    if (testResults) {
      processTestResults(testResults);
    } else if (propOrderId) {
      // Nếu không có testResults trực tiếp, fetch từ server
      const fetchData = async () => {
        setLoading(true);
        setError(null);
        try {
          const res = await axios.get(`/test-orders/${propOrderId}`);
          const data = res.data;
          processTestResults(data?.result?.testResults);
        } catch (err) {
          setError(err.message || 'Failed to load test results');
          console.error('FlagChart fetch error:', err);
        } finally {
          setLoading(false);
        }
      };
      fetchData();
    }
  }, [propOrderId, testResults]);

  // Calculate percentage helper
  const pct = (n) => (total === 0 ? "NaN" : Math.round((n / total) * 100));

  // Prepare donut segments (values on 0-100 scale)
  // Ensure "Other" is always last, regardless of percentage
  let offset = 25;
  const segments = [];
  const pushSeg = (value, color, key) => {
    if (!value || value <= 0) return;
    const seg = { value, color, offset, key };
    offset -= value;
    segments.push(seg);
  };

  // Add segments for top flags first (sorted by percentage)
  topFlags.forEach(flagData => {
    const percentage = flagData.percentage;
    const color = getFlagColor(flagData.flag);
    pushSeg(percentage, color, flagData.flag);
  });

  // Add "Other" segment last if there are remaining flags
  if (otherCount > 0) {
    const otherPercentage = Math.round((otherCount / total) * 100);
    pushSeg(otherPercentage, "#9ca3af", "OTHER");
  }

  return (
    <aside className="card status-card" role="complementary" aria-labelledby="flag-chart-title">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq" aria-hidden="true">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M3 12h18M12 3v18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <h4 id="flag-chart-title" style={{ color: '#FF5A5A', fontSize: 18, fontWeight: 800, margin: 0 }}>Flag Chart</h4>
        </div>
      </div>

      <div className="donut-wrap" style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 16 }}>
        <svg 
          width="140" 
          height="140" 
          viewBox="0 0 42 42" 
          className="donut" 
          role="img"
          aria-label={`Flag distribution chart showing ${total} total test results`}
        >
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
            <div role="status" aria-live="polite" style={{ color: "#6b7280", textAlign: 'center' }}>Loading…</div>
          ) : error ? (
            <div role="alert" aria-live="assertive" style={{ color: "#FF5A5A", fontSize: 14, textAlign: 'center' }}>{error}</div>
          ) : total === 0 ? (
            <div role="status" style={{ color: "#6b7280", fontSize: 14, textAlign: 'center' }}>No results</div>
          ) : (
            <ul 
              style={{ listStyle: "none", padding: 0, margin: 0, fontSize: 14, display: 'flex', gap: 16, flexWrap: 'wrap', justifyContent: 'center' }}
              role="list"
              aria-label="Flag distribution legend"
            >
              {topFlags.map((flagData) => {
                const flagMeaning = getFlagMeaning(flagData.flag);
                return (
                  <li key={flagData.flag} style={{ display: "flex", alignItems: "center", gap: 8 }} role="listitem">
                    <span style={{ display: "inline-block", width: 24, height: 24, background: getFlagColor(flagData.flag), borderRadius: 6 }} aria-hidden="true" />
                    <span aria-label={`${flagMeaning}: ${flagData.percentage} percent`}>{flagMeaning} {flagData.percentage}%</span>
                  </li>
                );
              })}
              {otherCount > 0 && (
                <li style={{ display: "flex", alignItems: "center", gap: 8 }} role="listitem">
                  <span style={{ display: "inline-block", width: 24, height: 24, background: "#9ca3af", borderRadius: 6 }} aria-hidden="true" />
                  <span aria-label={`Other flags: ${pct(otherCount)} percent`}>{getFlagMeaning('OTHER')} {pct(otherCount)}%</span>
                </li>
              )}
            </ul>
          )}
        </div>
      </div>
    </aside>
  );
}