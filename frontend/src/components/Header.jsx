import React from "react";
import { ChevronsRight, Menu, Bell, Users, LogOut } from "lucide-react";
import { FaHeartbeat } from "react-icons/fa";

export default function Header() {
  return (
    <header className="h-[60px] bg-card border-b border-border flex items-center justify-between px-4">
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
            <span style={{ margin: "0 10px", color: "#777777" }}>
              <ChevronsRight style={{ fontSize: "24px" }} />
            </span>
            <span style={{ color: "#FF5A5A", fontWeight: "bold" }}>HOMEPAGE</span>
          </>
        </div>
      </div>

      {/* Right section */}
      <div className="flex items-center gap-2">
        <span className="text-[14px] text-muted-foreground hidden sm:inline">Welcome: </span>
        <span className="text-[18px] font-semibold">User</span>
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
