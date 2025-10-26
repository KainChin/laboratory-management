import React from "react";
import { Info } from "lucide-react";

export default function OrderMeta({ meta = {} }) {
  return (
    <aside className="card meta-card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <Info size={14} />
          </div>
          <h4>Created</h4>
        </div>
      </div>

      <div className="meta-list">
        <div><span className="muted">Created</span><span className="meta-value">{meta.created}</span></div>
        <div><span className="muted">Created By</span><span className="meta-value">{meta.createdBy}</span></div>
        <div><span className="muted">Run Date</span><span className="meta-value">{meta.runDate}</span></div>
        <div><span className="muted">Run By</span><span className="meta-value">{meta.runBy}</span></div>
        <div><span className="muted">Reviewed Date</span><span className="meta-value">{meta.reviewedDate}</span></div>
        <div><span className="muted">Reviewed By</span><span className="meta-value">{meta.reviewedBy}</span></div>
      </div>

      <div className="status-row">
        <div className="status-left">
          <div className="icon-sq small">
            <Info size={14} />
          </div>
          <div className="muted">STATUS</div>
        </div>
        <div>
          <span className="badge green">{meta.status}</span>
        </div>
      </div>
    </aside>
  );
}