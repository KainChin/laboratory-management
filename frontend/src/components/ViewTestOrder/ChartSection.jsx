import React, { useState, useEffect } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

export default function ChartSection({ activityTotals = null }) {
  const [weekData, setWeekData] = useState([]);
  const [weekLabel, setWeekLabel] = useState("");
  const [totals, setTotals] = useState({ Pending: 0, Completed: 0, Reviewed: 0 });

  // Fetch data for current week
  useEffect(() => {
    const fetchWeekData = async () => {
      try {
        const response = await fetch(`http://localhost:6868/api/test-orders/daily-statistics`);
        if (!response.ok) throw new Error("Failed to fetch daily statistics");
        
        const json = await response.json();
        
        // Handle the API response format
        if (json.statusCode !== 200 || !json.result) {
          throw new Error(json.message || "Failed to fetch statistics");
        }
        
        const data = json.result;
        
        // Process daily data
        const dailyData = data.dailyData || [];
        const processedData = dailyData.map(day => ({
          day: day.day,
          Pending: Number(day.pending || 0),
          Completed: Number(day.completed || 0),
          Reviewed: Number(day.reviewed || 0),
        }));
        
        setWeekData(processedData);
        setWeekLabel("Current Week");
        
        // Calculate totals from the weekly data only
        const calculatedTotals = processedData.reduce(
          (acc, cur) => ({
            Pending: acc.Pending + cur.Pending,
            Completed: acc.Completed + cur.Completed,
            Reviewed: acc.Reviewed + cur.Reviewed,
          }),
          { Pending: 0, Completed: 0, Reviewed: 0 }
        );
        
        setTotals(calculatedTotals);
      } catch (error) {
        console.error("Error fetching week data:", error);
        // Use demo data if fetch fails
        const demoData = [
          { day: "Mon", Pending: 8, Completed: 5, Reviewed: 3 },
          { day: "Tue", Pending: 6, Completed: 8, Reviewed: 5 },
          { day: "Wed", Pending: 7, Completed: 6, Reviewed: 4 },
          { day: "Thu", Pending: 4, Completed: 10, Reviewed: 7 },
          { day: "Fri", Pending: 5, Completed: 7, Reviewed: 6 },
          { day: "Sat", Pending: 3, Completed: 9, Reviewed: 8 },
          { day: "Sun", Pending: 4, Completed: 8, Reviewed: 6 },
        ];
        setWeekData(demoData);
        setWeekLabel("Current Week");
        
        const demoTotals = demoData.reduce(
          (acc, cur) => ({
            Pending: acc.Pending + cur.Pending,
            Completed: acc.Completed + cur.Completed,
            Reviewed: acc.Reviewed + cur.Reviewed,
          }),
          { Pending: 0, Completed: 0, Reviewed: 0 }
        );
        setTotals(demoTotals);
      }
    };

    fetchWeekData();
  }, [activityTotals]);

  const maxValue = Math.max(1, ...weekData.flatMap((r) => [
    (r.Pending || 0) + (r.Completed || 0) + (r.Reviewed || 0)
  ]));

  return (
    <div className="bg-white px-6 py-6 rounded-2xl shadow-lg border border-gray-200">
      <div className="flex items-start justify-between gap-6 mb-4">
        <div className="flex-1">
          <h3 className="text-2xl font-semibold text-gray-800">Weekly Test Orders</h3>
          <p className="text-sm text-gray-500">Daily breakdown of Pending / Completed / Reviewed orders ({weekLabel})</p>
        </div>

        <div className="flex items-start gap-8">
          <div className="text-right">
            <div className="text-sm text-gray-500">Pending</div>
            <div className="text-2xl font-bold text-blue-600">{totals.Pending}</div>
          </div>
          <div className="text-right">
            <div className="text-sm text-gray-500">Completed</div>
            <div className="text-2xl font-bold text-green-600">{totals.Completed}</div>
          </div>
          <div className="text-right">
            <div className="text-sm text-gray-500">Reviewed</div>
            <div className="text-2xl font-bold text-purple-600">{totals.Reviewed}</div>
          </div>
        </div>
      </div>

      <ResponsiveContainer width="100%" height={360}>
        <BarChart data={weekData} margin={{ top: 12, right: 24, left: 24, bottom: 8 }}>
          <CartesianGrid stroke="#f3f4f6" vertical={false} />
          <XAxis 
            dataKey="day" 
            axisLine={false} 
            tickLine={false} 
            tick={{ fill: "#777777", fontSize: 13 }} 
            height={48} 
          />
          <YAxis 
            axisLine={false} 
            tickLine={false} 
            tickCount={6} 
            domain={[0, Math.ceil(maxValue * 1.1)]} 
            tick={{ fill: "#777777", fontSize: 13 }} 
          />
          <Tooltip
            cursor={{ fill: "#f9fafb" }}
            contentStyle={{ 
              background: "#fff", 
              border: "1px solid #e6eef8", 
              borderRadius: 8, 
              boxShadow: "0 6px 20px rgba(15,23,42,0.08)" 
            }}
            labelStyle={{ fontWeight: 700, marginBottom: 8 }}
          />
          <Legend 
            verticalAlign="top" 
            align="right" 
            iconType="rect" 
            wrapperStyle={{ top: -10, fontSize: 14 }} 
          />

          <Bar dataKey="Pending" stackId="a" fill="#2563eb" radius={[0, 0, 0, 0]} />
          <Bar dataKey="Completed" stackId="a" fill="#16a34a" radius={[0, 0, 0, 0]} />
          <Bar dataKey="Reviewed" stackId="a" fill="#9333ea" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
