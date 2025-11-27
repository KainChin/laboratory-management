import { useState, useEffect, Fragment } from "react";
import { ChevronsRight, Menu, LogOut } from "lucide-react";
import { FaHeartbeat } from "react-icons/fa";
import { useLocation, useNavigate } from "react-router-dom";
import { getUserName } from "../utils/jwtUtils";
import publicApi from "../api/publicAxios";

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const [userName, setUserName] = useState("User");
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);
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

  const handleLogout = async () => {
    try {
      await publicApi.delete("/auth/logout", {
        withCredentials: true,
      });
      localStorage.removeItem("token");
      navigate("/login");
    } catch (error) {
      alert("Đăng xuất thất bại! Vui lòng thử lại.");
      console.error("Logout error:", error);
    }
  };

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
          <div
            className="relative inline-flex"
            onMouseEnter={() => setShowLogoutTooltip(true)}
            onMouseLeave={() => setShowLogoutTooltip(false)}
          >
            <LogOut
              onClick={() => setShowLogoutConfirm(true)}
              style={{ color: "#777", fontSize: "24px", cursor: "pointer" }}
              className="hover:scale-110 transition-all duration-300 ease-in-out"
            />
            {showLogoutTooltip && (
              <div
                className="absolute top-full right-0 mt-2 px-3 py-1.5 bg-black text-white text-sm rounded shadow-lg whitespace-nowrap z-50"
                style={{ pointerEvents: 'none' }}
              >
                Logout
                <div
                  className="absolute top-0 right-1 -translate-y-full"
                  style={{
                    width: 0,
                    height: 0,
                    borderLeft: "8px solid transparent",
                    borderRight: "8px solid transparent",
                    borderBottom: "8px solid black"
                  }}
                />
              </div>
            )}
          </div>
        </div>
      </div>
      {/* Modal xác nhận logout */}
      {showLogoutConfirm && (
        <div
          className="fixed inset-0 flex items-center justify-center z-[10000] bg-black bg-opacity-60"
          style={{ animation: "fadeIn 0.2s" }}
        >
          <div className="bg-white p-6 rounded-lg shadow-2xl max-w-md w-full text-center border border-[#FF5A5A]">
            <div className="flex justify-between items-center mb-4">
              <span className="flex items-center gap-2 text-[#FF5A5A] text-xl font-bold">
                <LogOut style={{ color: "#FF5A5A" }} /> Confirm Logout
              </span>
              <button
                className="text-xl text-gray-400 hover:text-black"
                onClick={() => setShowLogoutConfirm(false)}
              >
                &times;
              </button>
            </div>
            <div className="mb-6">Are you sure you want to log out?</div>
            <div className="flex gap-3 justify-center">
              <button
                className="px-4 min-h-[40px] py-2 bg-gray-200 rounded hover:bg-gray-300"
                onClick={() => setShowLogoutConfirm(false)}
              >
                Cancel
              </button>
              <button
                className="px-4 min-h-[40px] py-2 bg-[#FF5A5A] text-white rounded hover:bg-[#FF3A3A] transition-colors duration-300"
                onClick={async () => {
                  setShowLogoutConfirm(false);
                  await handleLogout();
                }}
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
