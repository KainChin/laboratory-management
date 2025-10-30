import React from "react";

function _mapStatusLabel(status, flag) {
  const s = (status || "").toString().toUpperCase();
  const f = (flag || "").toString().toUpperCase();

  if (f.includes("HIGH") || f.includes("LOW") || f.includes("ABNORMAL")) return { label: "Critical", classes: "bg-rose-100 text-rose-700" };
  if (s === "PENDING") return { label: "Pending", classes: "bg-blue-100 text-blue-700" };
  if (s === "VALIDATED") return { label: "Abnormal", classes: "bg-amber-100 text-amber-700" };
  if (s === "APPROVED") return { label: "Normal", classes: "bg-emerald-100 text-emerald-700" };
  return { label: s || "-", classes: "bg-gray-100 text-gray-800" };
}

function _flagPill(flag) {
  const f = (flag || "").toString().toUpperCase();
  if (!f) return { label: "-", classes: "bg-gray-100 text-gray-800" };
  return { label: f, classes: "bg-amber-100 text-amber-800" };
}

export default function TestResults({ results = [] }) {
  return (
    <section className="bg-white rounded-lg border p-6 shadow-sm">
      <div className="flex items-center mb-4">
        <div className="p-2 rounded-full bg-rose-50 mr-3">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
            <circle cx="12" cy="12" r="10" stroke="#f65f63" strokeWidth="1.5" />
          </svg>
        </div>
        <h3 className="text-xl font-semibold text-rose-600">Test Result</h3>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-gray-500 text-left border-y">
              <th className="py-4 px-4">Test Name</th>
              <th className="py-4 px-4">Result</th>
              <th className="py-4 px-4">Reference Range</th>
              <th className="py-4 px-4">Status</th>
              <th className="py-4 px-4">Flag</th>
              <th className="py-4 px-4">Meta</th>
            </tr>
          </thead>

          <tbody>
            {results.length === 0 ? (
              <tr>
                <td colSpan="6" className="py-6 text-center text-gray-400">No test results</td>
              </tr>
            ) : (
              results.map((r) => {
                const statusBadge = _mapStatusLabel(r.status, r.flag);
                const flagBadge = _flagPill(r.flag);

                const valueText = (r.value !== undefined || r.value === 0)
                  ? (r.unit ? `${r.value} ${r.unit}` : String(r.value))
                  : "-";

                const refText = (r.referenceMin !== undefined && r.referenceMax !== undefined)
                  ? `${r.referenceMin} - ${r.referenceMax}`
                  : (r.referenceMin ?? r.referenceMax ?? "-");

                return (
                  <tr key={r.resultId} className="border-t last:border-b">
                    <td className="py-4 px-4 align-top">
                      <div className="text-gray-700 font-medium">{r.parameter ?? "-"}</div>
                    </td>

                    <td className="py-4 px-4 align-top text-gray-700 font-medium">{valueText}</td>

                    <td className="py-4 px-4 align-top text-gray-500">{refText}</td>

                    <td className="py-4 px-4 align-top">
                      <span className={`${statusBadge.classes} inline-block px-4 py-1 rounded-full text-sm font-semibold`}>
                        {statusBadge.label}
                      </span>
                    </td>

                    <td className="py-4 px-4 align-top">
                      <span className={`${flagBadge.classes} inline-block px-3 py-1 rounded-full text-sm font-medium`}>
                        {flagBadge.label}
                      </span>
                    </td>

                    <td className="py-4 px-4 align-top text-xs text-gray-500">
                      <div>resultId: {r.resultId}</div>
                      <div>status: {r.status ?? "-"}</div>
                      <div>createdBy: {r.createdBy ?? "-"}</div>
                      <div>createdAt: {r.createdAt ?? "-"}</div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}