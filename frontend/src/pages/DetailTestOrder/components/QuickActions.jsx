import React from "react";
import { List } from "lucide-react";

export default function QuickActions() {
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
        <button className="btn-green full">Mark as Reviewed</button>
        <button className="btn-purple full">AI Auto Review</button>
        <button className="btn-orange full">Generate Report</button>
        <button className="btn-red full">Edit Order</button>
      </div>
    </aside>
  );
}