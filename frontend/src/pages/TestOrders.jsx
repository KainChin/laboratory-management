import InfoCard from "../components/ViewTestOrder/InfoCard";
import ChartSection from "../components/ViewTestOrder/ChartSection";
import ActivityCard from "../components/ViewTestOrder/ActivityCard";
import OrdersTable from "../components/ViewTestOrder/OrdersTable";
import DeleteConfirmationModal from "../components/ViewTestOrder/DeleteConfirmationModal";
import { BarChart3, Clock3, CheckCircle2, CheckCheck } from "lucide-react";
import React, { useState, useEffect, useRef } from "react";
import ExcelJS from "exceljs";
import { saveAs } from "file-saver";

export default function TestOrders() {
  const [orderStats, setOrderStats] = useState({
    total: 0,
    pending: 0,
    completed: 0,
    reviewed: 0,
    weeklyData: [],
  });

  // Ref để chụp biểu đồ
  const chartRef = useRef(null);

  useEffect(() => {
    let mounted = true;

    async function fetchStatistics() {
      try {
        const res = await fetch(
          "http://localhost:6868/api/test-orders/statistics"
        );
        if (!res.ok) throw new Error("Failed to fetch statistics");
        const json = await res.json();
        const stats = json.result || json.data || {};

        const total = Number(stats.total ?? stats.count ?? 0);
        const pending = Number(stats.pending ?? 0);
        const completed = Number(stats.completed ?? 0);
        const reviewed = Number(stats.reviewed ?? 0);

        let weeklyData = [];
        if (Array.isArray(stats.weeklyData) && stats.weeklyData.length) {
          weeklyData = stats.weeklyData.map((it) => ({
            week: it.week ?? it.weekLabel ?? it.label,
            Completed: Number(it.Completed ?? it.completed ?? 0),
            Reviewed: Number(it.Reviewed ?? it.reviewed ?? 0),
            Pending: Number(it.Pending ?? it.pending ?? 0),
          }));
        }

        if (mounted) {
          setOrderStats({ total, pending, completed, reviewed, weeklyData });
        }
      } catch (err) {
        console.error("Error fetching statistics:", err);
      }
    }

    fetchStatistics();
    return () => {
      mounted = false;
    };
  }, []);

  const summary = [
    {
      title: "Total",
      value: orderStats.total.toLocaleString(),
      color: "text-sky-600",
      icon: <BarChart3 className="text-sky-400" size={24} />,
      border: "border-sky-200",
    },
    {
      title: "Pending",
      value: orderStats.pending.toLocaleString(),
      color: "text-blue-600",
      icon: <Clock3 className="text-blue-400" size={24} />,
      border: "border-blue-200",
    },
    {
      title: "Completed",
      value: orderStats.completed.toLocaleString(),
      color: "text-green-600",
      icon: <CheckCircle2 className="text-green-400" size={24} />,
      border: "border-green-200",
    },
    {
      title: "Reviewed",
      value: orderStats.reviewed.toLocaleString(),
      color: "text-purple-600",
      icon: <CheckCheck className="text-purple-400" size={24} />,
      border: "border-purple-200",
    },
  ];

  // 🎯 Excel export with ExcelJS
  const handleExportExcel = async () => {
    try {
      const queryParams = new URLSearchParams({
        page: "0",
        size: "1000",
        sortDir: "desc",
      });

      const response = await fetch(
        `http://localhost:6868/api/test-orders?${queryParams}`
      );
      if (!response.ok)
        throw new Error(`HTTP error! status: ${response.status}`);

      const result = await response.json();
      const orders = result.result?.items || [];

      if (orders.length === 0) {
        alert("No data available to export");
        return;
      }

      const workbook = new ExcelJS.Workbook();
      const sheet = workbook.addWorksheet("Test Orders");

      // Columns setup
      sheet.columns = [
        { header: "Order ID", key: "testOrderId", width: 15 },
        { header: "Patient Name", key: "patientName", width: 25 },
        { header: "Date of Birth", key: "dob", width: 15 },
        { header: "Phone", key: "phone", width: 15 },
        { header: "Email", key: "email", width: 25 },
        { header: "Gender", key: "gender", width: 10 },
        { header: "Status", key: "status", width: 15 },
        { header: "Address", key: "address", width: 25 },
        { header: "Country", key: "country", width: 15 },
        { header: "Citizen ID", key: "citizenId", width: 15 },
        { header: "Created By", key: "createdBy", width: 20 },
      ];

      // Header styling
      sheet.getRow(1).eachCell((cell) => {
        cell.font = { bold: true, color: { argb: "FFFFFFFF" } };
        cell.fill = {
          type: "pattern",
          pattern: "solid",
          fgColor: { argb: "FFFF5A5A" },
        };
        cell.alignment = { horizontal: "center", vertical: "middle" };
        cell.border = {
          top: { style: "thin", color: { argb: "FFAAAAAA" } },
          left: { style: "thin", color: { argb: "FFAAAAAA" } },
          bottom: { style: "thin", color: { argb: "FFAAAAAA" } },
          right: { style: "thin", color: { argb: "FFAAAAAA" } },
        };
      });

      // Add data with color by status
      orders.forEach((o) => {
        const row = sheet.addRow({
          testOrderId: o.testOrderId,
          patientName: o.patientName,
          dob: o.dob || o.dateOfBirth,
          phone: o.phone,
          email: o.email,
          gender: o.gender,
          status: o.status,
          address: o.address,
          country: o.country,
          citizenId: o.citizenId,
          createdBy: o.createdBy || o.creator,
        });

        const statusCell = row.getCell("status");
        const status = (o.status || "").toLowerCase();
        let color = "FFDDEBF7"; // pending-blue

        if (status.includes("comp")) color = "FFD4EDDA"; // green
        else if (status.includes("cancel")) color = "FFF8D7DA"; // red

        statusCell.fill = {
          type: "pattern",
          pattern: "solid",
          fgColor: { argb: color },
        };
        row.alignment = { vertical: "middle" };
        row.border = {
          bottom: { style: "hair", color: { argb: "FFD9D9D9" } },
        };
      });

      // Optional: Add chart sheet (image)
      const chartSheet = workbook.addWorksheet("Statistics");
      try {
        const chartCanvas = chartRef.current?.querySelector("canvas");
        if (chartCanvas) {
          const dataUrl = chartCanvas.toDataURL("image/png");
          const imageId = workbook.addImage({
            base64: dataUrl,
            extension: "png",
          });
          chartSheet.addImage(imageId, {
            tl: { col: 1, row: 1 },
            ext: { width: 600, height: 350 },
          });
        }
      } catch (err) {
        console.warn("Chart image not available:", err);
      }

      const buffer = await workbook.xlsx.writeBuffer();
      saveAs(
        new Blob([buffer]),
        `TestOrders_${new Date().toISOString().split("T")[0]}.xlsx`
      );
    } catch (error) {
      console.error("Export failed:", error);
      alert(`Export failed: ${error.message}`);
    }
  };

  return (
    <div className="space-y-12 max-w-[1920px] mx-auto px-6">
      {/* HEADER */}
      <div className="space-y-4">
        <div className="grid grid-cols-[1fr_auto_1fr] items-center">
          <div />
          <h1 className="justify-self-center text-[24px] leading-none font-extrabold tracking-[0.35em] uppercase text-[#FF5A5A]">
            TEST ORDERS
          </h1>
          <div className="justify-self-end flex gap-4">
            <button
              onClick={handleExportExcel}
              className="bg-[#FF5A5A] text-white px-3 min-h-[40px] py-2 rounded-lg hover:bg-[#FF3A3A] transition-colors duration-300"
            >
              Export Excel
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
      <div ref={chartRef}>
        <ChartSection
          activityTotals={{
            Completed: orderStats.completed,
            Reviewed: orderStats.reviewed,
            Pending: orderStats.pending,
          }}
        />
      </div>

      {/* ACTIVITY */}
      <ActivityCard />

      {/* TABLE */}
      <OrdersTable />
    </div>
  );
}
