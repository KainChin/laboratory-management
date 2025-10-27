import React from "react";
import { Link } from "react-router-dom";

export default function DetailTestOrder() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-full mx-auto px-6 py-6">
        <div className="bg-white rounded-lg shadow-sm p-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button className="p-2 bg-red-500 text-white rounded-md">≡</button>
            <div>
              <div className="text-sm text-gray-400">Laboratory Management</div>
              <div className="text-sm">
                <Link to="/test-orders" className="text-red-400 hover:underline">Test Orders</Link>
                <span className="mx-2 text-gray-300">›</span>
                <span className="text-gray-600">Detail</span>
              </div>
            </div>
          </div>
          <div className="text-sm text-gray-600">Welcome, [Lab User]</div>
        </div>

        <div className="mt-6">
          <div className="bg-white rounded-2xl p-6 shadow-sm">
            <h1 className="text-xl font-semibold mb-2">Test Order Detail</h1>
            <p className="text-gray-500 mb-4">Thông tin chi tiết test order sẽ hiển thị ở đây.</p>

            <div className="grid grid-cols-3 gap-6">
              <div className="col-span-2 bg-gray-50 p-4 rounded">Chi tiết chính (form / dữ liệu)</div>
              <div className="bg-gray-50 p-4 rounded">Sidebar / Activity</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}