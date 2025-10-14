export default function ActivityCard() {
  return (
    <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
      <h2 className="font-semibold text-red-500 mb-2">Recent Activity</h2>
      <p className="text-sm text-gray-500 mb-4">
        Manage patient test orders and view laboratory results
      </p>

      <div className="text-sm space-y-4">
        <div>
          <b>Test order TO-2024-005 completed</b>
          <div className="flex items-center justify-between">
            <p className="text-gray-700">Patient: KaiChin</p>
            <span className="text-gray-400 text-xs">5 minutes ago</span>
          </div>
        </div>
        <div>
          <b>Test Results Reviewed</b>
          <div className="flex items-center justify-between">
            <p className="text-gray-700">Patient: Luong Minh Nhat</p>
            <span className="text-gray-400 text-xs">1 hour ago</span>
          </div>
        </div>
      </div>
    </div>
  );
}
