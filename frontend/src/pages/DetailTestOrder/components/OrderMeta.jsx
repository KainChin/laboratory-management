import React from "react";
import { Info, Calendar } from "lucide-react";

export default function OrderMeta(props) {
  // support many shapes people may pass:
  const candidate =
    props?.meta?.result ??
    props?.meta ??
    props?.result ??
    props?.payload ??
    props?.order?.result ??
    props?.order ??
    (props?.order && props.order.order) ??
    props ??
    {};

  const o = candidate?.result ?? candidate ?? {};

  const fmt = (v) => {
    if (v === null || v === undefined || v === "") return "-";
    if (typeof v === "string" && v.includes("/")) return v;
    try {
      const d = new Date(v);
      if (isNaN(d)) return String(v);
      return d.toLocaleString();
    } catch {
      return String(v);
    }
  };

  const created = fmt(o.createdAt ?? o.created ?? o.created_date);
  const createdBy = o.createdBy ?? o.created_by ?? o.createdByName ?? "-";
  const runDate = fmt(o.runAt ?? o.run_date ?? o.runAt);
  const runBy = o.runBy ?? o.run_by ?? "-";
  const reviewedDate = fmt(o.reviewedAt ?? o.reviewed_date);
  const reviewedBy = o.reviewedBy ?? o.review_by ?? "-";
  const status = (o.status ?? "-").toString().toUpperCase();

  const statusColorMap = {
    COMPLETED: "bg-emerald-100 text-emerald-700",
    REVIEWED: "bg-purple-100 text-purple-700",
    AI_REVIEWED: "bg-orange-50 text-orange-600",
    CANCELLED: "bg-rose-100 text-rose-700",
    PENDING: "bg-blue-100 text-blue-700",
  };

  const badgeClasses =
    (statusColorMap[status] ?? "bg-gray-100 text-gray-800") +
    " inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold";

  return (
    <aside className="card meta-card" role="complementary" aria-labelledby="order-meta-title">
      <div className="card-header">
        <div className="card-header-left">
          {/* Created uses Calendar icon now */}
          <div className="icon-sq" aria-hidden="true"><Calendar size={24} /></div>
          <h4 id="order-meta-title">Created</h4>
        </div>
      </div>

      <div className="meta-list" role="list" aria-label="Order metadata">
        <div role="listitem"><span className="muted">Created</span><span className="meta-value" aria-label="Created date">{created}</span></div>
        <div role="listitem"><span className="muted">Created By</span><span className="meta-value" aria-label="Created by user">{createdBy}</span></div>
        <div role="listitem"><span className="muted">Run Date</span><span className="meta-value" aria-label="Test run date">{runDate}</span></div>
        <div role="listitem"><span className="muted">Run By</span><span className="meta-value" aria-label="Test run by user">{runBy}</span></div>
        <div role="listitem"><span className="muted">Reviewed Date</span><span className="meta-value" aria-label="Reviewed date">{reviewedDate}</span></div>
        <div role="listitem"><span className="muted">Reviewed By</span><span className="meta-value" aria-label="Reviewed by user">{reviewedBy}</span></div>
      </div>

      <div className="status-row">
        <div className="status-left">
          {/* Status keeps Info icon (different from Created) */}
          <div className="icon-sq small"><Info size={24} /></div>
          <div className="muted" style={{ color: "#FF5A5A" }}>STATUS</div>
        </div>
        <div>
          <span className={badgeClasses} role="status" aria-label={`Current status: ${status}`}>{status}</span>
        </div>
      </div>
    </aside>
  );
}