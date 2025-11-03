import React from "react";
import { Info } from "lucide-react";

export default function OrderMeta(props) {
  // debug: in ra toàn bộ props để kiểm tra shape
  console.log("OrderMeta props:", props);

  // support many shapes people may pass:
  // 1) whole payload { result: {...} }
  // 2) normalized object directly
  // 3) { order: {...} } or { order: { order: {...} } }
  // 4) { meta: {...} }
  const candidate =
    props?.meta?.result ??
    props?.meta ??
    props?.result ??
    props?.payload ??
    props?.order?.result ??
    props?.order ??
    // handle nested double-wrapped: props.order.order
    (props?.order && props.order.order) ??
    // fallback to props itself (in case they passed normalized directly)
    props ??
    {};

  const o = candidate?.result ?? candidate ?? {};

  console.log("OrderMeta using o:", o);

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

  // Colors tuned to your screenshot
  const statusColorMap = {
    COMPLETED: "bg-emerald-100 text-emerald-700",   // green badge
    AI_REVIEWED: "bg-orange-50 text-orange-600",    // orange badge
    CANCELLED: "bg-rose-100 text-rose-700",         // red/pink badge
    PENDING: "bg-blue-100 text-blue-700",           // blue badge
  };
  
  const badgeClasses =
    (statusColorMap[status] ?? "bg-gray-100 text-gray-800") +
    " inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold";

  return (
    <aside className="card meta-card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq"><Info size={14} /></div>
          <h4>Created</h4>
        </div>
      </div>

      <div className="meta-list">
        <div><span className="muted">Created</span><span className="meta-value">{created}</span></div>
        <div><span className="muted">Created By</span><span className="meta-value">{createdBy}</span></div>
        <div><span className="muted">Run Date</span><span className="meta-value">{runDate}</span></div>
        <div><span className="muted">Run By</span><span className="meta-value">{runBy}</span></div>
        <div><span className="muted">Reviewed Date</span><span className="meta-value">{reviewedDate}</span></div>
        <div><span className="muted">Reviewed By</span><span className="meta-value">{reviewedBy}</span></div>
      </div>

      <div className="status-row">
        <div className="status-left">
          <div className="icon-sq small"><Info size={14} /></div>
          <div className="muted">STATUS</div>
        </div>
        <div>
          <span className={badgeClasses}>{status}</span>
        </div>
      </div>
    </aside>
  );
}