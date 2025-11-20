import React, { useState, useMemo } from "react";
import { AlertCircle } from "lucide-react";
import ServiceWidget from "../components/ServiceWidget";
import AuthStatus from "../components/AuthStatus";

// Mock user data
const mockUserInfo = {
  name: "Nguyen Van Admin",
  deletedAt: null, // Set to a future date string to test deletion warning
};

export default function Index() {
  const displayUser = mockUserInfo;

  // Calculate remaining days until account deletion
  const deletionInfo = useMemo(() => {
    if (!displayUser?.deletedAt) {
      return null;
    }

    const deletionDate = new Date(displayUser.deletedAt);
    const today = new Date();
    const timeDiff = deletionDate.getTime() - today.getTime();
    const daysRemaining = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));

    return {
      daysRemaining,
      deletionDate: deletionDate.toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
      }),
    };
  }, [displayUser?.deletedAt]);

  return (
    <main className="flex-1 p-5 lg:p-6">
      <div className="mb-[20px]">
        <h1 className="text-[24px] font-bold text-[#FF5A5A] mb-[5px]">HOMEPAGE</h1>
        <p className="text-[#777777]">Start exploring features</p>
      </div>
      <div className="mx-auto">
        {/* Account Deletion Warning Banner */}
        {deletionInfo && deletionInfo.daysRemaining > 0 && (
          <div className="bg-warning-bg border-l-4 border-warning-border p-4 mb-6 rounded-md flex items-center justify-between shadow-sm">
            <div className="flex items-center gap-3">
              <AlertCircle className="w-5 h-5 text-warning-border flex-shrink-0" />
              <div>
                <span className="text-sm font-semibold text-warning-text">
                  Your account will be permanently deleted in {deletionInfo.daysRemaining} day
                  {deletionInfo.daysRemaining > 1 ? "s" : ""}
                </span>
                <span className="text-sm text-warning-text ml-2">
                  (on {deletionInfo.deletionDate})
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Authentication Status (Optional: có thể xóa nếu không cần) */}
        <div className="w-full max-w-[600px] mx-auto mb-6">
          <AuthStatus />
        </div>

        {/* Main Content */}
        <div className="flex flex-col items-center gap-8">
          <div className="w-full max-w-[600px] text-center p-8 shadow-md rounded-lg bg-white border border-gray-200">
            <h2 className="text-[#FF5A5A] font-bold text-[18px] mb-3">
              Good to see you again! 👋
            </h2>
            <p className="text-[16px] text-[#777777] leading-relaxed">
              Please choose one of the available features to get started.
              <br />
              We're happy to have you back 💖
            </p>
          </div>

          <ServiceWidget />
        </div>
      </div>
    </main>
  );
}
