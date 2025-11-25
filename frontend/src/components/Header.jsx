import React, { useState, useEffect } from "react";
import { ChevronsRight, Menu, Bell, Users, LogOut } from "lucide-react";
import { FaHeartbeat } from "react-icons/fa";
import { useLocation, useNavigate } from "react-router-dom";
import { getUserName } from "../utils/jwtUtils";

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const [userName, setUserName] = useState("User");

  // Get username from JWT token in localStorage
  useEffect(() => {
    const name = getUserName();
    setUserName(name);
  }, []);

  // Determine breadcrumb based on current path
  const getBreadcrumb = () => {
    const path = location.pathname;
    
    if (path.startsWith("/test-orders/detail/")) {
      return [
        { label: "HOMEPAGE", path: "/" },
        { label: "TEST ORDER", path: "/test-orders" },
        { label: "DETAIL", path: null } // Current page, not clickable
      ];
    } else if (path === "/test-orders") {
      return [
        { label: "HOMEPAGE", path: "/" },
        { label: "TEST ORDER", path: null } // Current page, not clickable
      ];
    } else {
      return [
        { label: "HOMEPAGE", path: null } // Current page, not clickable
      ];
    }
  };

  const breadcrumbs = getBreadcrumb();

  return (
    <header style={{ backgroundColor: "#FFFFFF" }} className="h-[60px] bg-card border-b border-border flex items-center justify-between px-4">
      {/* Left section */}
      <div className="flex items-center gap-3">
        <button
          className="lg:hidden p-2 hover:bg-muted rounded-md transition-colors"
          aria-label="Toggle menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className=" items-center hidden md:flex">
          <div style={{ display: "flex", alignItems: "center" }}>
            <FaHeartbeat
              style={{
                color: "#FF5A5A",
                fontSize: "24px",
                marginRight: "10px",
              }}
            />
            <span
              style={{ color: "black", fontWeight: "bold", fontSize: "16px" }}
            >
              Laboratory Management
            </span>
          </div>
          <>
            {breadcrumbs.map((crumb, index) => (
              <React.Fragment key={index}>
                <span style={{ margin: "0 10px", color: "#777777" }}>
                  <ChevronsRight style={{ fontSize: "24px" }} />
                </span>
                {crumb.path ? (
                  <span 
                    onClick={() => navigate(crumb.path)}
                    style={{ 
                      color: "#FF5A5A", 
                      fontWeight: "bold",
                      cursor: "pointer",
                      transition: "opacity 0.2s"
                    }}
                    onMouseEnter={(e) => e.target.style.opacity = "0.7"}
                    onMouseLeave={(e) => e.target.style.opacity = "1"}
                  >
                    {crumb.label}
                  </span>
                ) : (
                  <span style={{ color: "#FF5A5A", fontWeight: "bold" }}>
                    {crumb.label}
                  </span>
                )}
              </React.Fragment>
            ))}
          </>
        </div>
      </div>

      {/* Right section */}
      <div className="flex items-center gap-2">
        <span className="text-[14px] text-muted-foreground hidden sm:inline">Welcome: </span>
        <span className="text-[18px] font-semibold">{userName}</span>
        <div className="flex items-center gap-[25px] ml-2">
          <Bell
            style={{ color: "#777", fontSize: "24px", cursor: "pointer" }}
            className="hover:scale-110 transition-all duration-300 ease-in-out"
          />
          <Users
            style={{ color: "#777", fontSize: "24px", cursor: "pointer" }}
            className="hover:scale-110 transition-all duration-300 ease-in-out"
          />
          <LogOut
            style={{ color: "#777", fontSize: "24px", cursor: "pointer" }}
            className="hover:scale-110 transition-all duration-300 ease-in-out"
          />
        </div>
      </div>
    </header>
  );
}
