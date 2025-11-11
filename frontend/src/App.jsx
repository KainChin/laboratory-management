import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import TestOrders from "./pages/TestOrders";
import DetailTestOrder from "./pages/DetailTestOrder"; // <-- sử dụng index.jsx trong folder
import ToastContainer from "./components/Toast";

export default function App() {
  return (
    <BrowserRouter>
      <div className="flex bg-[#f8fafc] min-h-screen">
        <Sidebar />
        <div className="flex-1 flex flex-col">
          <Header />
          <main className="flex-1 overflow-y-auto">
            <div className="max-w-[1200px] mx-auto scale-[0.9] origin-top px-6 py-8">
              <Routes>
                <Route path="/" element={<TestOrders />} />
                <Route path="/test-orders" element={<TestOrders />} />
                <Route path="/test-orders/detail/:id" element={<DetailTestOrder />} />
              </Routes>
            </div>
          </main>
        </div>
      </div>
      <ToastContainer />
    </BrowserRouter>
  );
}
