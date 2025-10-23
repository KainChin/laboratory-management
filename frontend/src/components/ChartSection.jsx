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
  // If no data provided, render a demo dataset so user can preview the chart layout.
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
  const dataToRender = hasRealData ? data : demoData;

  // Calculate max value for Y axis
  const maxValue = Math.max(
    ...dataToRender.flatMap((item) => [
      item.Completed || 0,
      item.Cancelled || 0,
      item.Pending || 0,
    ])
  );

  // summary counts (displayed in header)
  const totals = dataToRender.reduce(
    (acc, cur) => {
      acc.Completed += Number(cur.Completed || 0);
      acc.Cancelled += Number(cur.Cancelled || 0);
      acc.Pending += Number(cur.Pending || 0);
      return acc;
    },
    { Completed: 0, Cancelled: 0, Pending: 0 }
  );

  return (
    <div className="bg-white px-6 py-5 rounded-2xl shadow-lg border border-gray-200">
      <div className="flex items-start justify-between gap-4 mb-4">
        <div>
          <h3 className="text-xl font-semibold text-gray-800">
            Weekly Test Orders
          </h3>
          <p className="text-sm text-gray-500">
            Trend of Completed / Cancelled / Pending by week
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="text-center">
            <div className="text-sm text-gray-500">Completed</div>
            <div className="text-lg font-bold text-green-600">
              {totals.Completed}
            </div>
          </div>
          <div className="text-center">
            <div className="text-sm text-gray-500">Cancelled</div>
            <div className="text-lg font-bold text-red-600">
              {totals.Cancelled}
            </div>
          </div>
          <div className="text-center">
            <div className="text-sm text-gray-500">Pending</div>
            <div className="text-lg font-bold text-blue-600">
              {totals.Pending}
            </div>
          </div>
        </div>
      </div>

      {!hasRealData && (
        <div className="text-sm text-gray-500 italic mb-3 text-right">
          Demo data
        </div>
      )}

      <ResponsiveContainer width="100%" height={360}>
        <LineChart
          data={dataToRender}
          margin={{ top: 12, right: 24, left: 24, bottom: 8 }}
        >
          <defs>
            <linearGradient id="gradCompleted" x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stopColor="#dcfce7" stopOpacity={0.9} />
              <stop offset="100%" stopColor="#dcfce7" stopOpacity={0.0} />
            </linearGradient>
            <linearGradient id="gradCancelled" x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stopColor="#fee2e2" stopOpacity={0.9} />
              <stop offset="100%" stopColor="#fee2e2" stopOpacity={0.0} />
            </linearGradient>
            <linearGradient id="gradPending" x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stopColor="#dbeafe" stopOpacity={0.9} />
              <stop offset="100%" stopColor="#dbeafe" stopOpacity={0.0} />
            </linearGradient>
          </defs>

          <CartesianGrid stroke="#f3f4f6" vertical={false} />
          <XAxis
            dataKey="week"
            axisLine={false}
            tickLine={false}
            interval={dataToRender.length > 12 ? Math.ceil(dataToRender.length / 12) : 0}
            tickFormatter={(val) => (typeof val === "string" ? val : val)}
            tick={{ fill: "#374151", fontSize: 13 }}
            height={dataToRender.length > 8 ? 56 : 40}
            angle={dataToRender.length > 8 ? -35 : 0}
            textAnchor={dataToRender.length > 8 ? "end" : "middle"}
          />

          <YAxis
            axisLine={false}
            tickLine={false}
            tickCount={5}
            domain={[0, maxValue > 0 ? maxValue + Math.ceil(maxValue * 0.15) : 40]}
            tick={{ fill: "#374151", fontSize: 13 }}
          />

          <Tooltip
            cursor={{ stroke: "#eef2ff", strokeWidth: 2 }}
            contentStyle={{
              background: "#ffffff",
              border: "1px solid #e6eef8",
              borderRadius: 8,
              boxShadow: "0 6px 20px rgba(15, 23, 42, 0.08)",
            }}
            labelStyle={{ fontWeight: 700 }}
            formatter={(value) => [value, "Count"]}
          />

          <Legend
            verticalAlign="top"
            align="right"
            iconType="circle"
            wrapperStyle={{ top: -10, fontSize: 14 }}
          />

          <Line
            type="monotone"
            dataKey="Completed"
            stroke="#16a34a"
            strokeWidth={3}
            dot={{ r: 4, strokeWidth: 0, fill: "#16a34a" }}
            activeDot={{ r: 6 }}
            strokeLinecap="round"
          />

          <Line
            type="monotone"
            dataKey="Cancelled"
            stroke="#ef4444"
            strokeWidth={3}
            dot={{ r: 4, strokeWidth: 0, fill: "#ef4444" }}
            activeDot={{ r: 6 }}
            strokeLinecap="round"
          />

          <Line
            type="monotone"
            dataKey="Pending"
            stroke="#2563eb"
            strokeWidth={3}
            dot={{ r: 4, strokeWidth: 0, fill: "#2563eb" }}
            activeDot={{ r: 6 }}
            strokeLinecap="round"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
