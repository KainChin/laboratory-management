import InfoCard from "../components/InfoCard";
import ChartSection from "../components/ChartSection";
import ActivityCard from "../components/ActivityCard";
import OrdersTable from "../components/OrdersTable";
import { BarChart3, Clock3, CheckCircle2, XCircle } from "lucide-react";
import React, { useState, useEffect } from "react";

export default function TestOrders() {
  const [orderStats, setOrderStats] = useState({
    total: 0,
    pending: 0,
    completed: 0,
    cancelled: 0,
    weeklyData: [],
  });

  useEffect(() => {
    let mounted = true;

    async function fetchStatistics() {
      try {
        const res = await fetch("http://localhost:6868/api/test-orders/statistics");
        if (!res.ok) throw new Error("Failed to fetch statistics");
        const json = await res.json();
        const stats = json.result || json.data || {};

        // totals (tolerant với nhiều tên trường)
        const total = Number(stats.total ?? stats.count ?? 0);
        const pending = Number(stats.pending ?? 0);
        const completed = Number(stats.completed ?? 0);
        const cancelled = Number(stats.cancelled ?? 0);

        // weeklyData: nếu API trả weeklyData dùng thẳng, nếu không => build từ orders
        let weeklyData = [];
        if (Array.isArray(stats.weeklyData) && stats.weeklyData.length) {
          weeklyData = stats.weeklyData.map((it) => ({
            week: it.week ?? it.weekLabel ?? it.label,
            Completed: Number(it.Completed ?? it.completed ?? 0),
            Cancelled: Number(it.Cancelled ?? it.cancelled ?? 0),
            Pending: Number(it.Pending ?? it.pending ?? 0),
          }));
        } else {
          // tìm mảng orders trong nhiều vị trí khả dĩ
          const rawOrders =
            Array.isArray(stats.orders) ? stats.orders :
            Array.isArray(json.orders) ? json.orders :
            Array.isArray(stats.data) ? stats.data :
            [];
          weeklyData = processWeeklyStats(rawOrders);
        }

        if (mounted) {
          setOrderStats((prev) => ({
            ...prev,
            total,
            pending,
            completed,
            cancelled,
            weeklyData,
          }));
        }
      } catch (err) {
        console.error("Error fetching statistics:", err);
      }
    }

    function processWeeklyStats(orders) {
      if (!Array.isArray(orders) || orders.length === 0) return [];

      const weekMap = orders.reduce((acc, order) => {
        // lấy ngày từ các tên trường thường gặp
        const dateStr = order.date || order.orderDate || order.createdAt || order.created_at || order.dateOfBirth;
        const d = parseDate(dateStr);
        if (!d) return acc;

        const weekNo = getWeekNumber(d);
        const weekKey = `W${String(weekNo).padStart(2, "0")}`;

        if (!acc[weekKey]) acc[weekKey] = { Completed: 0, Cancelled: 0, Pending: 0 };

        const status = (order.status || order.state || "PENDING").toString().toUpperCase();
        if (status.startsWith("COMP")) acc[weekKey].Completed += 1;
        else if (status.startsWith("CANCEL")) acc[weekKey].Cancelled += 1;
        else acc[weekKey].Pending += 1;

        return acc;
      }, {});

      return Object.entries(weekMap)
        .map(([week, stats]) => ({ week, ...stats }))
        .sort((a, b) => a.week.localeCompare(b.week));
    }

    function parseDate(s) {
      if (!s) return null;
      // ISO first
      const iso = new Date(s);
      if (!isNaN(iso.getTime())) return iso;
      // dd/MM/yyyy
      const parts = s.split("/");
      if (parts.length === 3) {
        const [d, m, y] = parts;
        const year = Number(y.length === 2 ? `20${y}` : y);
        const date = new Date(year, Number(m) - 1, Number(d));
        if (!isNaN(date.getTime())) return date;
      }
      return null;
    }

    function getWeekNumber(d) {
      const date = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
      const dayNum = date.getUTCDay() || 7;
      date.setUTCDate(date.getUTCDate() + 4 - dayNum);
      const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
      return Math.ceil((((date - yearStart) / 86400000) + 1) / 7);
    }

    fetchStatistics();
    return () => { mounted = false; };
  }, []);

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
      <ChartSection data={orderStats.weeklyData} activityTotals={{ Completed: orderStats.completed, Cancelled: orderStats.cancelled, Pending: orderStats.pending }} />

      {/* ACTIVITY */}
      <ActivityCard />

      {/* TABLE */}
      <OrdersTable />
    </div>
  );
}
