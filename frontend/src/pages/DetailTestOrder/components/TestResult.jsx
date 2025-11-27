import React, { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { ClipboardList, Loader2 } from "lucide-react";
import { useParams } from "react-router-dom";
import axios from "../../../api/axios";
import { showToast } from "../../../components/Toast";
import "../DetailTestOrder.css";

function getFlagClass(flag) {
  // Normalize and map allowed flag tokens to CSS classes
  if (!flag && flag !== 0) return "dto-flag-default";
  const f = String(flag).toUpperCase().trim();
  const allowed = new Set(["N", "H", "L", "A", "AA", "VS"]);
  if (allowed.has(f)) return `dto-flag-${f}`;
  // fallback: if flag is single-letter numeric or unknown, show default
  return "dto-flag-default";
}

export default function TestResult({ tests, onUpdate }) {
  // helper to robustly extract parameter array from different possible server shapes
  function extractParameters(src) {
    if (!src) return [];
    // try common locations for testResults
    let tr = src.testResults ?? src.result?.testResults ?? src;
    // if tr is a string, maybe server stored JSON string
    if (typeof tr === "string") {
      try {
        tr = JSON.parse(tr);
      } catch {
        // leave as-is
      }
    }

    // tr might already be the array of parameters
    if (Array.isArray(tr)) return tr;

    // common keys for parameter list
    let params = tr?.testResultParameter ?? tr?.testResultParameters ?? tr?.parameters ?? tr;

    if (!params) return [];

    // if params is a string, try parse
    if (typeof params === "string") {
      try {
        params = JSON.parse(params);
      } catch {
        // fallback: return empty
        return [];
      }
    }

    // if it's an object (single param), wrap into array
    if (!Array.isArray(params) && typeof params === "object") return [params];
    if (Array.isArray(params)) return params;
    return [];
  }

  const { id } = useParams();
  
  // local copy of parameters so we can update after POST without needing parent update
  const [parameters, setParameters] = useState(() => extractParameters(tests));
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [hl7Text, setHl7Text] = useState("");
  const [posting, setPosting] = useState(false);
  const [postError, setPostError] = useState(null);
  const [isResyncing, setIsResyncing] = useState(false);

  // keep local parameters in sync when parent tests prop changes
  useEffect(() => {
    setParameters(extractParameters(tests));
  }, [tests]);

  // determine whether server already has test results (use normalized extraction)
  const serverParams = extractParameters(tests);
  const hasServerResults = Array.isArray(serverParams) && serverParams.length > 0;
  const hasLocalParams = Array.isArray(parameters) && parameters.length > 0;
  const disableNew = hasServerResults || hasLocalParams;

  // handlers for modal
  const openModal = () => {
    setHl7Text("");
    setPostError(null);
    setIsModalOpen(true);
  };
  const closeModal = () => {
    setIsModalOpen(false);
    setHl7Text("");
    setPostError(null);
  };

  // prevent background scroll when modal open and ensure overlay covers viewport
  useEffect(() => {
    if (typeof window !== "undefined" && window.document && window.document.body) {
      if (isModalOpen) document.body.style.overflow = "hidden";
      else document.body.style.overflow = "";
    }
    return () => {
      if (typeof window !== "undefined" && window.document && window.document.body) {
        document.body.style.overflow = "";
      }
    };
  }, [isModalOpen]);

  async function submitHl7() {
    if (posting) return; // guard double-submit
    if (!hl7Text || !hl7Text.trim()) {
      setPostError("HL7 message is empty");
      return;
    }
    setPosting(true);
    setPostError(null);
    try {
      const res = await fetch("http://localhost:6868/api/test-results/hl7", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: hl7Text,
      });
      if (!res.ok) throw new Error(`Server responded ${res.status}`);
      const data = await res.json();
      const newParams = data?.result?.testResultParameter || [];
      setParameters(newParams);
      
      // inform parent page that test results changed
      try {
        if (typeof onUpdate === "function") {
          // pass the whole testResults object from response if available
          const testResults = data?.result || { testResultParameter: newParams };
          onUpdate({ testResults });
        }
      } catch (e) {
        console.warn("onUpdate callback failed:", e);
      }
      
      // close modal on success
      setIsModalOpen(false);
    } catch (err) {
      console.error("HL7 submit error:", err);
      setPostError(err.message || "Failed to submit HL7");
    } finally {
      setPosting(false);
    }
  }

  async function handleResync() {
    if (isResyncing) return;
    
    try {
      setIsResyncing(true);
      await axios.post(`/test-orders/${id}/resync`);
      showToast({
        type: "success",
        title: "Resync Success",
        message: "Test order resynced successfully"
      });
    } catch (error) {
      console.error("Error resyncing test order:", error);
      showToast({
        type: "error",
        title: "Resync Failed",
        message: error.response?.data?.message || error.message || "Failed to resync test order"
      });
    } finally {
      setIsResyncing(false);
    }
  }

  return (
    <section className="dto-card">
      <div className="dto-card-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <div className="dto-icon"><ClipboardList size={16} /></div>
          <h3 className="dto-title">Test Result</h3>
        </div>

        <div style={{ display: "flex", gap: 8 }}>
          <button
            onClick={handleResync}
            className="inline-flex items-center px-4 py-2 rounded-lg shadow"
            style={{
              backgroundColor: "#ef4444",
              color: "#fff",
              fontWeight: 600,
              opacity: disableNew ? 0.6 : 1,
              cursor: disableNew ? "not-allowed" : "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 8
            }}
            aria-label="Resync Test Result"
            disabled={disableNew || isResyncing}
            title={disableNew ? "Test results already exist" : "Resync test result"}
            aria-busy={isResyncing ? "true" : "false"}
          >
            {isResyncing ? (
              <>
                <Loader2 className="animate-spin" size={18} aria-hidden="true" />
                <span>Resyncing...</span>
              </>
            ) : (
              "Resync"
            )}
          </button>
          <button
            onClick={openModal}
            className="inline-flex items-center px-4 py-2 rounded-lg shadow"
            style={{
              backgroundColor: "#ef4444",
              color: "#fff",
              fontWeight: 600,
              opacity: disableNew ? 0.6 : 1,
              cursor: disableNew ? "not-allowed" : "pointer",
            }}
            aria-label="Add Test Result"
            disabled={disableNew}
            title={disableNew ? "Test results already exist" : "Add new test result"}
          >
            Add Test Result
          </button>
        </div>
      </div>

      <table className="dto-table">
        <thead>
          <tr>
            <th className="dto-th">Sequence</th>
            <th className="dto-th">Parameter Name</th>
            <th className="dto-th">Value</th>
            <th className="dto-th">Reference Range</th>
            <th className="dto-th">Flag</th>
          </tr>
        </thead>
        <tbody>
          {parameters.length === 0 ? (
            <tr><td colSpan={5} className="dto-empty">No test results</td></tr>
          ) : (
            parameters.map((param) => {
              const flagClass = getFlagClass(param.flag);
              return (
                <tr key={param.id} className="dto-row">
                  <td className="dto-td">{param.sequence}</td>
                  <td className="dto-td">{param.paramName}</td>
                  <td className="dto-td">{param.value}{param.unit ? ` ${param.unit}` : ""}</td>
                  <td className="dto-td">{param.refRange}</td>
                  <td className="dto-td flag-col">
                    <span
                      className={`dto-flag ${flagClass}`}
                      title={`Flag: ${param.flag ?? "Unknown"}`}
                      aria-label={`Flag ${param.flag ?? "Unknown"}`}
                    >
                      {param.flag}
                    </span>
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>

      {/* Modal rendered into document.body to avoid stacking/transform issues */}
      {isModalOpen && createPortal(
        <div className="modal-overlay" style={{ position: "fixed", top: 0, left: 0, width: "100vw", height: "100vh", background: "rgba(0,0,0,0.45)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 9999, padding: 20 }}>
          <div className="bg-white rounded-2xl w-full max-w-3xl p-4 md:p-6 shadow-lg mx-auto" style={{ maxHeight: '90vh', overflow: 'auto' }} role="dialog" aria-modal>
            <h3 className="text-2xl text-red-500 font-bold text-center" style={{ marginBottom: 8 }}>Send HL7 (raw)</h3>
            <p className="text-center text-sm text-gray-500 mb-6">Paste HL7 message here to create test result parameters</p>
            <textarea
              value={hl7Text}
              onChange={(e) => setHl7Text(e.target.value)}
              placeholder="Paste HL7 message here"
              style={{ width: "100%", minHeight: 220, padding: 10, borderRadius: 6, border: "1px solid #e5e7eb", fontFamily: "monospace" }}
            />

            {postError && <div style={{ color: "#e11d48", marginTop: 8 }}>{postError}</div>}

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", marginTop: 12 }}>
              <button onClick={closeModal} className="px-4 py-2 rounded-lg" style={{ background: "#f3f4f6" }}>Cancel</button>
              <button
                onClick={submitHl7}
                className="px-4 py-2 rounded-lg"
                style={{ background: "#ef4444", color: "#fff", fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 8 }}
                disabled={posting}
                title={posting ? "Sending HL7..." : "Send HL7"}
              >
                {posting ? (
                  <>
                    <span aria-hidden>⏳</span>
                    <span>Sending...</span>
                  </>
                ) : (
                  <span>Send HL7</span>
                )}
              </button>
            </div>
          </div>
        </div>, document.body
      )}
    </section>
  );
}
