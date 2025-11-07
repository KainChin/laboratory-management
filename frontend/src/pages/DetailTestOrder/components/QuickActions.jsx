import React, { useEffect, useRef, useState } from "react";
import { List } from "lucide-react";
import { useParams } from "react-router-dom";
import axios from '../../../api/axios';
import { createPortal } from "react-dom";

export default function QuickActions({ status, onStatusChange }) {
  const { id } = useParams();
  const [isReviewing, setIsReviewing] = useState(false);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const previousOverflowRef = useRef("");

  useEffect(() => {
    if (typeof document === "undefined") return;
    const { body } = document;
    if (isConfirmOpen) {
      previousOverflowRef.current = body.style.overflow;
      body.style.overflow = "hidden";
      return () => {
        body.style.overflow = previousOverflowRef.current || "";
      };
    }

    body.style.overflow = previousOverflowRef.current || "";

    return () => {
      body.style.overflow = previousOverflowRef.current || "";
    };
  }, [isConfirmOpen]);

  const handleMarkAsReviewed = async () => {
    if (isReviewing) return;
    setIsConfirmOpen(true);
  };

  const confirmReview = async () => {
    if (isReviewing) return;

    console.log("Current id from useParams:", id);
    try {
      setIsReviewing(true);
      const response = await axios.patch(`/api/test-orders/${id}/review`);
      console.log("Review API response:", response);
      const nextStatus =
        response?.data?.result?.status ??
        response?.data?.status ??
        "REVIEWED";
      onStatusChange(String(nextStatus).toUpperCase());
      setIsConfirmOpen(false);
    } catch (error) {
      console.error("Error marking as reviewed:", error);
      console.error("id was:", id);
    } finally {
      setIsReviewing(false);
    }
  };

  const closeConfirm = () => {
    if (isReviewing) return;
    setIsConfirmOpen(false);
  };

  return (
    <aside className="card quick-actions">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <List size={14} />
          </div>
          <h4>Quick Action</h4>
        </div>
      </div>

      <div className="actions-list">
        {(status || "").toString().toUpperCase() === "COMPLETED" && (
          <button 
            className="btn-green full" 
            onClick={handleMarkAsReviewed}
            disabled={isReviewing}
          >
            {isReviewing ? "Processing..." : "Mark as Reviewed"}
          </button>
        )}
        <button className="btn-purple full">AI Auto Review</button>
        <button className="btn-orange full">Generate Report</button>
        <button className="btn-red full">Edit Order</button>
      </div>

      {isConfirmOpen && createPortal(
        <div
          className="modal-overlay"
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100vw",
            height: "100vh",
            background: "rgba(0,0,0,0.45)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 9999,
            padding: 20,
          }}
          onClick={closeConfirm}
        >
          <div
            className="bg-white rounded-2xl w-full max-w-lg p-6 shadow-lg mx-auto"
            style={{ maxHeight: "90vh", overflow: "auto" }}
            role="dialog"
            aria-modal
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-2xl text-red-500 font-bold text-center mb-4">
              Confirm Review
            </h3>
            <p className="text-center text-sm text-gray-600 mb-6">
              Are you sure you want to mark this order as reviewed?
            </p>

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
              <button
                onClick={closeConfirm}
                className="px-4 py-2 rounded-lg"
                style={{ background: "#f3f4f6" }}
              >
                Cancel
              </button>
              <button
                onClick={confirmReview}
                className="px-4 py-2 rounded-lg"
                style={{
                  background: "#10b981",
                  color: "#fff",
                  fontWeight: 600,
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 8,
                  opacity: isReviewing ? 0.6 : 1,
                  cursor: isReviewing ? "not-allowed" : "pointer",
                }}
                disabled={isReviewing}
              >
                {isReviewing ? "Processing..." : "Yes, mark as reviewed"}
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </aside>
  );
}