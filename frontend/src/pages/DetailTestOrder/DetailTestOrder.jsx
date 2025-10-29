import React, { useState, useEffect } from "react";
import { Link, useParams, useNavigate, Routes, Route } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import Loading from "../../components/Loading";
import TestOrders from "../TestOrders"; // Import TestOrders component
import PatientInfo from "./PatientInfo"; // Import PatientInfo component
import OrderMeta from "./OrderMeta"; // Import OrderMeta component

export default function DetailTestOrder() {
  const navigate = useNavigate();
  const [isNavigating, setIsNavigating] = useState(false);
  const { id } = useParams();
  const [testOrder, setTestOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchTestOrder() {
      try {
        const response = await fetch(`http://localhost:6868/api/test-orders/${id}`);
        const payload = await response.json();
        console.log("[DetailTestOrder] raw payload:", payload);

        const src = payload?.result ?? payload; // nếu parent trả cả payload hoặc chỉ result
        const normalized = {
          ...src,
          patientName: src.patientName ?? src.name ?? src.patient?.name ?? "",
          dateOfBirth: src.dateOfBirth ?? src.dob ?? src.patient?.dateOfBirth ?? "",
          name: src.patientName ?? src.name ?? "",
          dob: src.dateOfBirth ?? src.dob ?? "",
        };
        console.log("[DetailTestOrder.jsx] normalized:", normalized);
        setTestOrder(normalized);
        setLoading(false);
      } catch (err) {
        console.error(err);
        setError(err.message);
        setLoading(false);
      }
    }

    fetchTestOrder();
  }, [id]);

  // Add fade-in effect when component mounts
  useEffect(() => {
    document.body.style.opacity = "1";
    document.body.style.transition = "opacity 0.3s ease";
    return () => {
      document.body.style.opacity = "";
      document.body.style.transition = "";
    };
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      {isNavigating && <Loading />}
      <div className="max-w-full mx-auto px-6 py-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-6">
            <div className="relative">
              <div className="absolute -left-2 -top-2 w-12 h-12 bg-red-50 rounded-full"></div>
              <button
                onClick={() => {
                  setIsNavigating(true);
                  document.body.style.opacity = "0";
                  document.body.style.transition = "opacity 0.3s ease";

                  setTimeout(() => {
                    window.scrollTo({ top: 0, behavior: "smooth" });
                    setTimeout(() => {
                      navigate("/"); // Quay lại trang danh sách
                    }, 300);
                  }, 200);
                }}
                className="relative z-10 p-2 bg-red-500 text-white rounded-full hover:bg-red-600 transition-all duration-300 transform hover:scale-105 active:scale-95"
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
      </div>

      <Routes>
        <Route path="/" element={<TestOrders />} />
        <Route path="/test-orders" element={<TestOrders />} />
      </Routes>
    </div>
  );
}
