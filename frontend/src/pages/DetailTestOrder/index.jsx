import React, { useState, useEffect } from "react";
import { ArrowLeft } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import html2pdf from "html2pdf.js";
import PatientInfo from "./components/PatientInfo";
import OrderMeta from "./components/OrderMeta";
import TestResult from "./components/TestResult";
import Comments from "./components/Comments";
import QuickActions from "./components/QuickActions";
import FlagChart from "./components/FlagChart";
import Loading from "../../components/Loading";
import "./DetailTestOrder.css";

export default function DetailTestOrder() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [isNavigating, setIsNavigating] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [testResults, setTestResults] = useState(null);

  // Fetch order data from the API (Giữ nguyên)
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
          dateOfBirth:
            src.dateOfBirth ?? src.dob ?? src.patient?.dateOfBirth ?? "",
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

  // Add fade-in effect (Giữ nguyên)
  useEffect(() => {
    document.body.style.opacity = "1";
    document.body.style.transition = "opacity 0.3s ease";
    return () => {
      document.body.style.opacity = "";
      document.body.style.transition = "";
    };
  }, []);

  // Handle navigation back (Giữ nguyên)
  const handleGoBack = () => {
    setIsNavigating(true);
    document.body.style.opacity = "0";
    document.body.style.transition = "opacity 0.3s ease";

    setTimeout(() => {
      localStorage.setItem("scrollToTable", "true");
      navigate("/");
    }, 200);
  };

  // ===================================================================
  // ++ HÀM EXPORT PDF ĐÃ ĐƯỢC CẬP NHẬT ++
  // ===================================================================
  const handleExportPDF = async () => {
    const element = document.querySelector(".dto-page"); // Chúng ta sẽ in toàn bộ trang
    if (!element) return;

    setIsExporting(true);

    // 1. Định nghĩa CSS để fix layout in
    const printStyles = `
      /* Ẩn header và các nút */
      .printing .dto-page-header {
        display: none !important;
      }
      
      /* PHÁ VỠ LAYOUT GRID (Quan trọng nhất) */
      .printing .dto-grid {
        display: block !important; /* Chuyển từ grid thành block */
        grid-template-columns: 1fr !important;
      }
      .printing .dto-left-col,
      .printing .dto-right-col {
        display: block !important; /* Ép các cột xếp chồng lên nhau */
        width: 100% !important;
        grid-column: auto !important;
      }
      
      /* ẨN CÁC COMPONENT BẠN KHÔNG MUỐN */
      .printing .no-print {
        display: none !important;
      }
      
      /* Dọn dẹp giao diện (bỏ bóng, nền, v.v.) */
      .printing .dto-page {
        background: #ffffff !important;
        padding: 0 !important;
        margin: 0 !important;
      }
      .printing .dto-left-col > *,
      .printing .dto-right-col > * {
        box-shadow: none !important;
        border: none !important;
        border-radius: 0 !important;
        padding: 12px !important; /* Đồng bộ padding */
      }
    `;

    // 2. Tạo và chèn thẻ <style> vào <head>
    const styleId = "pdf-print-styles";
    let style = document.getElementById(styleId);
    if (!style) {
      style = document.createElement("style");
      style.id = styleId;
      style.type = "text/css";
      style.innerHTML = printStyles;
      document.head.appendChild(style);
    }

    // 3. Thêm class 'printing'
    element.classList.add("printing");

    // 4. Cấu hình PDF
    const options = {
      margin: 10,
      filename: `test-order-${order.testOrderId}.pdf`,
      image: { type: "jpeg", quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true, logging: false },
      jsPDF: { unit: "mm", format: "a4", orientation: "portrait" },
    };

    // 5. Đợi 50ms để CSS được áp dụng, sau đó mới in
    setTimeout(async () => {
      try {
        await html2pdf().set(options).from(element).save();
      } catch (error) {
        console.error("Error exporting PDF:", error);
      } finally {
        // 6. Dọn dẹp sau khi in
        element.classList.remove("printing");
        setIsExporting(false);
        // Bạn có thể xóa thẻ style nếu muốn, nhưng để lại cũng không sao
        // const styleToRemove = document.getElementById(styleId);
        // if (styleToRemove) document.head.removeChild(styleToRemove);
      }
    }, 50); // Đợi 50ms để fix lỗi race condition
  };
  // ===================================================================
  // ++ HẾT HÀM MỚI ++
  // ===================================================================

  // Loading state or error handling (Giữ nguyên)
  if (loading) {
    return <Loading />;
  }
  if (error) {
    return <div className="text-center py-8 text-red-500">{error}</div>;
  }
  if (!order) {
    return <div className="text-center py-8">No test order found</div>;
  }

  // RETURN JSX (ĐÃ SỬA)
  return (
    // ++ SỬA: BỎ ref={contentRef} VÌ CHÚNG TA DÙNG querySelector
    <div className="dto-page">
      {isNavigating && <Loading />}

      {/* ++ SỬA: Thêm class "no-print" để ẩn toàn bộ header khi in */}
      <div className="dto-page-header no-print">
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
          <h1 className="text-[28px] font-bold text-[#f65f63] tracking-[0.35em] leading-tight">
            TEST ORDER DETAIL
          </h1>
          <div className="text-gray-600 text-sm">
            ORDER ID: <span className="font-medium">{order.testOrderId}</span>
          </div>
        </div>

        <div className="dto-header-actions">
          <button className="btn-outline">Edit Order</button>

          {/* ++ SỬA: Cập nhật lại nút Export PDF */}
          <button
            onClick={handleExportPDF}
            disabled={isExporting} // Vô hiệu hóa nút khi đang in
            className="btn-outline"
          >
            {isExporting ? "Generating..." : "Export PDF"}
          </button>
        </div>
      </div>

      <div className="dto-grid">
        <div className="dto-left-col">
          <PatientInfo patient={order} />
          <TestResult 
            tests={order.testResults} 
            onUpdate={(newResults) => {
              setTestResults(newResults.testResults);
              setOrder({ ...order, testResults: newResults.testResults });
            }}
          />

          {/* ++ SỬA: Bọc Comments trong div "no-print" để ẩn */}
          <div className="no-print">
            <Comments comments={order.comments} />
          </div>
        </div>

        <div className="dto-right-col">
          <OrderMeta order={order} />

          {/* ++ SỬA: Bọc QuickActions trong div "no-print" để ẩn */}
          <div className="no-print">
            <QuickActions
              status={order.status}
              onStatusChange={(newStatus) => {
                setOrder({ ...order, status: newStatus });
              }}
            />
          </div>

          {/* Flag Chart component */}
          <FlagChart 
            orderId={id} 
            testResults={testResults} 
          />
        </div>
      </div>
    </div>
  );
}
