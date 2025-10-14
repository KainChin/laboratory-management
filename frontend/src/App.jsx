import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import TestOrders from "./pages/TestOrders";

export default function App() {
  return (
    <div className="flex bg-[#f8fafc] min-h-screen">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Header />
        {/* scale nhẹ cho gọn như bản bạn thích */}
        <main className="flex-1 overflow-y-auto">
          <div className="max-w-[1200px] mx-auto scale-[0.9] origin-top px-6 py-8">
            <TestOrders />
          </div>
        </main>
      </div>
    </div>
  );
}
