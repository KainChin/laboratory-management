import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams, Routes, Route } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import Loading from "../../components/Loading";
import TestOrders from "../TestOrders"; // Import TestOrders component
import PatientInfo from "./PatientInfo"; // Import PatientInfo component
import OrderMeta from "./OrderMeta"; // Import OrderMeta component

// ---------- internal TestResults component (no new file) ----------
function _severityBadge(status, flag) {
  const f = (flag || "").toString().toUpperCase();
  const s = (status || "").toString().toUpperCase();

  if (f.includes("HIGH") || f.includes("LOW") || f.includes("ABNORMAL")) {
    return { label: "Critical", classes: "bg-rose-100 text-rose-700" };
  }
  if (s === "PENDING") return { label: "Pending", classes: "bg-blue-100 text-blue-700" };
  if (s === "VALIDATED") return { label: "Abnormal", classes: "bg-amber-100 text-amber-700" };
  if (s === "APPROVED") return { label: "Normal", classes: "bg-emerald-100 text-emerald-700" };
  return { label: s || "-", classes: "bg-gray-100 text-gray-800" };
}

function _flagBadge(flag) {
  const f = (flag || "").toString().toUpperCase();
  if (!f) return { label: "-", classes: "bg-gray-100 text-gray-800" };
  return { label: f, classes: "bg-amber-100 text-amber-800" };
}

function TestResults({ results = [] }) {
  return (
    <section className="bg-white rounded-lg border p-6 shadow-sm">
      <div className="flex items-center mb-4">
        <div className="p-2 rounded-full bg-rose-50 mr-3">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="#f65f63" strokeWidth="1.5" />
          </svg>
        </div>
        <h3 className="text-xl font-semibold text-rose-600">Test Result</h3>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-gray-500 text-left border-y">
              <th className="py-4 px-4">Test Name</th>
              <th className="py-4 px-4">Result</th>
              <th className="py-4 px-4">Reference Range</th>
              <th className="py-4 px-4">Status</th>
              <th className="py-4 px-4">Flag</th>
            </tr>
          </thead>

          <tbody>
            {results.length === 0 ? (
              <tr>
                <td colSpan="5" className="py-6 text-center text-gray-400">
                  No test results
                </td>
              </tr>
            ) : (
              results.map((r) => {
                const sev = _severityBadge(r.status, r.flag);
                const fb = _flagBadge(r.flag);
                const value = r.value !== undefined && r.unit ? `${r.value} ${r.unit}` : r.value ?? "-";
                const refRange =
                  r.referenceMin !== undefined && r.referenceMax !== undefined
                    ? `${r.referenceMin} - ${r.referenceMax}`
                    : r.referenceMin ?? r.referenceMax ?? "-";
                return (
                  <tr key={r.resultId} className="border-t last:border-b">
                    <td className="py-4 px-4 align-top">
                      <div className="text-gray-700 font-medium">{r.parameter ?? "-"}</div>
                    </td>

                    <td className="py-4 px-4 align-top text-gray-700 font-medium">{value}</td>

                    <td className="py-4 px-4 align-top text-gray-500">{refRange}</td>

                    <td className="py-4 px-4 align-top">
                      <span
                        className={`${sev.classes} inline-block px-4 py-1 rounded-full text-sm font-semibold`}
                      >
                        {sev.label}
                      </span>
                    </td>

                    <td className="py-4 px-4 align-top">
                      <span
                        className={`${fb.classes} inline-block px-3 py-1 rounded-full text-sm font-medium`}
                      >
                        {fb.label}
                      </span>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
// ---------- end TestResults component ----------

export default function DetailTestOrder(props) {
  const navigate = useNavigate();
  const [isNavigating, setIsNavigating] = useState(false);
  const { id } = useParams();
  const [testOrder, setTestOrder] = useState(null);
  const [testResults, setTestResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [loadingBack, setLoadingBack] = useState(false);

  // Fetch order data from the API and normalize
  useEffect(() => {
    let mounted = true;

    async function fetchData() {
      try {
        // adjust URL if different; keep same host/port you use
        const id = props?.match?.params?.id ?? props?.id ?? (window.location.pathname.split("/").pop());
        const res = await fetch(`http://localhost:6868/api/test-orders/${id}`);
        const payload = await res.json();
        console.log("[DetailTestOrder] raw payload:", payload);

        const src = payload?.result ?? payload ?? {};
        const normalized = {
          ...src,
          patientName: src.patientName ?? src.name ?? src.patient?.name ?? "",
          dateOfBirth: src.dateOfBirth ?? src.dob ?? src.patient?.dateOfBirth ?? "",
        };

        if (!mounted) return;
        setTestOrder(normalized);
        setTestResults(Array.isArray(normalized.testResults) ? normalized.testResults : []);
        setLoading(false);
      } catch (err) {
        console.error(err);
        if (!mounted) return;
        setError(err.message || "Failed to fetch");
        setLoading(false);
      }
    }

    fetchData();
    return () => {
      mounted = false;
    };
  }, [props]);

  // Add fade-in effect when component mounts
  useEffect(() => {
    document.body.style.opacity = "1";
    document.body.style.transition = "opacity 0.3s ease";
    return () => {
      document.body.style.opacity = "";
      document.body.style.transition = "";
    };
  }, []);

  function handleBackClick(e) {
    e?.preventDefault?.();
    // show loader then navigate back (gives smooth UX)
    setLoadingBack(true);
    // small delay so user sees animation; adjust 300-600ms as desired
    setTimeout(() => {
      // navigate back - change to router navigate if you use react-router
      if (window.history.length > 1) window.history.back();
      else window.location.href = "/"; // fallback
    }, 350);
  }

  return (
    <div className="min-h-screen bg-gray-50 dto-page">
      {/* overlay loader */}
      {loadingBack && (
        <div className="page-loader-overlay" aria-hidden>
          <div className="loader-spinner" />
        </div>
      )}

      <div className="max-w-full mx-auto px-6 py-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-6">
            <div className="relative">
              <div className="absolute -left-2 -top-2 w-12 h-12 bg-red-50 rounded-full"></div>
              <button
                onClick={handleBackClick}
                className="relative z-10 p-2 bg-red-500 text-white rounded-full hover:bg-red-600 transition-all duration-300 transform hover:scale-105 active:scale-95"
                title="Back"
              >
                <ArrowLeft size={24} />
              </button>
            </div>
            <div>
              <h1 className="text-[28px] font-bold text-[#f65f63] tracking-wide">
                TEST ORDER DETAIL
              </h1>
              <div className="text-gray-500 mt-1">ORDER ID: {id}</div>
            </div>
          </div>
          <div className="text-sm text-gray-600">Welcome, [Lab User]</div>
        </div>

        <div className="mt-6">
          <div className="bg-white rounded-2xl p-6 shadow-sm">
            <h1 className="text-xl font-semibold mb-2">Test Order Detail</h1>
            {loading ? (
              <div className="text-center py-8">Loading...</div>
            ) : error ? (
              <div className="text-center py-8 text-red-500">{error}</div>
            ) : testOrder ? (
              <div className="grid grid-cols-3 gap-6">
                <div className="col-span-2 bg-gray-50 p-6 rounded-lg">
                  {/* left: Patient info */}
                  {/* pass whole normalized object so PatientInfo can read patientName / name / dob */}
                  <PatientInfo patient={testOrder} />
                  {/* other left children */}
                </div>

                <div>
                  {/* PHẢI truyền testOrder (normalized) */}
                  <OrderMeta order={testOrder} />
                </div>
              </div>
            ) : (
              <div className="text-center py-8">No test order found</div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-3 gap-6">
          {/* left columns ... */}

          {/* Test Results full width row */}
          <div className="col-span-3 mt-6">
            <TestResults results={testResults} />
          </div>
        </div>
      </div>

      <Routes>
        <Route path="/" element={<TestOrders />} />
        <Route path="/test-orders" element={<TestOrders />} />
      </Routes>
    </div>
  );
}
