import React, { useEffect, useState, useRef } from "react";
import { MessageSquare, Edit2, Trash2, Check, X } from "lucide-react";
import "../DetailTestOrder.css";

export default function Comments({ orderId: propOrderId = null, currentUser = null }) {
  const [text, setText] = useState("");
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState(null);

  // confirmation state: comment id that requests confirmation
  const [confirmId, setConfirmId] = useState(null);
  const [confirmSide, setConfirmSide] = useState("right"); // 'right' or 'left'
  const commentRefs = useRef({}); // store DOM node per comment id

  // edit state
  const [editingId, setEditingId] = useState(null);
  const [editingText, setEditingText] = useState("");
  const [editingSaving, setEditingSaving] = useState(false);

  // success notification for edits
  const [successId, setSuccessId] = useState(null);
  const [successMsg, setSuccessMsg] = useState("");
  const successTimerRef = useRef(null);

  // ref to scrollable comments container
  const commentsListRef = useRef(null);

  const getOrderId = () => {
    if (propOrderId) return String(propOrderId).trim();
    const parts = window.location.pathname.split("/").filter(Boolean);
    return parts[parts.length - 1] ?? "";
  };
  
  // helper to determine createdBy value
  const getCreatedBy = () => {
    // if caller passed currentUser as string or object
    if (currentUser) {
      if (typeof currentUser === "string" && currentUser.trim()) return currentUser.trim();
      if (typeof currentUser === "object") {
        return (
          currentUser.userName ||
          currentUser.username ||
          currentUser.name ||
          currentUser.fullName ||
          currentUser.displayName ||
          currentUser.email ||
          ""
        );
      }
    }

    // try common localStorage keys (string or JSON)
    try {
      const tryKeys = ["user", "currentUser", "userName", "username"];
      for (const k of tryKeys) {
        const v = localStorage.getItem(k);
        if (!v) continue;
        // try parse JSON
        try {
          const obj = JSON.parse(v);
          if (obj) {
            const found =
              obj.userName ||
              obj.username ||
              obj.name ||
              obj.fullName ||
              obj.displayName ||
              obj.email;
            if (found) return String(found);
          }
        } catch (_) {
          // not JSON, treat as plain string
          if (typeof v === "string" && v.trim()) return v.trim();
        }
      }
    } catch (_) {}

    // fallback value so server validation won't block
    return "System";
  };

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const id = getOrderId();
        if (!id) throw new Error("Missing orderId");
        const res = await fetch(`http://localhost:6868/api/test-orders/${encodeURIComponent(id)}`);
        if (!res.ok) throw new Error(`Failed to load (${res.status})`);
        const payload = await res.json();
        const src = payload?.result ?? payload ?? {};
        if (!mounted) return;
        // limit number of items rendered to avoid page growth (adjust cap as needed)
        let list = Array.isArray(src.comments) ? src.comments.slice() : [];

        // Try to parse known datetime fields and sort newest first when possible
        const parseDate = (c) => {
          if (!c) return NaN;
          const keys = ["createdAt","created_at","createdOn","created_on","createdDate","created_date","timestamp","time","created"];
          for (const k of keys) {
            const v = c[k];
            if (v == null) continue;
            // if object with .seconds (firebase) or ._seconds
            if (typeof v === "object") {
              if (v.seconds != null) return Number(v.seconds) * 1000 + (v.nanoseconds ? Math.floor(v.nanoseconds/1000000) : 0);
              if (v._seconds != null) return Number(v._seconds) * 1000;
            }
            const n = Date.parse(String(v));
            if (!isNaN(n)) return n;
          }
          return NaN;
        };

        const hasValidDate = list.some((c) => !isNaN(parseDate(c)));
        if (hasValidDate) {
          list.sort((a, b) => {
            const da = parseDate(a);
            const db = parseDate(b);
            return (isNaN(db) ? 0 : db) - (isNaN(da) ? 0 : da); // newest first
          });
        } else {
          // no date fields detected — assume server returns oldest-first and reverse so newest on top
          list = list.reverse();
        }

        setComments(list.slice(0, 100));
      } catch (err) {
        if (!mounted) return;
        setError(err.message || "Failed to load comments");
        console.error("Comments load error:", err);
      } finally {
        if (!mounted) return;
        setLoading(false);
      }
    }
    load();
    return () => { mounted = false; };
  }, [propOrderId]);

  // clear success timer on unmount
  useEffect(() => {
    return () => {
      if (successTimerRef.current) clearTimeout(successTimerRef.current);
    };
  }, []);

  async function handleAdd() {
    const trimmed = (text || "").trim();
    if (!trimmed) return;
    setSaving(true);
    setError(null);
    try {
      const id = getOrderId();
      if (!id) throw new Error("Missing orderId");

      const payloadBody = {
        commentText: trimmed,
      };
      // ensure createdBy is always sent (server requires it)
      const createdBy = getCreatedBy();
      if (createdBy) payloadBody.createdBy = createdBy;

      const res = await fetch(`http://localhost:6868/api/test-orders/${encodeURIComponent(id)}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept": "application/json" },
        body: JSON.stringify(payloadBody),
      });

      if (!res.ok) {
        let errMsg = `Save failed: ${res.status}`;
        try {
          const j = await res.json();
          if (j?.message) errMsg = Array.isArray(j.message) ? j.message.join(", ") : String(j.message);
        } catch (_) {}
        throw new Error(errMsg);
      }

      const payload = await res.json().catch(() => null);
      const created = payload?.result ?? payload;
      if (created && (created.commentId || created.commentId === "")) {
        // prepend new comment and keep container scrolled to top so page doesn't grow
        setComments((s) => {
          const next = [created, ...s];
          return next.slice(0, 100); // cap to prevent long page
        });
      } else {
        const r2 = await fetch(`http://localhost:6868/api/test-orders/${encodeURIComponent(id)}`);
        if (r2.ok) {
          const p2 = await r2.json();
          const src2 = p2?.result ?? p2 ?? {};
          const list2 = Array.isArray(src2.comments) ? src2.comments : [];
          setComments(list2.slice(0, 100));
        }
      }

      setText("");

      // ensure scroll stays at top (use rAF/timeout so DOM updated)
      requestAnimationFrame(() => {
        if (commentsListRef.current) commentsListRef.current.scrollTop = 0;
      });
    } catch (err) {
      setError(err.message || "Failed to add comment");
      console.error("Add comment error:", err);
    } finally {
      setSaving(false);
    }
  }

  async function doDelete(commentId) {
    if (!commentId) return;
    setDeletingId(commentId);
    setError(null);
    try {
      const id = getOrderId();
      if (!id) throw new Error("Missing orderId");
      const url = `http://localhost:6868/api/test-orders/${encodeURIComponent(id)}/comments/${encodeURIComponent(commentId)}`;
      const res = await fetch(url, { method: "DELETE", headers: { "Accept": "application/json" } });
      if (!res.ok) {
        let errMsg = `Delete failed: ${res.status}`;
        try {
          const j = await res.json();
          if (j?.message) errMsg = Array.isArray(j.message) ? j.message.join(", ") : String(j.message);
        } catch (_) {}
        throw new Error(errMsg);
      }
      setComments((prev) => prev.filter((c) => (c.commentId ?? c.id ?? "") !== commentId).slice(0, 100));
    } catch (err) {
      setError(err.message || "Failed to delete comment");
      console.error("Delete comment error:", err);
    } finally {
      setDeletingId(null);
      setConfirmId(null);
    }
  }

  // EDIT: open inline editor
  function requestEdit(comment) {
    const cid = comment.commentId ?? comment.id ?? null;
    if (!cid) return;
    setEditingId(cid);
    setEditingText(comment.commentText ?? comment.comment ?? comment.body ?? "");
    setError(null);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditingText("");
  }

  async function saveEdit(commentId) {
    if (!commentId) return;
    const trimmed = (editingText || "").trim();
    if (!trimmed) return;
    setEditingSaving(true);
    setError(null);
    try {
      const id = getOrderId();
      if (!id) throw new Error("Missing orderId");
      const url = `http://localhost:6868/api/test-orders/${encodeURIComponent(id)}/comments/${encodeURIComponent(commentId)}`;
      const res = await fetch(url, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ commentText: trimmed }),
      });
      if (!res.ok) {
        let errMsg = `Update failed: ${res.status}`;
        try {
          const j = await res.json();
          if (j?.message) errMsg = Array.isArray(j.message) ? j.message.join(", ") : String(j.message);
        } catch (_) {}
        throw new Error(errMsg);
      }
      const payload = await res.json().catch(() => null);
      const updated = payload?.result ?? payload;

      // update local list (prefer server returned fields)
      setComments((prev) =>
        prev.map((c) => {
          const cid = c.commentId ?? c.id ?? null;
          if (cid !== commentId) return c;
          if (updated && (updated.commentId || updated.commentText)) return { ...c, ...updated };
          return { ...c, commentText: trimmed };
        }).slice(0, 100)
      );

      // show inline success badge near edited comment
      setSuccessId(commentId);
      setSuccessMsg("Updated successfully");
      if (successTimerRef.current) clearTimeout(successTimerRef.current);
      successTimerRef.current = setTimeout(() => {
        setSuccessId(null);
        setSuccessMsg("");
      }, 3000);

      setEditingText("");
      setEditingId(null);
    } catch (err) {
      setError(err.message || "Failed to update comment");
      console.error("Update comment error:", err);
    } finally {
      setEditingSaving(false);
    }
  }

  // Request delete: scroll comment into view and compute side for popup
  function requestDelete(commentId) {
    const el = commentRefs.current[commentId];
    const container = commentsListRef.current;
    const DLG_W = 320; // estimated dialog width

    // default side
    setConfirmSide("right");

    if (el && container) {
      // bring comment to center of the container (smooth)
      const elRect = el.getBoundingClientRect();
      const contRect = container.getBoundingClientRect();
      const currentScroll = container.scrollTop;
      const targetScroll = currentScroll + (elRect.top - (contRect.top + contRect.height / 2));
      container.scrollTo({ top: Math.max(0, targetScroll), behavior: "smooth" });

      // compute available space and flip if needed after scroll completes (small delay)
      setTimeout(() => {
        const updatedRect = el.getBoundingClientRect();
        const rightSpace = window.innerWidth - updatedRect.right;
        const leftSpace = updatedRect.left;
        if (rightSpace < DLG_W + 24 && leftSpace >= DLG_W + 24) {
          setConfirmSide("left");
        } else {
          setConfirmSide("right");
        }
      }, 220);
    }

    // show confirmation (will appear after scroll/side computed)
    setConfirmId(commentId);
  }
  function cancelDelete() {
    setConfirmId(null);
  }

  return (
    <section className="card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <MessageSquare size={14} />
          </div>
          <h3>Comments</h3>
        </div>
      </div>

      <div className="muted small-desc">Comments and notes related to this test order</div>

      {error && <div style={{ color: "#dc2626", marginBottom: 8 }}>{error}</div>}

      {/* set maxHeight so only ~3 comments show; overflow creates scrollbar */}
      <div
        className="comments-list"
        ref={commentsListRef}
        style={{
          maxHeight: 260,         // ~3 comments height — chỉnh nếu cần
          overflowY: "auto",
          paddingRight: 8,
        }}
      >
        {loading ? (
          <div className="dto-empty">Loading comments...</div>
        ) : comments.length === 0 ? (
          <div className="dto-empty">No comments</div>
        ) : (
          comments.map((c, i) => {
            const cid = c.commentId ?? c.id ?? String(i);
            const showingConfirm = confirmId === cid;
            const isEditing = editingId === cid;
            const isSuccess = successId === cid;
            return (
              <div className="comment" key={cid} style={{ position: "relative" }} ref={(el) => { commentRefs.current[cid] = el; }}>
                <div className="comment-author"><b>{c.createdBy ?? c.author ?? "Unknown"}</b></div>

                {!isEditing ? (
                  <div className="comment-body" style={{ marginBottom: 8 }}>{c.commentText ?? c.comment ?? c.body}</div>
                ) : (
                  <div style={{ marginBottom: 8 }}>
                    <input
                      value={editingText}
                      onChange={(e) => setEditingText(e.target.value)}
                      placeholder="Edit your comment..."
                      disabled={editingSaving}
                      style={{ width: "100%", padding: "8px", borderRadius: "4px", border: "1px solid #e6e9ef" }}
                    />
                  </div>
                )}

                <div style={{ display: "flex", justifyContent: "flex-end" }}>
                  <div className="comment-actions" style={{ display: "flex", gap: 8 }}>
                    {!isEditing && (
                      <button className="icon-btn" title="Edit" onClick={() => requestEdit(c)} disabled={editingSaving || deletingId === cid}>
                        <Edit2 size={14} />
                      </button>
                    )}
                    {isEditing ? (
                      <>
                        <button
                          className="icon-btn"
                          title="Save"
                          onClick={() => saveEdit(cid)}
                          disabled={editingSaving}
                          style={{ color: "#4caf50" }}
                        >
                          <Check size={14} />
                        </button>
                        <button
                          className="icon-btn"
                          title="Cancel"
                          onClick={cancelEdit}
                          disabled={editingSaving}
                          style={{ color: "#f44336" }}
                        >
                          <X size={14} />
                        </button>
                      </>
                    ) : (
                      <button
                        className="icon-btn"
                        title="Delete"
                        onClick={() => requestDelete(cid)}
                        disabled={deletingId === cid}
                        aria-disabled={deletingId === cid}
                      >
                        <Trash2 size={14} />
                      </button>
                    )}
                  </div>
                </div>

                {/* Inline confirmation box near the comment */}
                {showingConfirm && (
                  <div
                    role="dialog"
                    aria-modal="true"
                    style={{
                      position: "absolute",
                      right: confirmSide === "right" ? 12 : "auto",
                      left: confirmSide === "left" ? 12 : "auto",
                      top: "calc(100% - 8px)",
                      width: 300,
                      background: "#fff",
                      borderRadius: 8,
                      boxShadow: "0 8px 30px rgba(2,6,23,0.12)",
                      padding: 12,
                      zIndex: 80,
                      border: "1px solid #eef2f7",
                    }}
                  >
                    <div style={{ display: "flex", gap: 10, alignItems: "center", marginBottom: 8 }}>
                      <div style={{ width:32, height:32, borderRadius:6, background:"#fff6f7", display:"flex", alignItems:"center", justifyContent:"center", border:"1px solid #ffe3e6", color:"#ef4444" }}>
                        <Trash2 size={14} />
                      </div>
                      <div style={{ fontWeight:700 }}>Delete comment</div>
                    </div>
                    <div style={{ color:"#374151", fontSize:13, marginBottom:12 }}>Are you sure you want to delete this comment?</div>
                    <div style={{ display:"flex", justifyContent:"flex-end", gap:8 }}>
                      <button onClick={cancelDelete} style={{ padding:"6px 10px", borderRadius:6, border:"1px solid #e6e9ef", background:"#fff", cursor:"pointer" }}>
                        Cancel
                      </button>
                      <button
                        onClick={() => doDelete(cid)}
                        disabled={deletingId === cid}
                        style={{ padding:"6px 10px", borderRadius:6, border:"none", background:"#ff5a67", color:"#fff", fontWeight:700, cursor:"pointer" }}
                      >
                        {deletingId === cid ? "Deleting..." : "Delete"}
                      </button>
                    </div>
                  </div>
                )}

                {/* Inline success badge shown near edited comment */}
                {isSuccess && (
                  <div style={{
                    position: "absolute",
                    right: 12,
                    top: 8,
                    background: "#10B981",
                    color: "#fff",
                    padding: "6px 10px",
                    borderRadius: 999,
                    fontWeight: 700,
                    fontSize: 12,
                    boxShadow: "0 6px 16px rgba(16,185,129,0.16)"
                  }}>
                    {successMsg || "Updated"}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      <div className="comment-input" style={{ marginTop: 16 }}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Enter your comment..."
          disabled={saving}
          onKeyDown={(e) => { if (e.key === "Enter") handleAdd(); }}
          style={{ flex: 1 }}
        />
        <button className="btn-primary" onClick={handleAdd} disabled={saving}>
          {saving ? "Saving..." : "Add Comment"}
        </button>
      </div>
    </section>
  );
}