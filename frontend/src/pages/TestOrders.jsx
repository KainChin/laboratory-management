import InfoCard from "../components/InfoCard";
import ChartSection from "../components/ChartSection";
import ActivityCard from "../components/ActivityCard";
import OrdersTable from "../components/OrdersTable";
import { BarChart3, Clock3, CheckCircle2, XCircle } from "lucide-react";

export default function TestOrders() {
  const summary = [
    {
      title: "Total",
      value: "2,847",
      color: "text-sky-600",
      icon: <BarChart3 className="text-sky-400" size={18} />,
      border: "border-sky-200",
    },
    {
      title: "Pending",
      value: "156",
      color: "text-blue-600",
      icon: <Clock3 className="text-blue-400" size={18} />,
      border: "border-blue-200",
    },
    {
      title: "Completed",
      value: "2,530",
      color: "text-green-600",
      icon: <CheckCircle2 className="text-green-400" size={18} />,
      border: "border-green-200",
    },
    {
      title: "Cancelled",
      value: "168",
      color: "text-red-600",
      icon: <XCircle className="text-red-400" size={18} />,
      border: "border-red-200",
    },
  ];

  return (
    <div className="space-y-10">
      {/* HEADER: Title center + buttons right */}
      <div className="space-y-3">
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
      <ChartSection />

      {/* ACTIVITY */}
      <ActivityCard />

      {/* TABLE */}
      <OrdersTable />
    </div>
  );
}
