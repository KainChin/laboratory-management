import { User, Settings, ChevronRight, HeartPulse } from "lucide-react";

export default function Header() {
  return (
    <header className="flex justify-between items-center bg-white px-6 py-3 shadow-sm border-b border-gray-200">
      <div className="flex items-center space-x-2 text-gray-700">
        <HeartPulse className="text-red-500" size={20} />
        <span className="font-semibold">Laboratory Management</span>
        <ChevronRight className="text-red-400" size={16} />
        <span className="text-red-500 font-medium">Test Orders</span>
      </div>
      <div className="flex items-center space-x-3">
        <User size={18} className="text-red-400" />
        <span className="text-gray-700">
          Welcome, <b className="text-gray-900">[Administrator]</b>
        </span>
        <Settings size={20} className="text-red-400 cursor-pointer hover:rotate-90 transition-transform" />
      </div>
    </header>
  );
}
