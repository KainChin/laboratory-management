import { useState, useEffect, Fragment } from "react";
import { ChevronsRight, Menu, Bell, Users, LogOut } from "lucide-react";
import { FaHeartbeat } from "react-icons/fa";
import { useLocation, useNavigate } from "react-router-dom";
import { getUserName } from "../utils/jwtUtils";
import UserDetailsModal from "./UserDetailsModal";

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const [userName, setUserName] = useState("User");
  const [showUserModal, setShowUserModal] = useState(false);
  const [showUserTooltip, setShowUserTooltip] = useState(false);
  const [showLogoutTooltip, setShowLogoutTooltip] = useState(false);

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
              <Fragment key={index}>
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
              </Fragment>
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
          <div
            className="relative inline-flex"
            onMouseEnter={() => setShowUserTooltip(true)}
            onMouseLeave={() => setShowUserTooltip(false)}
          >
            <Users
              onClick={() => setShowUserModal(true)}
              style={{ color: "#777", fontSize: "24px", cursor: "pointer" }}
              className="hover:scale-110 transition-all duration-300 ease-in-out"
            />
            {/* User Details Tooltip */}
            {showUserTooltip && (
              <div 
                className="absolute top-full left-1/2 transform -translate-x-1/2 mt-2 px-3 py-1.5 bg-black text-white text-sm rounded shadow-lg whitespace-nowrap z-50 pointer-events-none"
                style={{
                  animation: "fadeIn 0.2s ease-in-out"
                }}
              >
                User details
                {/* Arrow pointing up */}
                <div 
                  className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-px"
                  style={{
                    width: 0,
                    height: 0,
                    borderLeft: "6px solid transparent",
                    borderRight: "6px solid transparent",
                    borderBottom: "6px solid black"
                  }}
                />
              </div>
            )}
          </div>
          <div
            className="relative inline-flex"
            onMouseEnter={() => setShowLogoutTooltip(true)}
            onMouseLeave={() => setShowLogoutTooltip(false)}
          >
            <LogOut
              style={{ color: "#777", fontSize: "24px", cursor: "pointer" }}
              className="hover:scale-110 transition-all duration-300 ease-in-out"
            />
            {/* Logout Tooltip */}
            {showLogoutTooltip && (
              <div 
                className="absolute top-full left-1/2 transform -translate-x-1/2 mt-2 px-3 py-1.5 bg-black text-white text-sm rounded shadow-lg whitespace-nowrap z-50 pointer-events-none"
                style={{
                  animation: "fadeIn 0.2s ease-in-out"
                }}
              >
                Logout
                {/* Arrow pointing up */}
                <div 
                  className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-px"
                  style={{
                    width: 0,
                    height: 0,
                    borderLeft: "6px solid transparent",
                    borderRight: "6px solid transparent",
                    borderBottom: "6px solid black"
                  }}
                />
              </div>
            )}
          </div>
        </div>
      </div>

      {/* User Details Modal */}
      <UserDetailsModal
        open={showUserModal}
        onClose={() => setShowUserModal(false)}
      />
    </header>
  );
}
