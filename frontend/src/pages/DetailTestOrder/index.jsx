import React, { useState, useEffect } from "react";
import { ArrowLeft } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import PatientInfo from "./components/PatientInfo";
import OrderMeta from "./components/OrderMeta";
import TestResult from "./components/TestResult";
import Comments from "./components/Comments";
import QuickActions from "./components/QuickActions";
import StatusChart from "./components/StatusChart";
import Loading from "../../components/Loading";
import "./DetailTestOrder.css";

export default function DetailTestOrder() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [isNavigating, setIsNavigating] = useState(false);
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch order data from the API
  useEffect(() => {
    async function fetchTestOrder() {
      try {
        const res = await fetch(`http://localhost:6868/api/test-orders/${id}`);
        const payload = await res.json();
        console.log("[DetailTestOrder index.jsx] raw payload:", payload);

        const src = payload?.result ?? payload;
        const normalized = {
          ...src,
          patientName: src.patientName ?? src.name ?? src.patient?.name ?? "",
          dateOfBirth: src.dateOfBirth ?? src.dob ?? src.patient?.dateOfBirth ?? "",
          name: src.patientName ?? src.name ?? "",
          dob: src.dateOfBirth ?? src.dob ?? "",
        };
        console.log("[DetailTestOrder index.jsx] normalized:", normalized);
        console.log("Order state (index.jsx):", normalized);
        setOrder(normalized);
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
    document.body.style.opacity = '1';
    document.body.style.transition = 'opacity 0.3s ease';
    return () => {
      document.body.style.opacity = '';
      document.body.style.transition = '';
    };
  }, []);

  // Handle navigation back
  const handleGoBack = () => {
    setIsNavigating(true);
    document.body.style.opacity = '0';
    document.body.style.transition = 'opacity 0.3s ease';

    setTimeout(() => {
      localStorage.setItem('scrollToTable', 'true');
      navigate('/');
    }, 200);
  };

  // Loading state or error handling
  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <div className="text-center py-8 text-red-500">{error}</div>;
  }

  if (!order) {
    return <div className="text-center py-8">No test order found</div>;
  }

  return (
    <div className="dto-page">
      {isNavigating && <Loading />}
      <div className="dto-page-header">
        <div className="relative">
          <div className="absolute -left-1 -top-1 w-10 h-10 bg-red-50 rounded-full"></div>
          <button
            onClick={handleGoBack}
            className="relative z-10 p-2 bg-red-500 text-white rounded-full hover:bg-red-600 transition-all duration-300 transform hover:scale-105 active:scale-95"
          >
            <ArrowLeft size={20} />
          </button>
        </div>

        <div>
          <h1 className="text-[28px] font-bold text-[#f65f63] tracking-[0.35em] leading-tight">TEST ORDER DETAIL</h1>
          <div className="text-gray-600 text-sm">
            ORDER ID: <span className="font-medium">{order.testOrderId}</span>
          </div>
        </div>

        <div className="dto-header-actions">
          <button className="btn-outline">Edit Order</button>
          <button className="btn-outline">Print Result</button>
        </div>
      </div>

      <div className="dto-grid">
        <div className="dto-left-col">
          <PatientInfo patient={order} />
          <TestResult tests={order.testResults} />
          <Comments comments={order.comments} />
        </div>

        <div className="dto-right-col">
          <OrderMeta order={order} />
          <QuickActions />
          <StatusChart status={order.status} />
        </div>
      </div>
    </div>
  );
}
