import InfoCard from "../components/InfoCard";
import ChartSection from "../components/ChartSection";
import ActivityCard from "../components/ActivityCard";
import OrdersTable from "../components/OrdersTable";
import { BarChart3, Clock3, CheckCircle2, XCircle } from "lucide-react";
import { useState, useEffect } from "react";

export default function TestOrders() {
  // State for order stats
  const [orderStats, setOrderStats] = useState({
    total: 0,
    pending: 0,
    completed: 0,
    cancelled: 0,
    weeklyData: []
  });

  useEffect(() => {
    async function fetchStatistics() {
      try {
        const res = await fetch("http://localhost:6868/api/test-orders/statistics");
        if (!res.ok) throw new Error("Failed to fetch statistics");
        const data = await res.json();
        console.log("Statistics API Response:", data);
        
        const stats = data.result || {};
        
        setOrderStats({
          total: stats.total || 0,
          pending: stats.pending || 0,
          completed: stats.completed || 0,
          cancelled: stats.cancelled || 0,
          weeklyData: orderStats.weeklyData // Preserve existing weekly data
        });
      } catch (err) {
        console.error("Error fetching statistics:", err);
      }
    }

    function processWeeklyStats(orders) {
      if (!orders.length) return [];

      // Group orders by week
      const weekMap = orders.reduce((acc, order) => {
        try {
          // Convert dd/MM/yyyy to Date object
          const [day, month, year] = (order.dateOfBirth || "").split("/");
          if (!day || !month || !year) {
            console.warn("Invalid date format:", order.dateOfBirth);
            return acc;
          }

          const date = new Date(year, month - 1, day);
          if (isNaN(date.getTime())) {
            console.warn("Invalid date:", order.dateOfBirth);
            return acc;
          }

          const weekNum = getWeekNumber(date);
          const weekKey = `Week ${String(weekNum).padStart(2, "0")}`;
          
          if (!acc[weekKey]) {
            acc[weekKey] = { Completed: 0, Cancelled: 0, Pending: 0 };
          }
          
          const status = (order.status || "PENDING").toUpperCase();
          if (status === "COMPLETED") acc[weekKey].Completed += 1;
          else if (status === "CANCELLED") acc[weekKey].Cancelled += 1;
          else acc[weekKey].Pending += 1;
          
          return acc;
        } catch (err) {
          console.warn("Error processing order:", order, err);
          return acc;
        }
      }, {});

      // Convert to array format for chart, sort by week
      return Object.entries(weekMap)
        .map(([week, stats]) => ({
          week,
          ...stats
        }))
        .sort((a, b) => {
          const weekA = parseInt(a.week.split(" ")[1]);
          const weekB = parseInt(b.week.split(" ")[1]);
          return weekA - weekB;
        });
    }

    function getWeekNumber(d) {
      d = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
      d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay()||7));
      const yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));
      const weekNo = Math.ceil(( ( (d - yearStart) / 86400000) + 1)/7);
      return weekNo;
    }

    fetchStatistics();
  }, [orderStats.weeklyData]);

  const summary = [
    {
      title: "Total",
      value: orderStats.total.toLocaleString(),
      color: "text-sky-600",
      icon: <BarChart3 className="text-sky-400" size={18} />,
      border: "border-sky-200",
    },
    {
      title: "Pending",
      value: orderStats.pending.toLocaleString(),
      color: "text-blue-600",
      icon: <Clock3 className="text-blue-400" size={18} />,
      border: "border-blue-200",
    },
    {
      title: "Completed",
      value: orderStats.completed.toLocaleString(),
      color: "text-green-600",
      icon: <CheckCircle2 className="text-green-400" size={18} />,
      border: "border-green-200",
    },
    {
      title: "Cancelled",
      value: orderStats.cancelled.toLocaleString(),
      color: "text-red-600",
      icon: <XCircle className="text-red-400" size={18} />,
      border: "border-red-200",
    },
  ];

  return (
    <div className="space-y-12 max-w-[1920px] mx-auto px-6">
      {/* HEADER: Title center + buttons right */}
      <div className="space-y-4">
        <div className="grid grid-cols-[1fr_auto_1fr] items-center">
          <div />
          <h1 className="justify-self-center text-[28px] leading-none font-extrabold tracking-[0.35em] uppercase text-[#f65f63]">
            TEST ORDERS
          </h1>
          <div className="justify-self-end flex gap-4">
            <button className="bg-[#f65f63]/90 hover:bg-[#f65f63] text-white font-semibold px-5 py-2 rounded-lg transition-shadow shadow-sm hover:shadow">
              Export Excel
            </button>
            <button className="bg-[#f65f63] hover:bg-[#e74f53] text-white font-semibold px-5 py-2 rounded-lg transition-shadow shadow-sm hover:shadow">
              Print Report
            </button>
          </div>
        </div>
        <p className="text-center italic text-[20px] text-gray-700">
          Manage patient test orders and view laboratory results
        </p>
      </div>

      {/* SUMMARY CARDS */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {summary.map((item) => (
          <InfoCard key={item.title} {...item} />
        ))}
      </div>

      {/* CHART */}
      <ChartSection data={orderStats.weeklyData} />

      {/* ACTIVITY */}
      <ActivityCard />

      {/* TABLE */}
      <OrdersTable />
    </div>
  );
}
