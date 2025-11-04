import React, { useEffect, useState } from "react";
import { ClipboardList, CheckCircle, AlertTriangle, AlertCircle } from "lucide-react";
import "../DetailTestOrder.css";

function mapStatus(status, flag) {
  const s = (status || "").toString().toUpperCase();

  // flag can be boolean (new API) or a string like HIGH/LOW/ABNORMAL
  if (flag === true || flag === "TRUE") return { label: "Abnormal", kind: "abnormal" };
  const f = (flag || "").toString().toUpperCase();
  if (f.includes("HIGH") || f.includes("LOW") || f.includes("ABNORMAL")) return { label: "Critical", kind: "critical" };

  if (s === "PENDING") return { label: "Pending", kind: "pending" };
  // treat completed with no flag as normal
  if (s === "COMPLETED") return { label: "Normal", kind: "normal" };
  if (s === "VALIDATED") return { label: "Abnormal", kind: "abnormal" };
  if (s === "APPROVED") return { label: "Normal", kind: "normal" };
  return { label: s || "-", kind: "unknown" };
}

function StatusBadge({ kind, label }) {
  return <span className={`dto-badge dto-badge-${kind}`}>{label}</span>;
}

function getFlagClass(flag) {
  // API may provide boolean flags or string reasons
  if (!flag) return "dto-flag-default";
  const f = flag.toString().toUpperCase();
  if (f.includes("HIGH")) return "dto-flag-high";
  if (f.includes("LOW")) return "dto-flag-low";
  if (f === "NORMAL") return "dto-flag-normal";
  if (f === "ABNORMAL") return "dto-flag-high";
  return "dto-flag-default";
}

// Infer flag from numeric value and reference range when API provides a boolean flag
function inferFlagFromValue(r) {
  if (!r) return null;
  const v = r.value;
  const min = r.minValue;
  const max = r.maxValue;
  if ((v === null || v === undefined) || (min === undefined && max === undefined)) return null;
  const num = Number(v);
  if (!Number.isFinite(num)) return null;
  if (min !== undefined && max !== undefined) {
    if (num > Number(max)) return "HIGH";
    if (num < Number(min)) return "LOW";
    return "NORMAL";
  }
  // if only one bound exists
  if (min !== undefined) {
    if (num < Number(min)) return "LOW";
    return "NORMAL";
  }
  if (max !== undefined) {
    if (num > Number(max)) return "HIGH";
    return "NORMAL";
  }
  return null;
}

export default function TestResult({ tests = null, orderId = null }) {
  const [rows, setRows] = useState(Array.isArray(tests) ? tests : []);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;

    async function fetchIfNeeded() {
      if (Array.isArray(tests) && tests.length) {
        setRows(tests);
        return;
      }

      setLoading(true);
      setError(null);
      try {
        const id =
          orderId ??
          (() => {
            const parts = window.location.pathname.split("/").filter(Boolean);
            return parts[parts.length - 1];
          })();

        const res = await fetch(`http://localhost:6868/api/test-orders/${id}`);
        if (!res.ok) throw new Error(`Fetch failed ${res.status}`);
        const payload = await res.json();
        const src = payload?.result ?? payload ?? {};
        const rr = Array.isArray(src.testResults) ? src.testResults : [];
        if (!mounted) return;
        setRows(rr);
        console.log("TestResult fetched rows:", rr);
      } catch (err) {
        if (!mounted) return;
        setError(err.message || "Failed to load test results");
        console.error("TestResult fetch error:", err);
      } finally {
        if (!mounted) return;
        setLoading(false);
      }
    }

    fetchIfNeeded();
    return () => {
      mounted = false;
    };
  }, [tests, orderId]);

  const renderIcon = (kind) => {
    if (kind === "normal") return <CheckCircle size={16} className="icon-normal" />;
    if (kind === "abnormal") return <AlertTriangle size={16} className="icon-abnormal" />;
    if (kind === "critical") return <AlertCircle size={16} className="icon-critical" />;
    return <ClipboardList size={16} />;
  };

  const fmtValue = (r) => {
    if (r == null || (r.value === null || r.value === undefined)) return "-";
    return r.unit ? `${r.value} ${r.unit}` : String(r.value);
  };

  const fmtRef = (r) => {
    // new API uses minValue / maxValue
    if (r.minValue !== undefined && r.maxValue !== undefined) return `${r.minValue} - ${r.maxValue}`;
    return r.minValue ?? r.maxValue ?? "-";
  };

  return (
    <section className="dto-card">
      <div className="dto-card-header">
        <div className="dto-icon"><ClipboardList size={16} /></div>
        <h3 className="dto-title">Test Result</h3>
      </div>

      {/* Removed scroll wrappers so no scrollbar appears */}
      <table className="dto-table">
        <thead>
          <tr>
            <th className="dto-th icon-col"></th>
            <th className="dto-th name-col">Test Name</th>
            <th className="dto-th result-col">Result</th>
            <th className="dto-th ref-col">Reference Range</th>
            <th className="dto-th status-col">Status</th>
            <th className="dto-th flag-col">Flag</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={6} className="dto-empty">Loading...</td></tr>
          ) : error ? (
            <tr><td colSpan={6} className="dto-empty dto-error">Error: {error}</td></tr>
          ) : rows.length === 0 ? (
            <tr><td colSpan={6} className="dto-empty">No test results</td></tr>
          ) : (
            rows.map((t) => {
              // if API gives boolean flag, try to infer HIGH/LOW/NORMAL from values
              const inferred = t.flag === true ? inferFlagFromValue(t) : null;
              const flagText = inferred ? inferred : (t.flag === false ? null : (t.flag ?? null));
              const mapped = mapStatus(t.status, flagText);
              const flagClass = getFlagClass(flagText);
              return (
                <tr key={t.resultId} className="dto-row">
                  <td className="dto-td icon-col">{renderIcon(mapped.kind)}</td>

                  {/* removed createdBy / createdAt meta from Test Name cell */}
                  <td className="dto-td name-col">
                    <div className="dto-param">{t.parameter ?? "-"}</div>
                  </td>

                  <td className="dto-td result-col">{fmtValue(t)}</td>

                  <td className="dto-td ref-col">{fmtRef(t)}</td>

                  <td className="dto-td status-col"><StatusBadge kind={mapped.kind} label={mapped.label} /></td>

                  <td className="dto-td flag-col">
                    <span className={`dto-flag ${flagClass}`}>{
                      // show inferred text when boolean flag provided, otherwise show provided string
                      t.flag === true ? (inferred === "NORMAL" ? "-" : (inferred ?? "ABNORMAL")) : (t.flag === false ? "-" : (t.flag ? String(t.flag).toUpperCase() : "-"))
                    }</span>
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </section>
  );
}