import React, { useState, useEffect } from "react";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";
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
  const [isNavigating, setIsNavigating] = useState(false);

  // Add fade-in effect when component mounts
  useEffect(() => {
    document.body.style.opacity = '1';
    document.body.style.transition = 'opacity 0.3s ease';
    return () => {
      document.body.style.opacity = '';
      document.body.style.transition = '';
    };
  }, []);

  const order = {
    id: "P001",
    patient: {
      name: "Nguyễn Văn A",
      gender: "Male",
      phone: "0234 654 769",
      address: "123 Main Street, City, State 12345",
      dob: "29/06/1997",
      age: 45,
      email: "nguyenvana@gmail.com",
      citizenId: "0124 456 789",
    },
    meta: {
      created: "29/05/2025",
      createdBy: "John. Smith",
      runDate: "30/05/2025",
      runBy: "Selena Gomez",
      reviewedDate: "01/06/2025",
      reviewedBy: "Justitia",
      status: "Completed",
    },
    tests: [
      { name: "CBC", result: "6.810^3/µL", ref: "4.0-10.0", status: "Normal", flag: "" },
      { name: "Hemoglobin", result: "12.5g/dL", ref: "13.5-16.5", status: "Abnormal", flag: "Low" },
      { name: "Glucose", result: "180mg/dL", ref: "70-100", status: "Critical", flag: "High" },
      { name: "Creatinine", result: "1.1mg/dL", ref: "0.7-1.3", status: "Normal", flag: "" },
    ],
  };

  return (
    <div className="dto-page">
      {isNavigating && <Loading />}
      <div className="dto-page-header">
        <div className="relative">
          <div className="absolute -left-1 -top-1 w-10 h-10 bg-red-50 rounded-full"></div>
          <button 
            onClick={() => {
              setIsNavigating(true);
              // Add fade out effect
              document.body.style.opacity = '0';
              document.body.style.transition = 'opacity 0.3s ease';
              
              setTimeout(() => {
                // Store the scroll position we want in localStorage
                localStorage.setItem('scrollToTable', 'true');
                navigate('/');
              }, 200);
            }}
            className="relative z-10 p-2 bg-red-500 text-white rounded-full hover:bg-red-600 transition-all duration-300 transform hover:scale-105 active:scale-95"
          >
            <ArrowLeft size={20} />
          </button>
        </div>

        <div>
          <h1 className="text-[28px] font-bold text-[#f65f63] tracking-[0.35em] leading-tight">TEST ORDER DETAIL</h1>
          <div className="text-gray-600 text-sm">
            ORDER ID: <span className="font-medium">{order.id}</span>
          </div>
        </div>

        <div className="dto-header-actions">
          <button className="btn-outline">Edit Order</button>
          <button className="btn-outline">Print Result</button>
        </div>
      </div>

      <div className="dto-grid">
        <div className="dto-left-col">
          <PatientInfo patient={order.patient} />
          <TestResult tests={order.tests} />
          <Comments />
        </div>

        <div className="dto-right-col">
          <OrderMeta meta={order.meta} />
          <QuickActions />
          <StatusChart />
        </div>
      </div>
    </div>
  );
}