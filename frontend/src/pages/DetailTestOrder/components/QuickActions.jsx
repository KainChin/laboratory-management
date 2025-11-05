import React from "react";
import { List } from "lucide-react";
import { useParams } from "react-router-dom";
import axios from '../../../api/axios';

export default function QuickActions({ status, onStatusChange }) {
  const { id } = useParams();

  const handleMarkAsReviewed = async () => {
    console.log("Current id from useParams:", id);
    try {
      const response = await axios.patch(`/api/test-orders/${id}/review`);
      console.log("Review API response:", response);
      onStatusChange("REVIEWED");
    } catch (error) {
      console.error("Error marking as reviewed:", error);
      console.error("id was:", id);
    }
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
          >
            Mark as Reviewed
          </button>
        )}
        <button className="btn-purple full">AI Auto Review</button>
        <button className="btn-orange full">Generate Report</button>
        <button className="btn-red full">Edit Order</button>
      </div>
    </aside>
  );
}