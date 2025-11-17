import React, { useEffect, useState, useRef, useMemo } from "react";
import { Link, useNavigate, useParams, Routes, Route } from "react-router-dom";
import { ArrowLeft, FileDown } from "lucide-react";
import html2pdf from 'html2pdf.js';
import Loading from "../../components/Loading";
import TestOrders from "../TestOrders"; // Import TestOrders component
import PatientInfo from "./components/PatientInfo"; // Import PatientInfo component
import OrderMeta from "./components/OrderMeta"; // Import OrderMeta component
import TestResult from "./components/TestResult"; // external TestResult component

export default function DetailTestOrder(props) {
  const navigate = useNavigate();
  const [isNavigating, setIsNavigating] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const { id } = useParams();
  const contentRef = useRef(null);

  const handleExportPDF = async () => {
    try {
      setIsExporting(true);

      // Get the content element
      const element = contentRef.current;
      if (!element) return;

      // Configure pdf options
      const options = {
        margin: 10,
        filename: `test-order-${id}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: {
          scale: 2,
          useCORS: true,
          logging: false
        },
        jsPDF: {
          unit: 'mm',
          format: 'a4',
          orientation: 'portrait'
        }
      };

      // Before export: Add print class
      element.classList.add('printing');

      // Generate PDF
      await html2pdf().set(options).from(element).save();

      // After export: Remove print class
      element.classList.remove('printing');
    } catch (error) {
      console.error('Error generating PDF:', error);
    } finally {
      setIsExporting(false);
    }
  };
  const [testOrder, setTestOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [loadingBack, setLoadingBack] = useState(false);
  const waitingStatuses = useMemo(() => ["PENDING"], []);

  // Fetch order data from the API and normalize
  useEffect(() => {
    let mounted = true;

    async function fetchData() {
      try {
        // adjust URL if different; keep same host/port you use
        const id = props?.match?.params?.id ?? props?.id ?? (window.location.pathname.split("/").pop());
        const res = await fetch(`/api/test-orders/${id}`);
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

  // Auto-poll while waiting for instrument service
  useEffect(() => {
    if (!testOrder) return;
    const status = (testOrder.status || "").toString().toUpperCase();
    if (!waitingStatuses.includes(status)) return;
    const orderId = id;
    const timer = setInterval(async () => {
      try {
        const res = await fetch(`/api/test-orders/${orderId}`);
        if (!res.ok) return;
        const payload = await res.json();
        const src = payload?.result ?? payload ?? {};
        const normalized = {
          ...src,
          patientName: src.patientName ?? src.name ?? src.patient?.name ?? "",
          dateOfBirth: src.dateOfBirth ?? src.dob ?? src.patient?.dateOfBirth ?? "",
        };
        setTestOrder(normalized);
      } catch (err) { console.error(err); }
    }, 5000);
    return () => clearInterval(timer);
  }, [id, testOrder]);

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
          <div className="flex items-center gap-4">
            <div className="text-sm text-gray-600 mr-4">Welcome, [Lab User]</div>
            {!loading && !error && testOrder && (
              <>
                <button
                  onClick={handleExportPDF}
                  disabled={isExporting}
                  className="flex items-center gap-2 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <FileDown size={24} />
                  {isExporting ? 'Generating PDF...' : 'Export PDF'}
                </button>
              </>
            )}
          </div>
        </div>

        <div className="mt-6">
          <div className="bg-white rounded-2xl p-6 shadow-sm" ref={contentRef}>
            <h1 className="text-xl font-semibold mb-2">Test Order Detail</h1>
            {testOrder && waitingStatuses.includes((testOrder.status || "").toString().toUpperCase()) && (
              <div className="mb-4 rounded border border-blue-200 bg-blue-50 text-blue-700 px-4 py-3">
                Waiting for Instrument Service response. System will automatically update when results are available...
              </div>
            )}
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

        <div className="grid grid-cols-3 gap-6" ref={contentRef}>
          {/* Test Results full width row */}
          <div className="col-span-3 mt-6">
            <TestResult
              tests={testOrder}
              onUpdate={async (updated) => {
                // try to refresh authoritative testOrder from server after HL7 POST
                try {
                  const orderId = id ?? (window.location.pathname.split("/").pop());
                  const res = await fetch(`/api/test-orders/${orderId}`);
                  if (!res.ok) throw new Error(`Fetch failed ${res.status}`);
                  const payload = await res.json();
                  const src = payload?.result ?? payload ?? {};
                  const normalized = {
                    ...src,
                    patientName: src.patientName ?? src.name ?? src.patient?.name ?? "",
                    dateOfBirth: src.dateOfBirth ?? src.dob ?? src.patient?.dateOfBirth ?? "",
                  };
                  setTestOrder(normalized);
                } catch {
                  // fallback: merge whatever was returned
                  setTestOrder((prev) => ({ ...(prev || {}), ...(updated || {}) }));
                }
              }}
            />
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
