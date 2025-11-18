import React, { useEffect, useState } from "react";
import { useLocation, Link } from "react-router-dom";
import { motion } from "framer-motion";
import { FaBars } from "react-icons/fa";
import {
  FaHome,
  FaUsers,
  FaUserCog,
  FaUserCheck,
} from "react-icons/fa";

// Inline component
export function SidebarIcon({ icon, active, isSideBarOpen }) {
  return (
    <div
      className={`w-10 h-10 rounded-[5px] flex justify-center items-center
      cursor-pointer transition-all duration-300 ease-in-out
      ${active ? "bg-[#FFFFFF33]" : "bg-transparent"}
      ${!isSideBarOpen && "hover:bg-[#FFFFFF33]"}`}
    >
      {icon}
    </div>
  );
}

export default function Sidebar() {
  const location = useLocation();
  const [isSideBarOpen, setIsSideBarOpen] = useState(() => {
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme) {
      const parsed = JSON.parse(savedTheme);
      return parsed.isSideBarOpen ?? false;
    }
    return false;
  });

  const handleOpenSideBar = () => {
    setIsSideBarOpen(!isSideBarOpen);

    const theme = { isSideBarOpen: !isSideBarOpen };
    localStorage.setItem("theme", JSON.stringify(theme));
  };

  const visibleMenuItems = [
    { path: "/", icon: FaHome, privilege: "READ_ONLY", desc: "Home" },
    { path: "/test-orders", icon: FaUsers, privilege: "VIEW_ROLE", desc: "Test Orders" },
    { path: "#", icon: FaUserCog, privilege: "VIEW_USER", desc: "User management" },
    { path: "#", icon: FaUserCheck, privilege: "VIEW_USER", desc: "Account management" },
  ];

  // disable scroll on tablet and mobile viewport
  useEffect(() => {
    const handleScrollLock = () => {
      const isMobile = window.innerWidth <= 780;
      if (isSideBarOpen && isMobile) {
        document.body.style.overflowY = "hidden";
      } else {
        document.body.style.overflowY = "auto";
      }
    };

    handleScrollLock(); // apply on mount or change
    window.addEventListener("resize", handleScrollLock); // update on resize
    return () => {
      document.body.style.overflowY = "auto";
      window.removeEventListener("resize", handleScrollLock);
    };
  }, [isSideBarOpen]);

  return (
    <div
      className={`bg-[#FF5A5A] text-white z-[100] transition-all duration-200 ease-in-out
        ${isSideBarOpen ? "md:w-[200px]" : "w-[60px]"}`}
    >
      <div
        className={`p-[6px] border-b border-white/20 w-full h-[60px] bg-[#FF5A5A] `}
      >
        <motion.div
          className={`p-2 ml-[2px] w-max rounded-[5px] hover:cursor-pointer hover:scale-110 transition-all duration-200
                      ${location.pathname === "/" && "bg-[#FFFFFF33]"}`}
        >
          <FaBars
            onClick={handleOpenSideBar}
            className="text-[24px] md:text-white text-black"
          />
        </motion.div>
      </div>

      <div className={`pt-5 bg-[#FF5A5A] w-full md:opacity-100
                                          ${isSideBarOpen ? "opacity-100" : "opacity-0"}`}>
        {
          visibleMenuItems.map((item, index) => (
            <Link
              to={item.path}
              key={index}
              className={`flex items-center w-full px-2 mb-5 transition-all duration-200 ease-in-out
                                                   hover:cursor-pointer hover:scale-105
                                                   ${isSideBarOpen ? "hover:bg-white/20 rounded-r-full" : "bg-transparent hover:bg-transparent"}
                                                   ${isSideBarOpen && location.pathname === item.path && "bg-[#FFFFFF33]"}`}
            >
              <div>
                <SidebarIcon
                  icon={<item.icon size={24} />}
                  active={!isSideBarOpen && location.pathname === item.path}
                  isSideBarOpen={isSideBarOpen}
                />
              </div>
              {isSideBarOpen && (
                <span className="pt-1 whitespace-nowrap text-[14px]
                       transition-all duration-300 ease-in-out hover:cursor-pointer">
                  {item.desc}
                </span>
              )}
            </Link>
          ))
        }
      </div>
    </div>
  );
}
