import React, { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

export function showToast({ type = "success", title, message, duration = 3000 } = {}) {
  try {
    window.dispatchEvent(
      new CustomEvent("toast:show", {
        detail: { type, title, message, duration },
      })
    );
  } catch {}
}

export default function ToastContainer() {
  const [toasts, setToasts] = useState([]);
  const timersRef = useRef(new Map());

  function dismissToast(id) {
    try {
      const timers = timersRef.current.get(id);
      if (timers) {
        clearInterval(timers.interval);
        clearTimeout(timers.timeout);
        timersRef.current.delete(id);
      }
      setToasts((list) => list.filter((t) => t.id !== id));
    } catch {}
  }

  useEffect(() => {
    function onShow(e) {
      const { type = "success", title, message, duration = 3000 } = e.detail || {};
      const id = `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
      const now = Date.now();
      setToasts((list) => [
        ...list,
        { id, type, title, message, duration, progress: 100, createdAt: now, expiresAt: now + duration },
      ]);
    }
    window.addEventListener("toast:show", onShow);
    return () => window.removeEventListener("toast:show", onShow);
  }, []);

  // Create timers for new toasts only; keep refs stable across renders
  useEffect(() => {
    toasts.forEach((t) => {
      if (timersRef.current.has(t.id)) return;

      const timeoutDelay = Math.max(0, (t.expiresAt || (t.createdAt + (t.duration || 3000))) - Date.now());
      const timeout = setTimeout(() => {
        setToasts((list) => list.filter((it) => it.id !== t.id));
        timersRef.current.delete(t.id);
      }, timeoutDelay);

      timersRef.current.set(t.id, { timeout });
    });
  }, [toasts]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      timersRef.current.forEach(({ timeout }) => {
        clearTimeout(timeout);
      });
      timersRef.current.clear();
    };
  }, []);

  const node = (
    <div
      style={{ position: "fixed", top: 16, right: 16, zIndex: 40, display: "flex", flexDirection: "column", gap: 12, pointerEvents: "none" }}
      aria-live="polite"
      aria-atomic="true"
    >
      <style>
        {`
          @keyframes shrink {
            from { width: 100%; }
            to { width: 0%; }
          }
        `}
      </style>
      {toasts.map((t) => {
        const isSuccess = t.type === "success";
        const border = isSuccess ? "#86efac" : "#fca5a5";
        const bg = isSuccess ? "#ecfdf5" : "#fef2f2";
        const fg = isSuccess ? "#065f46" : "#7f1d1d";
        const bar = isSuccess ? "#10b981" : "#ef4444";
        return (
          <div
            key={t.id}
            style={{ width: 360, boxShadow: "0 10px 15px -3px rgba(0,0,0,0.1)", border: `1px solid ${border}`, background: bg, color: fg, borderRadius: 12, overflow: "hidden", pointerEvents: "auto", position: "relative" }}
            role="status"
          >
            <button
              onClick={(e) => {
                e.stopPropagation();
                dismissToast(t.id);
              }}
              aria-label="Dismiss notification"
              title="Close"
              style={{
                position: "absolute",
                top: 6,
                right: 6,
                width: 24,
                height: 24,
                borderRadius: 6,
                border: "1px solid rgba(0,0,0,0.08)",
                background: "rgba(255,255,255,0.8)",
                color: fg,
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
              }}
            >
              ✕
            </button>

            <div style={{ padding: 12, paddingRight: 36 }}>
              {t.title && <div style={{ fontWeight: 700, marginBottom: 4 }}>{t.title}</div>}
              {t.message && <div style={{ fontSize: 14, lineHeight: 1.4 }}>{t.message}</div>}
            </div>
            <div style={{ height: 3, background: "rgba(0,0,0,0.06)" }}>
              <div style={{ 
                height: "100%", 
                background: bar, 
                width: "100%",
                animation: `shrink ${t.duration || 3000}ms linear forwards`
              }} />
            </div>
          </div>
        );
      })}
    </div>
  );

  return createPortal(node, document.body);
}


