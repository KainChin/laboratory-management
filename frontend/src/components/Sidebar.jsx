import React, { useState, useEffect } from "react";
import {
  Menu,
  Home,
  Users,
  FlaskConical,
  ClipboardList,
  Clock,
  BarChart2,
  ChevronUp,
} from "lucide-react";

export default function Sidebar() {
  const menu = [Home, Users, FlaskConical, ClipboardList, Clock, BarChart2];

  const [showTopBtn, setShowTopBtn] = useState(false);

  useEffect(() => {
    function onScroll() {
      setShowTopBtn(window.scrollY > 200); // hiện khi cuộn quá 200px, chỉnh nếu cần
    }
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  // ensure main content is not hidden behind the fixed sidebar
  useEffect(() => {
    const SIDEBAR_WIDTH = "4rem"; // w-16 = 4rem (64px)

    function applyPadding() {
      // only apply on wide screens (avoid mobile layout conflicts)
      if (window.innerWidth >= 640) {
        document.body.style.paddingLeft = SIDEBAR_WIDTH;
      } else {
        document.body.style.paddingLeft = "";
      }
    }

    applyPadding();
    window.addEventListener("resize", applyPadding);
    return () => {
      window.removeEventListener("resize", applyPadding);
      document.body.style.paddingLeft = "";
    };
  }, []);

  function scrollToTop() {
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  return (
    <>
      <aside className="fixed left-0 top-0 h-screen w-16 bg-red-500 text-white flex flex-col items-center py-4 space-y-6 z-40">
        <button className="p-2 bg-white/20 rounded-lg hover:bg-white/30 transition">
          <Menu size={20} />
        </button>
        <div className="flex flex-col space-y-6 mt-2">
          {menu.map((Icon, i) => (
            <button
              key={i}
              className="p-2 rounded-lg hover:bg-white/30 transition"
              aria-label={`menu-${i}`}
            >
              <Icon size={18} />
            </button>
          ))}
        </div>
      </aside>

      {/* nút mũi tên ở góc dưới phải (trắng) */}
      <button
        onClick={scrollToTop}
        aria-label="Back to top"
        className={
          "fixed bottom-6 right-6 z-50 bg-white rounded-full p-3 shadow-lg transition-opacity " +
          (showTopBtn ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none")
        }
      >
        <ChevronUp size={20} className="text-red-500" />
      </button>
    </>
  );
}
