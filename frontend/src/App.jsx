import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Index from "./pages/Index";
import TestOrders from "./pages/TestOrders";
import DetailTestOrder from "./pages/DetailTestOrder";
import ToastContainer from "./components/Toast";

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-background flex w-[100%]">
        <Sidebar />
        <div className="w-[100%]">
          <Header />
          <Routes>
            <Route path="/" element={<Index />} />
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
    </BrowserRouter>
  );
}
