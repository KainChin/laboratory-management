import React, { useEffect, useState, useRef } from "react";
import { createPortal } from "react-dom";
import { MessageSquare, Edit2, Trash2 } from "lucide-react";
import "../DetailTestOrder.css";

export default function Comments({ orderId: propOrderId = null, currentUser = null }) {
  const [text, setText] = useState("");
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState(null);

  const [editingId, setEditingId] = useState(null);
  const [editingText, setEditingText] = useState("");
  const [editingSaving, setEditingSaving] = useState(false);

  const [successId, setSuccessId] = useState(null);
  const [successMsg, setSuccessMsg] = useState("");
  const successTimerRef = useRef(null);

  const commentsListRef = useRef(null);
  const editingInputRef = useRef(null);
  const commentRefs = useRef({});

  // confirm portal state (rendered to document.body)
  const [confirmPortal, setConfirmPortal] = useState(null);
  // shape: { commentId, top, left, width, height, vertical, pointerLeft }

  const getOrderId = () => {
    if (propOrderId) return String(propOrderId).trim();
    const parts = window.location.pathname.split("/").filter(Boolean);
    return parts[parts.length - 1] ?? "";
  };

  const getCreatedBy = () => {
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
    try {
      const tryKeys = ["user", "currentUser", "userName", "username"];
      for (const k of tryKeys) {
        const v = localStorage.getItem(k);
        if (!v) continue;
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
          if (typeof v === "string" && v.trim()) return v.trim();
        }
      }
    } catch (_) {}
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
        let list = Array.isArray(src.comments) ? src.comments.slice() : [];

        const parseDate = (c) => {
          if (!c) return NaN;
          const keys = ["createdAt","created_at","createdOn","created_on","createdDate","created_date","timestamp","time","created"];
          for (const k of keys) {
            const v = c[k];
            if (v == null) continue;
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
            return (isNaN(db) ? 0 : db) - (isNaN(da) ? 0 : da);
          });
        } else {
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

      const payloadBody = { commentText: trimmed };
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
        setComments((s) => [created, ...s].slice(0, 100));
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
      setConfirmPortal(null);
    }
  }

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

      setComments((prev) =>
        prev
          .map((c) => {
            const cid = c.commentId ?? c.id ?? null;
            if (cid !== commentId) return c;
            if (updated && (updated.commentId || updated.commentText)) return { ...c, ...updated };
            return { ...c, commentText: trimmed };
          })
          .slice(0, 100)
      );

      setSuccessId(commentId);
      setSuccessMsg("Updated");
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

  // new: render confirmation dialog as a portal so it's never clipped by overflow
  function openConfirmPortal(commentId, btnEl) {
    // estimate dialog size and pointer, compute fixed coords
    const DLG_W = 320;
    const DLG_H = 120;
    try {
      if (!btnEl || !btnEl.getBoundingClientRect) throw new Error("no btnEl");
      const btnRect = btnEl.getBoundingClientRect();
      const vw = window.innerWidth;
      const vh = window.innerHeight;
      // prefer below
      let vertical = "below";
      let top = Math.round(btnRect.bottom + 8);
      if (btnRect.bottom + 8 + DLG_H > vh && btnRect.top - 8 - DLG_H >= 0) {
        vertical = "above";
        top = Math.round(btnRect.top - 8 - DLG_H);
      }
      // center horizontally on button, clamp to viewport
      let left = Math.round(btnRect.left + btnRect.width / 2 - DLG_W / 2);
      left = Math.max(8, Math.min(left, vw - DLG_W - 8));
      const pointerCenter = Math.round(btnRect.left + btnRect.width / 2);
      const pointerLeft = Math.max(12, Math.min(DLG_W - 12, pointerCenter - left));
      setConfirmPortal({ commentId, top, left, width: DLG_W, height: DLG_H, vertical, pointerLeft });
    } catch (e) {
      // fallback: clear portal (could render inline)
      setConfirmPortal({ commentId, top: null });
    }
  }

  // close portal on scroll/resize/escape/click outside
  useEffect(() => {
    if (!confirmPortal) return;
    function onScroll() {
      setConfirmPortal(null);
    }
    function onResize() {
      setConfirmPortal(null);
    }
    function onKey(e) {
      if (e.key === "Escape") setConfirmPortal(null);
    }
    function onDown(e) {
      // if click is outside portal area close it (portal element in body has id 'comment-confirm-portal')
      const el = document.getElementById("comment-confirm-portal");
      if (!el) return;
      if (!el.contains(e.target)) setConfirmPortal(null);
    }
    window.addEventListener("scroll", onScroll, true);
    window.addEventListener("resize", onResize);
    window.addEventListener("keydown", onKey);
    document.addEventListener("mousedown", onDown);
    return () => {
      window.removeEventListener("scroll", onScroll, true);
      window.removeEventListener("resize", onResize);
      window.removeEventListener("keydown", onKey);
      document.removeEventListener("mousedown", onDown);
    };
  }, [confirmPortal]);

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

      <div
        className="comments-list"
        ref={commentsListRef}
        style={{
          maxHeight: 260,
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
            const isEditing = editingId === cid;
            const isSuccess = successId === cid;
            return (
              <div
                className="comment"
                key={cid}
                style={{ position: "relative", padding: 12, borderRadius: 8, marginBottom: 10, background: "#fff", boxShadow: "0 6px 14px rgba(12,18,26,0.04)" }}
                ref={(el) => {
                  commentRefs.current[cid] = el;
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
                  <div>
                    <div className="comment-author" style={{ marginBottom: 6, fontSize: 14 }}>
                      <b>{c.createdBy ?? c.author ?? "Unknown"}</b>
                    </div>

                    {!isEditing ? (
                      <div className="comment-body" style={{ marginBottom: 6, color: "#111827" }}>{c.commentText ?? c.comment ?? c.body}</div>
                    ) : (
                      <div style={{ marginBottom: 8 }}>
                        <textarea
                          ref={editingInputRef}
                          value={editingText}
                          onChange={(e) => setEditingText(e.target.value)}
                          placeholder="Edit your comment..."
                          disabled={editingSaving}
                          rows={4}
                          style={{ width: "100%", padding: 10, borderRadius: 8, border: "1px solid #e6e9ef", resize: "vertical", minHeight: 80 }}
                        />
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 8, gap: 8 }}>
                          <div style={{ color: "#6b7280", fontSize: 13 }} />
                          <div style={{ display: "flex", gap: 8 }}>
                            <button
                              onClick={() => saveEdit(cid)}
                              disabled={editingSaving}
                              style={{ padding: "8px 12px", borderRadius: 8, background: "#10b981", color: "#fff", border: "none", fontWeight: 700, cursor: "pointer" }}
                              title="Save (Ctrl+Enter)"
                            >
                              {editingSaving ? "Saving..." : "Save"}
                            </button>
                            <button
                              onClick={cancelEdit}
                              disabled={editingSaving}
                              style={{ padding: "8px 12px", borderRadius: 8, background: "#ffffff", color: "#374151", border: "1px solid #e6e9ef", cursor: "pointer" }}
                            >
                              Cancel
                            </button>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>

                  <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 8 }}>
                    <div style={{ fontSize: 12, color: "#9ca3af" }} />

                    <div className="comment-actions" style={{ display: "flex", gap: 8 }}>
                      {!isEditing && (
                        <button
                          className="icon-btn"
                          title="Edit"
                          onClick={() => requestEdit(c)}
                          disabled={editingSaving || deletingId === cid}
                          style={{ background: "#fff", border: "1px solid #eef2f7", padding: 8, borderRadius: 8, cursor: "pointer" }}
                        >
                          <Edit2 size={14} />
                        </button>
                      )}
                      {!isEditing && (
                        <button
                          className="icon-btn"
                          title="Delete"
                          onClick={(e) => {
                            // pass the actual button element so portal can anchor to it
                            openConfirmPortal(cid, e.currentTarget);
                          }}
                          disabled={deletingId === cid}
                          aria-disabled={deletingId === cid}
                          style={{ background: "#fff", border: "1px solid #ffe8ea", padding: 8, borderRadius: 8, cursor: "pointer", color: "#ef4444" }}
                        >
                          <Trash2 size={14} />
                        </button>
                      )}
                    </div>
                  </div>
                </div>

                {/* inline success badge */}
                {isSuccess && (
                  <div
                    style={{
                      position: "absolute",
                      right: 12,
                      top: 12,
                      background: "#10B981",
                      color: "#fff",
                      padding: "6px 10px",
                      borderRadius: 999,
                      fontWeight: 700,
                      fontSize: 12,
                      boxShadow: "0 6px 16px rgba(16,185,129,0.16)",
                    }}
                  >
                    {successMsg || "Updated"}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* portal confirm dialog (rendered to document.body) */}
      {confirmPortal &&
        createPortal(
          <div
            id="comment-confirm-portal"
            role="dialog"
            aria-modal="true"
            style={{
              position: "fixed",
              top: confirmPortal.top,
              left: confirmPortal.left,
              width: confirmPortal.width,
              zIndex: 1200,
              pointerEvents: "auto",
              transition: "opacity .12s ease, transform .12s ease",
            }}
          >
            {/* pointer */}
            <div
              style={{
                position: "absolute",
                left: confirmPortal.pointerLeft - 9,
                top: confirmPortal.vertical === "below" ? -10 : confirmPortal.height,
                width: 18,
                height: 10,
                overflow: "visible",
                filter: "drop-shadow(0 6px 12px rgba(2,6,23,0.06))",
              }}
            >
              <svg width="18" height="10" viewBox="0 0 18 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                {confirmPortal.vertical === "below" ? (
                  <path d="M0 10L9 0L18 10H0Z" fill="#fff" stroke="#eef2f7" />
                ) : (
                  <path d="M0 0L9 10L18 0H0Z" fill="#fff" stroke="#eef2f7" />
                )}
              </svg>
            </div>

            <div
              style={{
                background: "#fff",
                borderRadius: 10,
                boxShadow: "0 12px 40px rgba(2,6,23,0.12)",
                padding: 12,
                border: "1px solid #eef2f7",
                height: confirmPortal.height,
                display: "flex",
                flexDirection: "column",
                justifyContent: "space-between",
              }}
            >
              <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                <div style={{ width:36, height:36, borderRadius:8, background:"#fff6f7", display:"flex", alignItems:"center", justifyContent:"center", border:"1px solid #ffdde0", color:"#ef4444" }}>
                  <Trash2 size={16} />
                </div>
                <div>
                  <div style={{ fontWeight: 800, fontSize: 14, color: "#111827" }}>Delete comment</div>
                  <div style={{ color: "#6b7280", fontSize: 13, marginTop: 4 }}>This action cannot be undone. Are you sure?</div>
                </div>
              </div>

              <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
                <button onClick={() => setConfirmPortal(null)} style={{ padding: "8px 12px", borderRadius: 8, border: "1px solid #e6e9ef", background: "#fff", cursor: "pointer" }}>
                  Cancel
                </button>
                <button onClick={() => doDelete(confirmPortal.commentId)} disabled={deletingId === confirmPortal.commentId} style={{ padding: "8px 12px", borderRadius: 8, border: "none", background: "#ef4444", color: "#fff", fontWeight: 800 }}>
                  {deletingId === confirmPortal.commentId ? "Deleting..." : "Delete"}
                </button>
              </div>
            </div>
          </div>,
          document.body
        )}

      <div className="comment-input" style={{ marginTop: 16, display: "flex", gap: 8 }}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Enter your comment..."
          disabled={saving}
          onKeyDown={(e) => {
            if (e.key === "Enter") handleAdd();
          }}
          style={{ flex: 1, padding: 10, borderRadius: 8, border: "1px solid #e6e9ef" }}
        />
        <button
          className="btn-primary"
          onClick={handleAdd}
          disabled={saving}
          style={{ padding: "10px 14px", borderRadius: 8, background: "#ef4444", color: "#fff", border: "none", fontWeight: 700 }}
        >
          {saving ? "Saving..." : "Add Comment"}
        </button>
      </div>
    </section>
  );
}