import { Eye, Edit, Trash2, Filter, Search } from "lucide-react";
import { useState, useEffect } from "react";
import DeleteConfirmationModal from "./DeleteConfirmationModal";

export default function OrdersTable() {
  // const PAGE_SIZE and page/setPage are now declared above for backend pagination
  const [totalPages, setTotalPages] = useState(1);
  const [keyword, setKeyword] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [sortDir, setSortDir] = useState("asc");
  const PAGE_SIZE = 5;
  const [page, setPage] = useState(1);
  const [successMsg, setSuccessMsg] = useState("");
  // Đổi mảng data sang orders (state), để bảng tự động cập nhật khi thao tác
  const [orders, setOrders] = useState([]);
  const [deleteId, setDeleteId] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteMsg, setDeleteMsg] = useState("");
  const [deleteError, setDeleteError] = useState("");

  function fetchOrdersWrapper(pageIdx = page) {
    async function fetchOrders() {
      try {
        const params = new URLSearchParams({
          keyword,
          page: pageIdx.toString(),
          size: PAGE_SIZE.toString(),
          sortDir,
        });
        const res = await fetch(`http://localhost:6868/api/test-orders?${params}`);
        if (!res.ok) throw new Error('Failed to fetch orders');
        const result = await res.json();
        const items = result.result?.items || [];
        setTotalPages(result.result?.totalPages || 1);
        setOrders(items.map(order => ({
          id: order.testOrderId,
          name: order.patientName,
          status: order.status || "Pending",
          date: order.dateOfBirth,
          creator: order.createdBy || "Unknown",
          dob: order.dateOfBirth,
          phone: order.phone,
          email: order.email,
          gender: order.gender,
          address: order.address,
          country: order.country,
          citizenId: order.citizenId,
        })));
      } catch (err) {
        console.warn('Fetch orders failed:', err);
      }
    }
    fetchOrders();
  }

  useEffect(() => {
    fetchOrdersWrapper();
  }, [page, keyword, sortDir]);

  const statusColor = {
    Completed: "bg-green-100 text-green-700",
    Cancelled: "bg-red-100 text-red-700",
    Pending: "bg-blue-100 text-blue-700",
    Reviewed: "bg-purple-100 text-purple-700",
  };

  const [showModal, setShowModal] = useState(false);
  const [mode, setMode] = useState("create");
  const [form, setForm] = useState({
    patientName: "",
    dob: "",
    phone: "",
    email: "",
    gender: "",
    address: "",
    country: "",
    citizenId: "",
  });
  // Lưu id đang edit để update đúng đơn hàng
  const [editingId, setEditingId] = useState(null);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((s) => ({ ...s, [name]: value }));
  }

  function resetForm() {
    setForm({
      patientName: "",
      dob: "",
      phone: "",
      email: "",
      gender: "",
      address: "",
      country: "",
      citizenId: "",
    });
  }

  // reusable date formatter to dd/MM/yyyy
  function formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  }

  // convert backend dd/MM/yyyy -> yyyy-MM-dd for input[type=date]
  function parseBackendDateToInput(dateStr) {
    if (!dateStr) return "";
    const parts = dateStr.split('/');
    if (parts.length !== 3) return dateStr;
    const [dd, mm, yyyy] = parts;
    return `${yyyy}-${mm.padStart(2, '0')}-${dd.padStart(2, '0')}`;
  }

  function openCreateModal() {
    setMode("create");
    setEditingId(null);
    resetForm();
    setShowModal(true);
  }
  function openEditModal(order) {
    setMode("edit");
    setEditingId(order.id);
    setForm({
      patientName: order.name || "",
      dob: order.dob ? parseBackendDateToInput(order.dob) : "",
      phone: order.phone || "",
      email: order.email || "",
      gender: order.gender || "",
      address: order.address || "",
      country: order.country || "",
      citizenId: order.citizenId || "",
    });
    setShowModal(true);
  }

  function openViewModal(order) {
    setMode("view");
    setEditingId(order.id);
    setForm({
      patientName: order.name || "",
      dob: order.dob || "",
      phone: order.phone || "",
      email: order.email || "",
      gender: order.gender || "",
      address: order.address || "",
      country: order.country || "",
      citizenId: order.citizenId || "",
    });
    setShowModal(true);
  }

  async function handleCreate() {
    // Build payload for backend
    const payload = {
      patientName: form.patientName,
      dateOfBirth: form.dob ? formatDate(form.dob) : formatDate(new Date()),
      citizenId: form.citizenId,
      country: form.country,
      gender: form.gender ? form.gender.toUpperCase() : "",
      address: form.address,
      email: form.email,
      phone: form.phone,
    };

    try {
      const res = await fetch('http://localhost:6868/api/test-orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || 'Failed to create test order');
      }
      const result = await res.json();
      // result.result is the created order (from RestResponse)
      const created = result.result || {};
      setOrders([
        ...orders,
        {
          id: created.id || `P00${orders.length + 1}`,
          name: created.patientName || form.patientName,
          status: created.status || "Pending",
          date: created.dateOfBirth || formatDate(form.dob) || formatDate(new Date()),
          creator: created.createdBy || "John. Smith",
          dob: created.dateOfBirth || formatDate(form.dob),
          phone: created.phone || form.phone,
          email: created.email || form.email,
          gender: created.gender || form.gender,
          address: created.address || form.address,
          country: created.country || form.country,
          citizenId: created.citizenId || form.citizenId,
        }
      ]);
      setShowModal(false);
      setSuccessMsg("Create test order successfully!");
      setTimeout(() => setSuccessMsg(""), 3000);
      resetForm();
      // REFRESH danh sách sau khi thêm
      fetchOrdersWrapper(1); // về trang 1 sau khi thêm mới
      setPage(1);
    } catch (err) {
      alert('Create failed: ' + err.message);
    }
  }

  function handleUpdate() {
    // Send update to backend
    if (!editingId) {
      alert('No order selected to edit');
      return;
    }
    const payload = {
      patientName: form.patientName || undefined,
      dateOfBirth: form.dob ? formatDate(form.dob) : undefined,
      gender: form.gender ? form.gender.toUpperCase() : undefined,
      phone: form.phone || undefined,
      address: form.address || undefined,
      email: form.email || undefined,
    };

    (async () => {
      try {
        const res = await fetch(`http://localhost:6868/api/test-orders/${editingId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        if (!res.ok) {
          const text = await res.text();
          throw new Error(text || 'Failed to update test order');
        }
        const result = await res.json();
        const updated = result.result || {};
        setOrders(orders.map(o => o.id === editingId ? {
          ...o,
          name: updated.patientName || form.patientName || o.name,
          dob: updated.dateOfBirth || form.dob || o.dob,
          phone: updated.phone || form.phone || o.phone,
          email: updated.email || form.email || o.email,
          gender: updated.gender || form.gender || o.gender,
          address: updated.address || form.address || o.address,
        } : o));

        setShowModal(false);
        setEditingId(null);
        setSuccessMsg('Update successfully!');
        setTimeout(() => setSuccessMsg(''), 3000);
        resetForm();
      } catch (err) {
        alert('Update failed: ' + err.message);
      }
    })();
  }

  // No local pagination, backend handles it

  return (
    <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 relative">
      {successMsg && (
        <div className="absolute left-1/2 -translate-x-1/2 top-2 bg-green-100 text-green-700 px-6 py-2 rounded-lg shadow font-semibold z-50">
          {successMsg}
        </div>
      )}
      <div className="flex justify-between items-center mb-3">
        <h2 className="text-red-500 font-semibold">Test Order Lists</h2>
        <button onClick={openCreateModal} className="bg-red-500 text-white px-3 py-1.5 rounded-lg hover:bg-red-600">
          + New Test Order
        </button>
      </div>

      <div className="flex items-center gap-2 mb-3">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-2 top-2 text-gray-400" />
          <input
            type="text"
            value={searchInput}
            onChange={e => setSearchInput(e.target.value)}
            onKeyDown={e => { if (e.key === 'Enter') { setKeyword(searchInput); setPage(1); } }}
            placeholder="Search patient name..."
            className="w-full pl-8 pr-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-red-300"
          />
        </div>
        <button
          className="bg-red-100 text-red-500 px-3 py-1.5 rounded-lg flex items-center gap-1"
          onClick={() => { setKeyword(searchInput); setPage(1); }}
        >
          <Filter size={14} /> Search
        </button>
        <button
          className="bg-gray-100 text-gray-700 px-3 py-1.5 rounded-lg ml-2 border"
          onClick={() => { setSortDir(sortDir === 'asc' ? 'desc' : 'asc'); setPage(1); }}
        >
          Sort: {sortDir === 'asc' ? 'A-Z' : 'Z-A'}
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left border-collapse">
          <thead>
            <tr className="bg-red-500 text-white">
              <th className="py-2 px-3 rounded-tl-lg">Name</th>
              <th className="py-2 px-3">Status</th>
              <th className="py-2 px-3">Date of Birth</th>
              <th className="py-2 px-3">Created By</th>
              <th className="py-2 px-3 rounded-tr-lg text-center">Action</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((row) => (
              <tr key={row.id} className="border-b hover:bg-gray-50">
                <td className="py-2 px-3">{row.name}</td>
                <td className="py-2 px-3">
                  <span className={`px-2 py-1 text-xs font-semibold rounded-full ${statusColor[row.status]}`}>
                    {row.status}
                  </span>
                </td>
                <td className="py-2 px-3">{row.dob}</td>
                <td className="py-2 px-3">{row.creator}</td>
                <td className="py-2 px-3 text-center space-x-2">
                  <button className="text-blue-500 hover:text-blue-700" onClick={() => openViewModal(row)}><Eye size={15} /></button>
                  <button className="text-orange-500 hover:text-orange-700" onClick={() => openEditModal(row)}><Edit size={15} /></button>
                  <button className="text-red-500 hover:text-red-700" onClick={() => { setDeleteId(row.id); setShowDeleteModal(true); }}><Trash2 size={15} /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination controls */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center mt-4 gap-2">
          <button
            className="px-3 py-1 rounded border bg-gray-100 text-gray-700 disabled:opacity-50"
            onClick={() => setPage(page - 1)}
            disabled={page === 1}
          >
            Prev
          </button>
          {[...Array(totalPages)].map((_, i) => (
            <button
              key={i}
              className={`px-3 py-1 rounded border ${page === i + 1 ? 'bg-red-500 text-white' : 'bg-gray-100 text-gray-700'}`}
              onClick={() => setPage(i + 1)}
            >
              {i + 1}
            </button>
          ))}
          <button
            className="px-3 py-1 rounded border bg-gray-100 text-gray-700 disabled:opacity-50"
            onClick={() => setPage(page + 1)}
            disabled={page === totalPages}
          >
            Next
          </button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl w-[820px] p-8 shadow-lg">
            <h3 className="text-2xl text-red-500 font-bold text-center">
              {mode === 'view' ? 'Detail Test Order Information' : mode === 'edit' ? 'UPDATE TEST ORDER' : 'NEW TEST ORDER'}
            </h3>
            <p className="text-center text-sm text-gray-500 mb-6">
              {mode === 'view'
                ? 'View patient information for this test order'
                : mode === 'edit'
                  ? 'Update patient information for this test order'
                  : 'Enter patient information to create a new test order'}
            </p>

            <div className="border rounded-lg p-6 bg-gray-50">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-red-500 font-semibold text-sm">Patient Name</label>
                  <input name="patientName" value={form.patientName} onChange={handleChange} readOnly={mode === 'view'} className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white" />
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">Date of Birth</label>
                  {mode === 'view' ? (
                    <div className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white">{form.dob}</div>
                  ) : (
                    <input name="dob" value={form.dob} onChange={handleChange} type="date" className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm" />
                  )}
                </div>

                <div>
                  <label className="text-red-500 font-semibold text-sm">Phone Number</label>
                  <input name="phone" value={form.phone} onChange={handleChange} readOnly={mode === 'view'} className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white" />
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">Email</label>
                  <input name="email" value={form.email} onChange={handleChange} type="email" readOnly={mode === 'view'} className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white" />
                </div>

                <div>
                  <label className="text-red-500 font-semibold text-sm">Gender</label>
                  {mode === 'view' ? (
                    <div className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white">{form.gender ? (form.gender === 'MALE' ? 'Male' : form.gender === 'FEMALE' ? 'Female' : form.gender) : ''}</div>
                  ) : (
                    <select name="gender" value={form.gender} onChange={handleChange} className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm">
                      <option value="">Select</option>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                  )}
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">Address</label>
                  <input name="address" value={form.address} onChange={handleChange} readOnly={mode === 'view'} className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white" />
                </div>

                <div>
                  <label className="text-red-500 font-semibold text-sm">Country</label>
                  <input name="country" value={form.country} onChange={handleChange} readOnly={mode === 'view'} className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white" />
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">Citizen ID</label>
                  <input name="citizenId" value={form.citizenId} onChange={handleChange} readOnly={mode === 'view'} className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white" />
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-4 mt-6">
              <button onClick={() => { setShowModal(false); setMode('create'); }} className="px-4 py-2 border border-gray-200 rounded-lg bg-white">Close</button>
              {mode === "edit" ? (
                <button onClick={handleUpdate} className="px-4 py-2 bg-red-500 text-white rounded-lg">Save</button>
              ) : mode === 'view' ? null : (
                <button onClick={handleCreate} className="px-4 py-2 bg-red-500 text-white rounded-lg">Create</button>
              )}
            </div>
          </div>
        </div>
      )}
      {/* Xoá - Modal xác nhận */}
      <DeleteConfirmationModal
        open={showDeleteModal}
        orderId={deleteId}
        onCancel={() => setShowDeleteModal(false)}
        onConfirm={async () => {
          try {
            const res = await fetch(`http://localhost:6868/api/test-orders/${deleteId}`, { method: 'DELETE' });
            if (!res.ok) {
              const text = await res.text();
              setDeleteError(text || 'Xoá thất bại!');
              setDeleteMsg("");
              setTimeout(() => {
                setDeleteError("");
                setDeleteId(null);
              }, 3000);
            } else {
              // Sau khi xoá thành công:
              // Nếu đã xoá phần tử cuối cùng của trang và không phải trang 1, thì lùi về trang trước
              const isLastItem = orders.length === 1; // chỉ còn 1 phần tử trang này (sau khi xoá = 0)
              if (isLastItem && page > 1) {
                setPage(page - 1);
                fetchOrdersWrapper(page - 1);
              } else {
                fetchOrdersWrapper();
              }
              setDeleteMsg('Đã xoá thành công!');
              setDeleteError("");
              setShowDeleteModal(false);
              setDeleteId(null);
              setTimeout(() => setDeleteMsg(""), 3000);
            }
          } catch (e) {
            setDeleteError('Lỗi mạng hoặc server!');
            setDeleteMsg("");
            setTimeout(() => {
              setDeleteError("");
              setDeleteId(null);
            }, 3000);
          }
        }}
      />
      {/* Thông báo thành công xoá */}
      {deleteMsg && <div className="fixed left-1/2 top-5 -translate-x-1/2 bg-green-100 text-green-700 px-6 py-2 shadow-lg rounded-lg z-50 font-semibold">{deleteMsg}</div>}
      {deleteError && <div className="fixed left-1/2 top-5 -translate-x-1/2 bg-red-100 text-red-600 px-6 py-2 shadow-lg rounded-lg z-50 font-semibold">{deleteError}</div>}
    </div>
  );
}
