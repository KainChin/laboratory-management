import React, { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { ClipboardList } from "lucide-react";
import axios from "../../../api/axios";
import "../DetailTestOrder.css";
import { showToast } from "../../../components/Toast";
import { getFlagColor } from "../../../utils/flagUtils";

function getFlagClass(flag) {
  if (!flag) return "dto-flag-default";
  const f = String(flag).toUpperCase();
  // Return class based on exact flag value to match Status Chart colors
  if (f === "H") return "dto-flag-H";
  if (f === "L") return "dto-flag-L";
  if (f === "N") return "dto-flag-N";
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

  // local copy of parameters so we can update after POST without needing parent update
  const [parameters, setParameters] = useState(() => extractParameters(tests));
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [hl7Text, setHl7Text] = useState("");
  const [posting, setPosting] = useState(false);
  const [postError, setPostError] = useState(null);

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
      const res = await axios.post("/test-results/hl7", hl7Text, {
        headers: { "Content-Type": "text/plain" },
      });
      const data = res.data;
      const resultPayload = data?.result ?? {};
      const extractedParams = extractParameters(resultPayload);
      const newParams = extractedParams.length ? extractedParams : [];

      // keep local parameters in sync
      setParameters(newParams);

      // inform parent page that test results / status changed
      try {
        if (typeof onUpdate === "function") {
          let nextTestResults =
            resultPayload?.testResults ??
            (Array.isArray(resultPayload)
              ? { testResultParameter: resultPayload }
              : resultPayload);

          if (
            !nextTestResults ||
            typeof nextTestResults !== "object" ||
            !nextTestResults.testResultParameter
          ) {
            nextTestResults = { testResultParameter: newParams };
          }

          const nextStatus =
            resultPayload?.status ?? resultPayload?.testOrderStatus ?? resultPayload?.testStatus;

          onUpdate({
            testResults: nextTestResults,
            status: nextStatus,
          });
        }
      } catch (e) {
        console.warn("onUpdate callback failed:", e);
      }

      showToast({ type: "success", title: "HL7 Imported", message: data?.message || "Retrieved test result successfully" });

      // close modal on success
      setIsModalOpen(false);
    } catch (err) {
      console.error("HL7 submit error:", err);
      
      // Extract detailed error message from response
      let errorMessage = "Failed to submit HL7";
      if (err.response?.data?.message) {
        // Handle both array and string message formats
        const msg = err.response.data.message;
        errorMessage = Array.isArray(msg) ? msg.join(", ") : msg;
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      setPostError(errorMessage);
    } finally {
      setPosting(false);
    }
  }

  return (
    <section className="dto-card">
      <div className="dto-card-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <div className="dto-icon"><ClipboardList size={24} /></div>
          <h3 className="dto-title" style={{ color: '#FF5A5A', fontSize: 18, fontWeight: 800 }}>Test Result</h3>
        </div>

        <div>
          <button
            onClick={openModal}
            className="inline-flex items-center px-4 py-2 rounded-lg shadow"
            style={{
              backgroundColor: "#FF5A5A",
              color: "#fff",
              fontWeight: 600,
              opacity: disableNew ? 0.6 : 1,
              cursor: disableNew ? "not-allowed" : "pointer",
              transition: "background-color 0.3s ease",
            }}
            onMouseEnter={(e) => !disableNew && (e.target.style.backgroundColor = "#FF3A3A")}
            onMouseLeave={(e) => !disableNew && (e.target.style.backgroundColor = "#FF5A5A")}
            aria-label="Add Test Result"
            disabled={disableNew}
            title={disableNew ? "Test results already exist" : "Add new test result"}
            aria-disabled={disableNew ? "true" : "false"}
          >
            Add Test Result
          </button>
        </div>
      </div>

      <table className="dto-table" role="table" aria-label="Test Results Table">
        <thead>
          <tr role="row">
            <th className="dto-th" role="columnheader" aria-label="Sequence Number">Sequence</th>
            <th className="dto-th" role="columnheader" aria-label="Parameter Name">Parameter Name</th>
            <th className="dto-th" role="columnheader" aria-label="Test Result Value">Value</th>
            <th className="dto-th" role="columnheader" aria-label="Reference Range">Reference Range</th>
            <th className="dto-th" role="columnheader" aria-label="Result Flag">Flag</th>
          </tr>
        </thead>
        <tbody role="rowgroup">
          {parameters.length === 0 ? (
            <tr role="row"><td colSpan={5} className="dto-empty" role="cell" aria-label="No test results available">No test results</td></tr>
          ) : (
            parameters.map((param) => {
              const flagClass = getFlagClass(param.flag);
              const flagColor = getFlagColor(param.flag);
              return (
                <tr key={param.id} className="dto-row" role="row">
                  <td className="dto-td">{param.sequence}</td>
                  <td className="dto-td">{param.paramName}</td>
                  <td className="dto-td">{param.value}{param.unit ? ` ${param.unit}` : ""}</td>
                  <td className="dto-td">{param.refRange}</td>
                  <td className="dto-td flag-col">
                    <span
                      className={`dto-flag ${flagClass}`}
                      style={{ color: flagColor }}
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
        <div
          className="modal-overlay"
          style={{ position: "fixed", top: 0, left: 0, width: "100vw", height: "100vh", background: "rgba(0,0,0,0.45)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 9999, padding: 20, animation: "fadeInOverlay 0.3s ease-out" }}
          onClick={() => setIsModalOpen(false)}
          role="dialog"
          aria-modal="true"
          aria-labelledby="hl7-modal-title"
          aria-describedby="hl7-description"
        >
          <div
            className="bg-white rounded-2xl w-full max-w-3xl p-4 md:p-6 shadow-lg mx-auto"
            style={{ maxHeight: '90vh', overflow: 'auto', animation: 'slideUp 0.4s ease-out' }}
            role="dialog" aria-modal
            onClick={(e) => e.stopPropagation()}
          >
            <h3 id="hl7-modal-title" className="text-2xl text-red-500 font-bold text-center" style={{ marginBottom: 8 }}>Send HL7 (raw)</h3>
            <p id="hl7-description" className="text-center text-sm text-gray-500 mb-6">Paste HL7 message here to create test result parameters</p>
            <textarea
              value={hl7Text}
              onChange={(e) => setHl7Text(e.target.value)}
              placeholder="Paste HL7 message here"
              style={{ width: "100%", minHeight: 220, padding: 10, borderRadius: 6, border: "1px solid #CCC", fontFamily: "monospace" }}
              onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
              onBlur={(e) => e.target.style.borderColor = "#CCC"}
              id="hl7-message-input"
              aria-label="HL7 Message Input"
              aria-required="true"
              aria-describedby="hl7-description"
              aria-invalid={postError ? "true" : "false"}
            />

            {postError && <div role="alert" aria-live="assertive" style={{ color: "#FF0000", marginTop: 8 }}>{postError}</div>}

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", marginTop: 12 }}>
              <button 
                onClick={closeModal} 
                className="px-4 py-2 rounded-lg" 
                style={{ background: "#f3f4f6", border: "1px solid #CCC" }}
                aria-label="Cancel and close modal"
              >
                Cancel
              </button>
              <button
                onClick={submitHl7}
                className="px-4 py-2 rounded-lg"
                style={{ background: "#ef4444", color: "#fff", fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 8, border: "1px solid #CCC" }}
                disabled={posting}
                title={posting ? "Sending HL7..." : "Send HL7"}
                aria-label={posting ? "Sending HL7 message" : "Send HL7 message"}
                aria-busy={posting ? "true" : "false"}
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
