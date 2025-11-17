import React from "react";
import { Link, useLocation } from "react-router-dom";
import { User, Settings, ChevronRight, HeartPulse } from "lucide-react";

export default function Header() {
  const location = useLocation();
  const segments = location.pathname.split("/").filter(Boolean);
  const isDetail = segments.includes("detail");

  return (
    <header className="flex justify-between items-center bg-white px-6 py-3 shadow-sm border-b border-gray-200">
      <div className="flex items-center space-x-2 text-gray-700">
        <HeartPulse className="text-red-500" size={24} />
        <span className="font-semibold">Laboratory Management</span>
        <ChevronRight className="text-red-400" size={24} />
        <Link to="/test-orders" className="text-red-500 font-medium">
          Test Orders
        </Link>
        {isDetail && (
          <>
            <ChevronRight className="text-red-400" size={24} />
            <span className="text-red-500 font-medium">Detail</span>
          </>
        )}
      </div>

      <div className="flex items-center space-x-3">
        <User size={24} className="text-red-400" />
        <span className="text-gray-700">
          Welcome, <b className="text-gray-900">[Administrator]</b>
        </span>
        <Settings
          size={24}
          className="text-red-400 cursor-pointer hover:rotate-90 transition-transform"
        />
      </div>
    </header>
  );
}
