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

export default function ChartSection({ data = [] }) {
  // If no data provided, show empty state
  if (data.length === 0) {
    data = [
      { week: "Week 01", Completed: 0, Cancelled: 0, Pending: 0 }
    ];
  }

  // Calculate max value for Y axis
  const maxValue = Math.max(
    ...data.flatMap(item => [
      item.Completed || 0,
      item.Cancelled || 0,
      item.Pending || 0
    ])
  );

  return (
    <div className="bg-white px-8 py-6 rounded-xl shadow-sm border border-gray-200">
      <ResponsiveContainer width="100%" height={400}>
                <LineChart data={data} margin={{ top: 36, right: 35, left: 40, bottom: 15 }}>
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
            domain={[0, maxValue > 0 ? maxValue + 5 : 40]}
            tick={{ fill: "#111827", fontSize: 14 }}
          />
          <Tooltip
            cursor={{ stroke: "#f3f4f6", strokeWidth: 2 }}
            contentStyle={{
              background: "#fff",
              border: "1px solid #e5e7eb",
              borderRadius: 8,
              boxShadow: "0 4px 10px rgba(0,0,0,0.04)",
            }}
            labelStyle={{ fontWeight: 600 }}
          />
          <Legend
            verticalAlign="top"
            align="center"
            iconType="circle"
            wrapperStyle={{ top: 0, fontSize: 16, marginBottom: 24 }}
          />
          <Line type="linear" dataKey="Completed" stroke="#16a34a" strokeWidth={2}
                dot={{ r: 5, strokeWidth: 0, fill: "#16a34a" }} activeDot={{ r: 6 }} />
          <Line type="linear" dataKey="Cancelled" stroke="#ef4444" strokeWidth={2}
                dot={{ r: 5, strokeWidth: 0, fill: "#ef4444" }} activeDot={{ r: 6 }} />
          <Line type="linear" dataKey="Pending" stroke="#2563eb" strokeWidth={2}
                dot={{ r: 5, strokeWidth: 0, fill: "#2563eb" }} activeDot={{ r: 6 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
