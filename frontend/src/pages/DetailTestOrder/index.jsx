import React from "react";
import { ArrowLeft } from "lucide-react";
import PatientInfo from "./components/PatientInfo";
import OrderMeta from "./components/OrderMeta";
import TestResult from "./components/TestResult";
import Comments from "./components/Comments";
import QuickActions from "./components/QuickActions";
import StatusChart from "./components/StatusChart";
import "./DetailTestOrder.css";

export default function DetailTestOrder() {
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
      <div className="dto-page-header">
        <button className="dto-back">
          <ArrowLeft size={16} />
        </button>

        <div>
          <h1 className="dto-title">TEST ORDER DETAIL</h1>
          <div className="order-id">
            ORDER ID: <b>{order.id}</b>
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