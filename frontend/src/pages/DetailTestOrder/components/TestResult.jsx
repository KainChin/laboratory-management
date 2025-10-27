import React from "react";
import { ClipboardList, CheckCircle, AlertTriangle, AlertCircle } from "lucide-react";

function StatusBadge({ status }) {
  if (status === "Normal") return <span className="badge green">Normal</span>;
  if (status === "Abnormal") return <span className="badge yellow">Abnormal</span>;
  return <span className="badge red">Critical</span>;
}

export default function TestResult({ tests = [] }) {
  return (
    <section className="card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <ClipboardList size={14} />
          </div>
          <h3>Test Result</h3>
        </div>
      </div>

      <table className="result-table">
        <thead>
          <tr>
            <th></th>
            <th>Test Name</th>
            <th>Result</th>
            <th>Reference Range</th>
            <th>Status</th>
            <th>Flag</th>
          </tr>
        </thead>
        <tbody>
          {tests.map((t, i) => (
            <tr key={i}>
              <td className="icon-col">
                {t.status === "Normal" && <CheckCircle size={16} className="icon-success" />}
                {t.status === "Abnormal" && <AlertTriangle size={16} className="icon-warn" />}
                {t.status === "Critical" && <AlertCircle size={16} className="icon-critical" />}
              </td>
              <td className="test-name">{t.name}</td>
              <td>{t.result}</td>
              <td>{t.ref}</td>
              <td><StatusBadge status={t.status} /></td>
              <td>{t.flag ? <span className="flag">{t.flag}</span> : null}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}