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

export default function ChartSection() {
  const data = [
    { week: "Week 01", Completed: 0,  Cancelled: 12, Pending: 18 },
    { week: "Week 02", Completed: 12, Cancelled: 4,  Pending: 30 },
    { week: "Week 03", Completed: 38, Cancelled: 20, Pending: 25 },
    { week: "Week 04", Completed: 30, Cancelled: 15, Pending: 40 },
  ];

  return (
    <div className="bg-white px-8 py-6 rounded-xl shadow-sm border border-gray-200">
      <ResponsiveContainer width="100%" height={360}>
        <LineChart data={data} margin={{ top: 36, right: 30, left: 40, bottom: 10 }}>
          <CartesianGrid stroke="#e5e7eb" vertical={false} />
          <XAxis
            dataKey="week"
            axisLine={false}
            tickLine={false}
            interval={0}
            tick={{ fill: "#111827", fontSize: 16 }}
          />
          <YAxis
            axisLine={false}
            tickLine={false}
            tickCount={5}
            domain={[0, 40]}
            tick={{ fill: "#111827", fontSize: 16 }}
          />
          <Tooltip
            cursor={{ stroke: "#f3f4f6", strokeWidth: 2 }}
            contentStyle={{
              background: "#fff",
              border: "1px solid #e5e7eb",
              borderRadius: 8,
              boxShadow: "0 4px 10px rgba(0,0,0,0.04)",
            }}
            labelStyle={{ fontWeight: 700 }}
          />
          <Legend
            verticalAlign="top"
            align="center"
            iconType="circle"
            wrapperStyle={{ top: 0, fontSize: 18, marginBottom: 16 }}
          />
          <Line type="linear" dataKey="Completed" stroke="#16a34a" strokeWidth={2}
                dot={{ r: 5, strokeWidth: 0, fill: "#16a34a" }} activeDot={{ r: 6 }} />
          <Line type="linear" dataKey="Cancelled" stroke="#2563eb" strokeWidth={2}
                dot={{ r: 5, strokeWidth: 0, fill: "#2563eb" }} activeDot={{ r: 6 }} />
          <Line type="linear" dataKey="Pending" stroke="#ef4444" strokeWidth={2}
                dot={{ r: 5, strokeWidth: 0, fill: "#ef4444" }} activeDot={{ r: 6 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
