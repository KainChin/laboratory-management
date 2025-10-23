import React from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

export default function ChartSection({ data = [], activityTotals = null }) {
  // chuẩn hoá label week: "Week 01" -> "W01"
  const formatWeek = (w) => {
    if (!w) return "";
    const s = String(w);
    const m = s.match(/(\d{1,2})/);
    return m ? `W${String(m[1]).padStart(2, "0")}` : s;
  };

  const hasRealData = Array.isArray(data) && data.length > 0;
  const demoData = [
    { week: "W01", Completed: 5, Cancelled: 1, Pending: 8 },
    { week: "W02", Completed: 8, Cancelled: 0, Pending: 6 },
    { week: "W03", Completed: 6, Cancelled: 2, Pending: 7 },
    { week: "W04", Completed: 10, Cancelled: 1, Pending: 4 },
    { week: "W05", Completed: 7, Cancelled: 0, Pending: 5 },
    { week: "W06", Completed: 9, Cancelled: 1, Pending: 3 },
    { week: "W07", Completed: 11, Cancelled: 0, Pending: 2 },
    { week: "W08", Completed: 8, Cancelled: 1, Pending: 4 },
  ];

  const dataToRender = (hasRealData ? data : demoData).map((it) => ({
    ...it,
    week: formatWeek(it.week),
    Completed: Number(it.Completed || 0),
    Cancelled: Number(it.Cancelled || 0),
    Pending: Number(it.Pending || 0),
  }));

  const totalsFromData = dataToRender.reduce(
    (acc, cur) => {
      acc.Completed += cur.Completed;
      acc.Cancelled += cur.Cancelled;
      acc.Pending += cur.Pending;
      return acc;
    },
    { Completed: 0, Cancelled: 0, Pending: 0 }
  );

  const totals = activityTotals || totalsFromData;
  const maxValue = Math.max(1, ...dataToRender.flatMap((r) => [r.Completed, r.Cancelled, r.Pending]));

  return (
    <div className="bg-white px-6 py-6 rounded-2xl shadow-lg border border-gray-200">
      <div className="flex items-start justify-between gap-6 mb-3">
        <div>
          <h3 className="text-2xl font-semibold text-gray-800">Weekly Test Orders</h3>
          <p className="text-sm text-gray-500">Trend of Completed / Cancelled / Pending by week</p>
        </div>

        <div className="flex items-start gap-8">
          <div className="text-right">
            <div className="text-sm text-gray-500">Completed</div>
            <div className="text-2xl font-bold text-green-600">{totals.Completed}</div>
          </div>
          <div className="text-right">
            <div className="text-sm text-gray-500">Cancelled</div>
            <div className="text-2xl font-bold text-red-600">{totals.Cancelled}</div>
          </div>
          <div className="text-right">
            <div className="text-sm text-gray-500">Pending</div>
            <div className="text-2xl font-bold text-blue-600">{totals.Pending}</div>
          </div>
        </div>
      </div>

      {!hasRealData && (
        <div className="text-sm text-gray-400 italic mb-2 text-right">Demo data</div>
      )}

      <ResponsiveContainer width="100%" height={360}>
        <LineChart data={dataToRender} margin={{ top: 12, right: 24, left: 24, bottom: 8 }}>
          <CartesianGrid stroke="#f3f4f6" vertical={false} />
          <XAxis dataKey="week" axisLine={false} tickLine={false} tick={{ fill: "#374151", fontSize: 13 }} height={48} />
          <YAxis axisLine={false} tickLine={false} tickCount={5} domain={[0, Math.ceil(maxValue * 1.15)]} tick={{ fill: "#374151", fontSize: 13 }} />
          <Tooltip
            cursor={{ stroke: "#eef2ff", strokeWidth: 2 }}
            contentStyle={{ background: "#fff", border: "1px solid #e6eef8", borderRadius: 8, boxShadow: "0 6px 20px rgba(15,23,42,0.08)" }}
            labelStyle={{ fontWeight: 700 }}
            formatter={(v) => [v, "Count"]}
          />
          <Legend verticalAlign="top" align="right" iconType="circle" wrapperStyle={{ top: -10, fontSize: 14 }} />

          <Line type="monotone" dataKey="Cancelled" stroke="#ef4444" strokeWidth={3} dot={{ r: 4, fill: "#ef4444" }} activeDot={{ r: 6 }} />
          <Line type="monotone" dataKey="Completed" stroke="#16a34a" strokeWidth={3} dot={{ r: 4, fill: "#16a34a" }} activeDot={{ r: 6 }} />
          <Line type="monotone" dataKey="Pending" stroke="#2563eb" strokeWidth={3} dot={{ r: 4, fill: "#2563eb" }} activeDot={{ r: 6 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
