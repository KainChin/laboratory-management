import React, { useState, useEffect, useRef } from "react";
import { ArrowLeft } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import html2pdf from "html2pdf.js";
import { createPortal } from "react-dom";
import axios from "../../api/axios";
import PatientInfo from "./components/PatientInfo";
import OrderMeta from "./components/OrderMeta";
import TestResult from "./components/TestResult";
import Comments from "./components/Comments";
import QuickActions from "./components/QuickActions";
import FlagChart from "./components/FlagChart";
import Loading from "../../components/Loading";
import { showToast } from "../../components/Toast";
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
  const [isEditingPatient, setIsEditingPatient] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [pendingUpdate, setPendingUpdate] = useState(null);

  // Helper functions for date formatting (giống OrdersTable)
  // Convert yyyy-MM-dd (from date input) to dd/MM/yyyy (for backend)
  const formatDateForBackend = (dateStr) => {
    if (!dateStr) return "";
    const parts = dateStr.split("-");
    if (parts.length !== 3) return dateStr;
    const [yyyy, mm, dd] = parts;
    return `${dd}/${mm}/${yyyy}`;
  };

  // Convert dd/MM/yyyy (from backend) to yyyy-MM-dd (for date input)
  const parseBackendDateToInput = (dateStr) => {
    if (!dateStr) return "";
    const parts = dateStr.split("/");
    if (parts.length !== 3) return dateStr;
    const [dd, mm, yyyy] = parts;
    return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
  };

  // Calculate age from date of birth (dd/MM/yyyy format)
  const calculateAge = (dobStr) => {
    if (!dobStr) return "";
    const parts = dobStr.split("/");
    if (parts.length !== 3) return "";
    const [dd, mm, yyyy] = parts;
    const birthDate = new Date(yyyy, mm - 1, dd);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  };

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

  // Handle Edit Order
  const handleEditOrder = () => {
    setIsEditingPatient(true);
  };

  const handleCancelEdit = () => {
    setIsEditingPatient(false);
    setPendingUpdate(null);
  };

  const handleSavePatient = (formData) => {
    // Lưu dữ liệu tạm và hiển thị modal xác nhận
    setPendingUpdate(formData);
    setShowConfirmModal(true);
  };

  const confirmUpdate = async () => {
    if (!pendingUpdate || isUpdating) return;

    try {
      setIsUpdating(true);

      // Chuẩn bị payload theo format API yêu cầu
      const payload = {
        patientName: pendingUpdate.patientName || undefined,
        dateOfBirth: pendingUpdate.dateOfBirth ? formatDateForBackend(pendingUpdate.dateOfBirth) : undefined,
        citizenId: pendingUpdate.citizenId || undefined,
        country: pendingUpdate.country || undefined,
        gender: pendingUpdate.gender || undefined,
        phone: pendingUpdate.phone || undefined,
        address: pendingUpdate.address || undefined,
        email: pendingUpdate.email || undefined,
      };

      console.log("Payload being sent:", payload);

      // Gửi PUT request
      const response = await axios.put(`/api/test-orders/${id}`, payload);
      
      // Cập nhật order state với dữ liệu mới từ response
      const updated = response?.data?.result || response?.data || {};
      
      // Calculate new age from updated date of birth
      const newDob = updated.dateOfBirth || payload.dateOfBirth || order.dob;
      const newAge = calculateAge(newDob);
      
      setOrder(prev => ({
        ...prev,
        name: updated.patientName || payload.patientName || prev.name,
        patientName: updated.patientName || payload.patientName || prev.patientName,
        dob: newDob,
        dateOfBirth: newDob,
        age: updated.age || newAge, // Ưu tiên age từ backend, nếu không có thì tính từ dob
        citizenId: updated.citizenId || payload.citizenId || prev.citizenId,
        country: updated.country || payload.country || prev.country,
        gender: updated.gender || payload.gender || prev.gender,
        phone: updated.phone || payload.phone || prev.phone,
        address: updated.address || payload.address || prev.address,
        email: updated.email || payload.email || prev.email,
      }));

      // Đóng modal và thoát chế độ edit
      setShowConfirmModal(false);
      setIsEditingPatient(false);
      setPendingUpdate(null);

      // Hiển thị thông báo thành công
      showToast({ 
        type: "success", 
        title: "Success", 
        message: "Patient information updated successfully" 
      });

    } catch (error) {
      console.error("Error updating patient info:", error);
      console.error("Error response:", error.response?.data);
      showToast({ 
        type: "error", 
        title: "Update Failed", 
        message: error.response?.data?.message || error.message || "Failed to update patient information" 
      });
    } finally {
      setIsUpdating(false);
    }
  };

  const closeConfirmModal = () => {
    if (isUpdating) return;
    setShowConfirmModal(false);
  };

  // Loading state or error handling (Giữ nguyên)
  if (loading) {
    return <Loading />;
  }
  if (error) {
    return <div className="text-center py-8 text-[#FF5A5A]">{error}</div>;
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
          <div className="absolute -left-1 -top-1 w-10 h-10 bg-[#fff0f0] rounded-full"></div>
          <button
            onClick={handleGoBack}
            className="relative z-10 p-2 bg-[#FF5A5A] text-white rounded-full hover:bg-[#FF3A3A] transition-all duration-300 transform hover:scale-105 active:scale-95"
            aria-label="Go back to test orders list"
          >
            <ArrowLeft size={24} aria-hidden="true" />
          </button>
        </div>

        <div>
          <h1 className="text-[24px] font-bold text-[#FF5A5A] tracking-[0.35em] leading-tight">
            TEST ORDER DETAIL
          </h1>
          <div className="text-sm">
            <span className="text-[#FF5A5A] font-medium">ORDER ID:</span> <span className="font-medium text-black">{order.testOrderId}</span>
          </div>
        </div>

        <div className="dto-header-actions">
          {/* Nút Export PDF với màu đỏ */}
          <button
            onClick={handleExportPDF}
            disabled={isExporting}
            className="flex items-center gap-2 px-6 min-h-[40px] py-2 bg-[#FF5A5A] text-white rounded-lg hover:bg-[#FF3A3A] transition-all duration-300 transform hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 shadow-sm"
            style={{ border: "1px solid #CCC" }}
            onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
            onBlur={(e) => e.target.style.borderColor = "#CCC"}
            aria-label={isExporting ? "Generating PDF file" : "Export test order as PDF"}
            aria-busy={isExporting ? "true" : "false"}
          >
            {isExporting ? "Generating PDF..." : "Export PDF"}
          </button>
        </div>
      </div>

      <div className="dto-grid" role="main" aria-label="Test order details">
        <div className="dto-left-col" role="region" aria-label="Patient and test information">
          <PatientInfo 
            patient={order} 
            isEditing={isEditingPatient}
            onSave={handleSavePatient}
            onCancel={handleCancelEdit}
          />
          <TestResult 
            tests={order.testResults} 
            onUpdate={(updates = {}) => {
              if (updates.testResults) {
                setTestResults(updates.testResults);
              }

              setOrder((prev) => {
                if (!prev) return prev;

                const next = { ...prev };

                if (updates.testResults) {
                  next.testResults = updates.testResults;
                }

                const receivedStatus = updates.status
                  ? String(updates.status).toUpperCase()
                  : null;

                if (receivedStatus) {
                  next.status = receivedStatus;
                } else if (updates.testResults) {
                  const currentStatus = String(prev.status || "").toUpperCase();
                  if (!currentStatus || currentStatus === "PENDING") {
                    next.status = "COMPLETED";
                  }
                }

                return next;
              });
            }}
          />

          {/* ++ SỬA: Bọc Comments trong div "no-print" để ẩn */}
          <div className="no-print">
            <Comments comments={order.comments} />
          </div>
        </div>

        <div className="dto-right-col" role="complementary" aria-label="Order metadata and actions">
          <OrderMeta order={order} />

          {/* ++ SỬA: Bọc QuickActions trong div "no-print" để ẩn */}
          <div className="no-print">
            <QuickActions
              status={order.status}
              onStatusChange={(newStatus) => {
                setOrder((prev) => (
                  prev ? { ...prev, status: newStatus } : prev
                ));
              }}
              onEditOrder={handleEditOrder}
            />
          </div>

          {/* Flag Chart component */}
          <FlagChart 
            orderId={id} 
            testResults={testResults} 
          />
        </div>
      </div>

      {/* Confirmation Modal */}
      {showConfirmModal && createPortal(
        <div
          className="modal-overlay"
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100vw",
            height: "100vh",
            background: "rgba(0,0,0,0.45)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 9999,
            padding: 20,
            animation: "fadeInOverlay 0.3s ease-out",
          }}
          onClick={closeConfirmModal}
          role="dialog"
          aria-modal="true"
          aria-labelledby="confirm-update-title"
          aria-describedby="confirm-update-description"
        >
          <div
            className="bg-white rounded-2xl w-full max-w-lg p-6 shadow-lg mx-auto"
            style={{ 
              maxHeight: "90vh", 
              overflow: "auto",
              animation: "slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1)",
            }}
            role="dialog"
            aria-modal
            onClick={(e) => e.stopPropagation()}
          >
            <h3 id="confirm-update-title" className="text-2xl text-[#FF5A5A] font-bold text-center mb-4">
              Confirm Update
            </h3>
            <p id="confirm-update-description" className="text-center text-sm text-gray-600 mb-6">
              Are you sure you want to update patient information?
            </p>

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
              <button
                onClick={closeConfirmModal}
                className="px-4 min-h-[40px] py-2 rounded-lg"
                style={{ 
                  background: "#f3f4f6",
                  cursor: isUpdating ? "not-allowed" : "pointer",
                  opacity: isUpdating ? 0.6 : 1,
                  transition: "all 0.2s ease",
                  border: "1px solid #CCC",
                }}
                onMouseOver={(e) => !isUpdating && (e.target.style.background = "#e5e7eb")}
                onMouseOut={(e) => (e.target.style.background = "#f3f4f6")}
                onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                onBlur={(e) => e.target.style.borderColor = "#CCC"}
                disabled={isUpdating}
                aria-label="Cancel patient information update"
              >
                No, cancel
              </button>
              <button
                onClick={handleConfirmUpdate}
                className="px-4 min-h-[40px] py-2 rounded-lg"
                style={{
                  background: "#FF5A5A",
                  color: "#fff",
                  fontWeight: 600,
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 8,
                  opacity: isUpdating ? 0.6 : 1,
                  cursor: isUpdating ? "not-allowed" : "pointer",
                  transition: "all 0.2s ease",
                  border: "1px solid #CCC",
                }}
                onMouseOver={(e) => !isUpdating && (e.target.style.background = "#e54e54")}
                onMouseOut={(e) => (e.target.style.background = "#FF5A5A")}
                onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                onBlur={(e) => e.target.style.borderColor = "#CCC"}
                disabled={isUpdating}
                aria-label="Confirm patient information update"
                aria-busy={isUpdating ? "true" : "false"}
              >
                {isUpdating ? "Updating..." : "Yes, update"}
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}
