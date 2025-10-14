import { Eye, Edit, Trash2, Filter, Search } from "lucide-react";

export default function OrdersTable() {
  const data = [
    { id: "P001", name: "Nguyen Tan Dung", status: "Completed", date: "27/09/2025", creator: "John. Smith" },
    { id: "P002", name: "Nguyen Ngoc Van", status: "Cancelled", date: "27/09/2025", creator: "John. Smith" },
    { id: "P003", name: "Tran Phuoc An", status: "Reviewed", date: "27/09/2025", creator: "John. Smith" },
    { id: "P004", name: "Nguyen Tan Dung", status: "Pending", date: "27/09/2025", creator: "John. Smith" },
  ];

  const statusColor = {
    Completed: "bg-green-100 text-green-700",
    Cancelled: "bg-red-100 text-red-700",
    Pending: "bg-blue-100 text-blue-700",
    Reviewed: "bg-purple-100 text-purple-700",
  };

  return (
    <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
      <div className="flex justify-between items-center mb-3">
        <h2 className="text-red-500 font-semibold">Test Order Lists</h2>
        <button className="bg-red-500 text-white px-3 py-1.5 rounded-lg hover:bg-red-600">
          + New Test Order
        </button>
      </div>

      <div className="flex items-center gap-2 mb-3">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-2 top-2 text-gray-400" />
          <input
            type="text"
            placeholder="Search patient ID..."
            className="w-full pl-8 pr-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-red-300"
          />
        </div>
        <button className="bg-red-100 text-red-500 px-3 py-1.5 rounded-lg flex items-center gap-1">
          <Filter size={14} /> Filter
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left border-collapse">
          <thead>
            <tr className="bg-red-500 text-white">
              <th className="py-2 px-3 rounded-tl-lg">ID</th>
              <th className="py-2 px-3">Name</th>
              <th className="py-2 px-3">Status</th>
              <th className="py-2 px-3">Created Date</th>
              <th className="py-2 px-3">Created By</th>
              <th className="py-2 px-3 rounded-tr-lg text-center">Action</th>
            </tr>
          </thead>
          <tbody>
            {data.map((row, i) => (
              <tr key={i} className="border-b hover:bg-gray-50">
                <td className="py-2 px-3 font-medium">{row.id}</td>
                <td className="py-2 px-3">{row.name}</td>
                <td className="py-2 px-3">
                  <span className={`px-2 py-1 text-xs font-semibold rounded-full ${statusColor[row.status]}`}>
                    {row.status}
                  </span>
                </td>
                <td className="py-2 px-3">{row.date}</td>
                <td className="py-2 px-3">{row.creator}</td>
                <td className="py-2 px-3 text-center space-x-2">
                  <button className="text-blue-500 hover:text-blue-700"><Eye size={15} /></button>
                  <button className="text-orange-500 hover:text-orange-700"><Edit size={15} /></button>
                  <button className="text-red-500 hover:text-red-700"><Trash2 size={15} /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
