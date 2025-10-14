import { Menu, Home, Users, FlaskConical, ClipboardList, Clock, BarChart2 } from "lucide-react";

export default function Sidebar() {
  const menu = [Home, Users, FlaskConical, ClipboardList, Clock, BarChart2];

  return (
    <aside className="w-16 bg-red-500 text-white flex flex-col items-center py-4 space-y-6">
      <button className="p-2 bg-white/20 rounded-lg hover:bg-white/30 transition">
        <Menu size={20} />
      </button>
      <div className="flex flex-col space-y-6 mt-2">
        {menu.map((Icon, i) => (
          <button key={i} className="p-2 rounded-lg hover:bg-white/30 transition">
            <Icon size={18} />
          </button>
        ))}
      </div>
    </aside>
  );
}
