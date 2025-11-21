import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Index from "./pages/Index";
import TestOrders from "./pages/TestOrders";
import DetailTestOrder from "./pages/DetailTestOrder";
import ToastContainer from "./components/Toast";
import { useAuthToken } from "./hooks/useAuthToken";

function AppContent() {
  // Sử dụng hook để tự động xử lý token từ URL và silent login
  const { loading, error, userInfo, isAuthenticated, getToken, clearToken, refreshToken } = useAuthToken();

  // Hiển thị loading khi đang kiểm tra auth
  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-gray-600">Đang kiểm tra xác thực...</p>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="min-h-screen bg-background flex w-[100%]">
        <Sidebar />
        <div className="w-[100%]">
          <Header />
          <Routes>
            <Route path="/" element={<Index />} />
            <Route path="/home" element={<Index />} />
            <Route path="/test-orders" element={
              <main className="flex-1 overflow-y-auto">
                <div className="max-w-[1200px] mx-auto scale-[0.9] origin-top px-6 py-8">
                  <TestOrders />
                </div>
              </main>
            } />
            <Route path="/test-orders/detail/:id" element={
              <main className="flex-1 overflow-y-auto">
                <div className="max-w-[1200px] mx-auto scale-[0.9] origin-top px-6 py-8">
                  <DetailTestOrder />
                </div>
              </main>
            } />
          </Routes>
        </div>
      </div>
      <ToastContainer />
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}
