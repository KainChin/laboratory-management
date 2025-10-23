import { Eye, Edit, Trash2, Filter, Search } from "lucide-react";
import {
  useState,
  useEffect,
  useCallback,
  useRef,
  useLayoutEffect,
} from "react";
import { createPortal } from "react-dom";
import DeleteConfirmationModal from "./DeleteConfirmationModal";

// Modal portal so the overlay covers the whole viewport
function Modal({ children }) {
  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-2">
      {children}
    </div>,
    document.body
  );
}

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

  const fetchOrdersWrapper = useCallback(
    (pageIdx = page) => {
      async function fetchOrders() {
        try {
          const params = new URLSearchParams({
            keyword,
            page: pageIdx.toString(),
            size: PAGE_SIZE.toString(),
            sortDir,
          });
          const res = await fetch(
            `http://localhost:6868/api/test-orders?${params}`
          );
          if (!res.ok) throw new Error("Failed to fetch orders");
          const result = await res.json();
          const items = result.result?.items || [];
          setTotalPages(result.result?.totalPages || 1);
          // Format date from backend (assuming it's in dd/MM/yyyy format)
          setOrders(
            items.map((order) => ({
              id: order.testOrderId,
              name: order.patientName,
              status: order.status || "Pending",
              date: order.dateOfBirth,
              creator: order.createdBy || "Unknown",
              // Preserve the original date format for display
              dob: order.dateOfBirth || "",
              phone: order.phone,
              email: order.email,
              gender: order.gender,
              address: order.address,
              country: order.country,
              citizenId: order.citizenId,
            }))
          );
          // notify other components (ChartSection) that orders changed
          try {
            const mapped = items.map((order) => ({
              id: order.testOrderId,
              status: order.status || "Pending",
              // prefer createdAt / createdDate / date fields if backend provides them
              date:
                order.createdAt ||
                order.createdDate ||
                order.dateOfBirth ||
                null,
            }));
            window.dispatchEvent(
              new CustomEvent("orders:updated", { detail: { orders: mapped } })
            );
          } catch (err) {
            // log dispatch errors for debugging
            console.error("Failed to dispatch orders:updated event:", err);
          }
        } catch (err) {
          console.warn("Fetch orders failed:", err);
        }
      }
      fetchOrders();
    },
    [keyword, page, sortDir]
  );

  useEffect(() => {
    fetchOrdersWrapper();
  }, [fetchOrdersWrapper]);

  const statusColor = {
    Completed: "bg-green-100 text-green-700",
    COMPLETED: "bg-green-100 text-green-700",
    Cancelled: "bg-red-100 text-red-700",
    CANCELLED: "bg-red-100 text-red-700",
    Pending: "bg-blue-100 text-blue-700",
    PENDING: "bg-blue-100 text-blue-700",
    Reviewed: "bg-purple-100 text-purple-700",
    REVIEWED: "bg-purple-100 text-purple-700",
    AI_Reviewed: "bg-orange-100 text-orange-700",
    AI_REVIEWED: "bg-orange-100 text-orange-700",
  };

  const [showModal, setShowModal] = useState(false);
  const modalRef = useRef(null);
  const lastScaleRef = useRef(1);
  const [modalScale, setModalScale] = useState(1);
  const [mode, setMode] = useState("create");
  const [form, setForm] = useState({
    patientName: "",
    dob: "",
    phone: "",
    email: "",
    gender: "",
    status: "",
    address: "",
    country: "",
    citizenId: "",
  });
  // local form inside modal to avoid parent re-renders clobbering input while typing
  const [localForm, setLocalForm] = useState(form);
  // validation errors for form fields
  const [errors, setErrors] = useState({});
  // Lưu id đang edit để update đúng đơn hàng
  const [editingId, setEditingId] = useState(null);

  // initialize modal local form only when modal opens / mode or editingId changes
  useEffect(() => {
    // chỉ reset khi vừa mở modal hoặc đổi mode / editingId để tránh ghi đè khi đang gõ
    if (showModal && (mode === "create" || editingId)) {
      setLocalForm(form);
      setErrors({});
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showModal, mode, editingId]);

  function resetForm() {
    setForm({
      patientName: "",
      dob: "",
      phone: "",
      email: "",
      gender: "",
      status: "",
      address: "",
      country: "",
      citizenId: "",
    });
  }

  // reusable date formatter to yyyy-MM-dd (phù hợp input[type=date]); không phá nếu date invalid
  function formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    if (isNaN(d)) return dateStr;
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  // convert yyyy-MM-dd from date input to dd/MM/yyyy for backend
  function formatDateForBackend(dateStr) {
    if (!dateStr) return "";
    const parts = dateStr.split("-");
    if (parts.length !== 3) return dateStr; // return original if not yyyy-MM-dd
    const [yyyy, mm, dd] = parts;
    return `${dd}/${mm}/${yyyy}`;
  }

  // gender mapping helpers
  function enumToSelectGender(g) {
    if (!g) return "";
    if (g === "MALE") return "Male";
    if (g === "FEMALE") return "Female";
    if (g === "OTHER") return "Other";
    return g;
  }

  function selectToEnumGender(g) {
    if (!g) return "";
    if (g === "Male") return "MALE";
    if (g === "Female") return "FEMALE";
    if (g === "Other") return "OTHER";
    // if already enum
    return String(g).toUpperCase();
  }

  // status mapping between backend enums and select labels
  function enumToSelectStatus(s) {
    if (!s) return "";
    if (s === "PENDING" || s === "Pending") return "Pending";
    if (s === "COMPLETED" || s === "Completed") return "Completed";
    if (s === "CANCELLED" || s === "Cancelled") return "Cancelled";
    return s;
  }

  function selectToEnumStatus(s) {
    if (!s) return "";
    if (s === "Pending") return "PENDING";
    if (s === "Completed") return "COMPLETED";
    if (s === "Cancelled") return "CANCELLED";
    return String(s).toUpperCase();
  }

  // convert backend dd/MM/yyyy -> yyyy-MM-dd for input[type=date]
  function parseBackendDateToInput(dateStr) {
    if (!dateStr) return "";
    const parts = dateStr.split("/");
    if (parts.length !== 3) return dateStr;
    const [dd, mm, yyyy] = parts;
    return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
  }

  // validate form fields; return an object of errors (field -> message)
  // accepts a form object so we can validate localForm without relying on parent `form`
  function validateForm(f = form) {
    const e = {};
    if (!f.patientName || !String(f.patientName).trim()) {
      e.patientName = "Patient name is required";
    }
    if (!f.dob) {
      e.dob = "Date of birth is required";
    } else {
      const dobDate = new Date(f.dob);
      const today = new Date();
      dobDate.setHours(0, 0, 0, 0);
      today.setHours(0, 0, 0, 0);
      if (dobDate > today) {
        e.dob = "Date of birth cannot be in the future";
      }
    }
    if (!f.phone || !String(f.phone).trim()) {
      e.phone = "Phone number is required";
    } else if (!/^[0-9()+\-\s]{7,20}$/.test(f.phone)) {
      e.phone = "Phone number looks invalid";
    }
    if (!f.email || !String(f.email).trim()) {
      e.email = "Email is required";
    } else if (!/^\S+@\S+\.\S+$/.test(f.email)) {
      e.email = "Email is invalid";
    }
    if (!f.gender || !String(f.gender).trim()) {
      e.gender = "Gender is required";
    }
    if (!f.status || !String(f.status).trim()) {
      e.status = "Status is required";
    }
    if (!f.citizenId || !String(f.citizenId).trim()) {
      e.citizenId = "Citizen ID is required";
    }
    return e;
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
      // map backend enum (MALE/FEMALE) to select values (Male/Female)
      gender: enumToSelectGender(order.gender),
      status: enumToSelectStatus(order.status),
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
      status: enumToSelectStatus(order.status) || "",
      address: order.address || "",
      country: order.country || "",
      citizenId: order.citizenId || "",
    });
    setShowModal(true);
  }

  // handleCreate accepts optional form object (use localForm when provided)
  async function handleCreate(currentForm) {
    const f = currentForm || localForm;
    // validate
    const validation = validateForm(f);
    if (Object.keys(validation).length > 0) {
      setErrors(validation);
      return;
    }

    // Build payload for backend
    const payload = {
      patientName: f.patientName,
      dateOfBirth: f.dob ? formatDateForBackend(f.dob) : "",
      citizenId: f.citizenId,
      country: f.country,
      gender: selectToEnumGender(f.gender) || "",
      status: f.status ? selectToEnumStatus(f.status) : undefined,
      address: f.address,
      email: f.email,
      phone: f.phone,
    };

    try {
      const res = await fetch("http://localhost:6868/api/test-orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to create test order");
      }
      const result = await res.json();
      // result.result is the created order (from RestResponse)
      const created = result.result || {};
      setOrders([
        ...orders,
        {
          id: created.id || `P00${orders.length + 1}`,
          name: created.patientName || f.patientName,
          status: created.status || "Pending",
          date:
            created.dateOfBirth || formatDate(f.dob) || formatDate(new Date()),
          creator: created.createdBy || "John. Smith",
          dob: created.dateOfBirth || formatDate(f.dob),
          phone: created.phone || f.phone,
          email: created.email || f.email,
          gender: created.gender || f.gender,
          address: created.address || f.address,
          country: created.country || f.country,
          citizenId: created.citizenId || f.citizenId,
        },
      ]);
      setShowModal(false);
      setSuccessMsg("Create test order successfully!");
      setTimeout(() => setSuccessMsg(""), 3000);
      resetForm();
      setLocalForm({
        patientName: "",
        dob: "",
        phone: "",
        email: "",
        gender: "",
        status: "",
        address: "",
        country: "",
        citizenId: "",
      });
      setErrors({});
      // REFRESH danh sách sau khi thêm
      fetchOrdersWrapper(1); // về trang 1 sau khi thêm mới
      setPage(1);
    } catch (err) {
      alert("Create failed: " + err.message);
    }
  }

  // handleUpdate accepts optional form object (use localForm when provided)
  function handleUpdate(currentForm) {
    // Send update to backend
    if (!editingId) {
      alert("No order selected to edit");
      return;
    }
    const f = currentForm || localForm;
    // validate
    const validation = validateForm(f);
    if (Object.keys(validation).length > 0) {
      setErrors(validation);
      return;
    }

    const payload = {
      patientName: f.patientName || undefined,
      dateOfBirth: f.dob ? formatDateForBackend(f.dob) : undefined,
      gender: f.gender ? selectToEnumGender(f.gender) : undefined,
      status: f.status ? selectToEnumStatus(f.status) : undefined,
      phone: f.phone || undefined,
      address: f.address || undefined,
      email: f.email || undefined,
      citizenId: f.citizenId || undefined,
    };

    (async () => {
      try {
        const res = await fetch(
          `http://localhost:6868/api/test-orders/${editingId}`,
          {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          }
        );
        if (!res.ok) {
          const text = await res.text();
          throw new Error(text || "Failed to update test order");
        }
        const result = await res.json();
        const updated = result.result || {};
        setOrders(
          orders.map((o) =>
            o.id === editingId
              ? {
                  ...o,
                  name: updated.patientName || f.patientName || o.name,
                  dob: updated.dateOfBirth || f.dob || o.dob,
                  status: updated.status || f.status || o.status,
                  phone: updated.phone || f.phone || o.phone,
                  email: updated.email || f.email || o.email,
                  gender: updated.gender || f.gender || o.gender,
                  address: updated.address || f.address || o.address,
                  citizenId: updated.citizenId || f.citizenId || o.citizenId,
                }
              : o
          )
        );

        setShowModal(false);
        setEditingId(null);
        setSuccessMsg("Update successfully!");
        setTimeout(() => setSuccessMsg(""), 3000);
        resetForm();
        setLocalForm({
          patientName: "",
          dob: "",
          phone: "",
          email: "",
          gender: "",
          status: "",
          address: "",
          country: "",
          citizenId: "",
        });
        setErrors({});
      } catch {
        alert("Update failed");
      }
    })();
  }

  // No local pagination, backend handles it

  // auto scale modal so it fits the viewport without scrollbars
  // NOTE: do NOT depend on `form` to avoid rerunning measurement on every keystroke.
  useLayoutEffect(() => {
    let rafId;

    if (!showModal) {
      document.body.style.overflow = "";
      if (lastScaleRef.current !== 1) {
        lastScaleRef.current = 1;
        if (modalScale !== 1) setModalScale(1);
      }
      return;
    }

    // prevent background scrolling while modal open
    document.body.style.overflow = "hidden";

    // measure natural size by cloning modal into an off-screen node to avoid touching real element
    function measureNaturalRect() {
      const el = modalRef.current;
      if (!el) return null;
      try {
        const clone = el.cloneNode(true);
        clone.style.visibility = "hidden";
        clone.style.position = "absolute";
        clone.style.left = "-9999px";
        clone.style.top = "0";
        clone.style.transform = "scale(1)";
        clone.style.transition = "none";
        document.body.appendChild(clone);
        const rect = clone.getBoundingClientRect();
        document.body.removeChild(clone);
        return rect;
      } catch {
        return null;
      }
    }

    function updateScale() {
      const rect = measureNaturalRect();
      if (!rect) {
        if (lastScaleRef.current !== 1) {
          lastScaleRef.current = 1;
          setModalScale(1);
        }
        return;
      }

      const h = rect.height;
      const w = rect.width;
      const vh = window.innerHeight;
      const vw = window.innerWidth;
      const marginY = 48;
      const marginX = 48;
      const scaleY = (vh - marginY) / Math.max(1, h);
      const scaleX = (vw - marginX) / Math.max(1, w);
      const newScale = Math.min(1, scaleX, scaleY);

      // only update when difference is noticeable to avoid re-renders while typing
      if (Math.abs(newScale - lastScaleRef.current) > 0.005) {
        lastScaleRef.current = newScale;
        setModalScale(newScale);
      }
    }

    rafId = requestAnimationFrame(updateScale);
    const onResize = () => requestAnimationFrame(updateScale);
    window.addEventListener("resize", onResize);

    return () => {
      window.removeEventListener("resize", onResize);
      if (rafId) cancelAnimationFrame(rafId);
      document.body.style.overflow = "";
      lastScaleRef.current = 1;
      setModalScale(1);
    };
  }, [showModal, mode]);

  return (
    <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 relative">
      {successMsg && (
        <div className="absolute left-1/2 -translate-x-1/2 top-2 bg-green-100 text-green-700 px-6 py-2 rounded-lg shadow font-semibold z-50">
          {successMsg}
        </div>
      )}
      <div className="flex justify-between items-center mb-3">
        <h2 className="text-red-500 font-semibold">Test Order Lists</h2>
        <button
          onClick={openCreateModal}
          className="bg-red-500 text-white px-3 py-1.5 rounded-lg hover:bg-red-600"
        >
          + New Test Order
        </button>
      </div>

      <div className="flex items-center gap-2 mb-3">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-2 top-2 text-gray-400" />
          <input
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                setKeyword(searchInput);
                setPage(1);
              }
            }}
            placeholder="Search patient name..."
            className="w-full pl-8 pr-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-red-300"
          />
        </div>
        <button
          className="bg-red-100 text-red-500 px-3 py-1.5 rounded-lg flex items-center gap-1"
          onClick={() => {
            setKeyword(searchInput);
            setPage(1);
          }}
        >
          <Filter size={14} /> Search
        </button>
        <button
          className="bg-gray-100 text-gray-700 px-3 py-1.5 rounded-lg ml-2 border"
          onClick={() => {
            setSortDir(sortDir === "asc" ? "desc" : "asc");
            setPage(1);
          }}
        >
          Sort: {sortDir === "asc" ? "A-Z" : "Z-A"}
        </button>
      </div>

      <div className="overflow-x-auto">
        {/* fixed table layout + colgroup to keep columns stable; truncate long content */}
        <table
          className="w-full text-sm text-left border-collapse"
          style={{ tableLayout: "fixed" }}
        >
          <colgroup>
            <col style={{ width: "35%" }} />
            <col style={{ width: "15%" }} />
            <col style={{ width: "18%" }} />
            <col style={{ width: "22%" }} />
            <col style={{ width: "10%" }} />
          </colgroup>
          <thead>
            <tr className="bg-red-500 text-white">
              <th className="py-2 px-3 rounded-tl-lg">
                <div className="truncate">Name</div>
              </th>
              <th className="py-2 px-3">
                <div className="truncate">Status</div>
              </th>
              <th className="py-2 px-3">
                <div className="truncate">Date of Birth</div>
              </th>
              <th className="py-2 px-3">
                <div className="truncate">Created By</div>
              </th>
              <th className="py-2 px-3 rounded-tr-lg text-center">
                <div className="truncate">Action</div>
              </th>
            </tr>
          </thead>
          <tbody>
            {orders.map((row) => (
              <tr key={row.id} className="border-b hover:bg-gray-50">
                <td className="py-2 px-3">
                  <div className="truncate" title={row.name}>
                    {row.name}
                  </div>
                </td>
                <td className="py-2 px-3">
                  <div className="truncate">
                    <span
                      className={`px-2 py-1 text-xs font-semibold rounded-full ${
                        statusColor[row.status]
                      }`}
                    >
                      {row.status}
                    </span>
                  </div>
                </td>
                <td className="py-2 px-3">
                  <div className="truncate" title={row.dob}>
                    {row.dob || ""}
                  </div>
                </td>
                <td className="py-2 px-3">
                  <div className="truncate" title={row.creator}>
                    {row.creator}
                  </div>
                </td>
                <td className="py-2 px-3 text-center space-x-2">
                  <button
                    className="text-blue-500 hover:text-blue-700"
                    onClick={() => openViewModal(row)}
                  >
                    <Eye size={15} />
                  </button>
                  <button
                    className="text-orange-500 hover:text-orange-700"
                    onClick={() => openEditModal(row)}
                  >
                    <Edit size={15} />
                  </button>
                  <button
                    className="text-red-500 hover:text-red-700"
                    onClick={() => {
                      setDeleteId(row.id);
                      setShowDeleteModal(true);
                    }}
                  >
                    <Trash2 size={15} />
                  </button>
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
              className={`px-3 py-1 rounded border ${
                page === i + 1
                  ? "bg-red-500 text-white"
                  : "bg-gray-100 text-gray-700"
              }`}
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
        <Modal>
          <div
            ref={modalRef}
            style={{
              transform: `scale(${modalScale})`,
              transformOrigin: "center",
              transition: "transform 120ms ease",
            }}
            className="bg-white rounded-2xl w-full max-w-3xl p-4 md:p-6 shadow-lg mx-auto"
          >
            <h3 className="text-2xl text-red-500 font-bold text-center">
              {mode === "view"
                ? "Detail Test Order Information"
                : mode === "edit"
                ? "UPDATE TEST ORDER"
                : "NEW TEST ORDER"}
            </h3>
            <p className="text-center text-sm text-gray-500 mb-6">
              {mode === "view"
                ? "View patient information for this test order"
                : mode === "edit"
                ? "Update patient information for this test order"
                : "Enter patient information to create a new test order"}
            </p>

            <div className="border rounded-lg p-6 bg-gray-50">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Patient Name
                  </label>
                  <input
                    name="patientName"
                    value={localForm.patientName}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white"
                  />
                  {errors.patientName && (
                    <div className="text-sm text-red-600 mt-1">
                      {errors.patientName}
                    </div>
                  )}
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Date of Birth
                  </label>
                  {mode === "view" ? (
                    <div className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white">
                      {form.dob}
                    </div>
                  ) : (
                    <input
                      name="dob"
                      value={localForm.dob}
                      onChange={(e) => {
                        const { name, value } = e.target;
                        setLocalForm((s) => ({ ...s, [name]: value }));
                        setErrors((s) => ({ ...s, [name]: undefined }));
                      }}
                      type="date"
                      className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm"
                    />
                  )}
                  {errors.dob && (
                    <div className="text-sm text-red-600 mt-1">
                      {errors.dob}
                    </div>
                  )}
                </div>

                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Phone Number
                  </label>
                  <input
                    name="phone"
                    value={localForm.phone}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white"
                  />
                  {errors.phone && (
                    <div className="text-sm text-red-600 mt-1">
                      {errors.phone}
                    </div>
                  )}
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Email
                  </label>
                  <input
                    name="email"
                    value={localForm.email}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    type="email"
                    readOnly={mode === "view"}
                    className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white"
                  />
                  {errors.email && (
                    <div className="text-sm text-red-600 mt-1">
                      {errors.email}
                    </div>
                  )}
                </div>

                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Gender
                  </label>
                  {mode === "view" ? (
                    <div className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white">
                      {form.gender
                        ? form.gender === "MALE"
                          ? "Male"
                          : form.gender === "FEMALE"
                          ? "Female"
                          : form.gender
                        : ""}
                    </div>
                  ) : (
                    <select
                      name="gender"
                      value={localForm.gender}
                      onChange={(e) => {
                        const { name, value } = e.target;
                        setLocalForm((s) => ({ ...s, [name]: value }));
                        setErrors((s) => ({ ...s, [name]: undefined }));
                      }}
                      className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm"
                    >
                      <option value="">Select</option>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                  )}
                  {errors.gender && (
                    <div className="text-sm text-red-600 mt-1">
                      {errors.gender}
                    </div>
                  )}
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Status
                  </label>
                  {mode === "view" ? (
                    <div className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white">
                      {form.status || ""}
                    </div>
                  ) : (
                    <select
                      name="status"
                      value={localForm.status}
                      onChange={(e) => {
                        const { name, value } = e.target;
                        setLocalForm((s) => ({ ...s, [name]: value }));
                        setErrors((s) => ({ ...s, [name]: undefined }));
                      }}
                      className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm"
                    >
                      <option value="">Select</option>
                      <option value="Pending">Pending</option>
                      <option value="Completed">Completed</option>
                      <option value="Cancelled">Cancelled</option>
                    </select>
                  )}
                  {errors.status && (
                    <div className="text-sm text-red-600 mt-1">
                      {errors.status}
                    </div>
                  )}
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Address
                  </label>
                  <input
                    name="address"
                    value={localForm.address}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white"
                  />
                </div>

                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Country
                  </label>
                  <input
                    name="country"
                    value={localForm.country}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white"
                  />
                </div>
                <div>
                  <label className="text-red-500 font-semibold text-sm">
                    Citizen ID
                  </label>
                  <input
                    name="citizenId"
                    value={localForm.citizenId}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 p-2 border border-gray-200 rounded-lg text-sm bg-white"
                  />
                  {errors.citizenId && (
                    <div className="text-sm text-red-600 mt-1">
                      {errors.citizenId}
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-4 mt-6">
              <button
                onClick={() => {
                  setShowModal(false);
                  setMode("create");
                  setErrors({});
                }}
                className="px-4 py-2 border border-gray-200 rounded-lg bg-white"
              >
                Close
              </button>
              {mode === "edit" ? (
                <button
                  onClick={() => handleUpdate(localForm)}
                  className="px-4 py-2 bg-red-500 text-white rounded-lg"
                >
                  Save
                </button>
              ) : mode === "view" ? null : (
                <button
                  onClick={() => handleCreate(localForm)}
                  className="px-4 py-2 bg-red-500 text-white rounded-lg"
                >
                  Create
                </button>
              )}
            </div>
          </div>
        </Modal>
      )}
      {/* Xoá - Modal xác nhận */}
      <DeleteConfirmationModal
        open={showDeleteModal}
        orderId={deleteId}
        onCancel={() => setShowDeleteModal(false)}
        onConfirm={async () => {
          try {
            const res = await fetch(
              `http://localhost:6868/api/test-orders/${deleteId}?page=${page}&size=${PAGE_SIZE}&keyword=${keyword}&sortBy=patientName&sortDir=${sortDir}`,
              { method: "DELETE" }
            );
            if (!res.ok) {
              const text = await res.text();
              setDeleteError(text || "Failed to remove test order!");
              setDeleteMsg("");
              setTimeout(() => {
                setDeleteError("");
                setDeleteId(null);
              }, 3000);
            } else {
              const data = await res.json();
              if (data?.result) {
                const items = data.result.items || [];
                
                // Nếu có items trong response, cập nhật danh sách
                if (items.length > 0) {
                  setTotalPages(data.result.totalPages || 1);
                  setOrders(
                    items.map((order) => ({
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
                    }))
                  );
                } else {
                  // Nếu result trả về mảng rỗng và không phải trang 1, gọi lại API với page nhỏ hơn 1 đơn vị
                  if (page > 1) {
                    setPage(page - 1);
                    fetchOrdersWrapper(page - 1);
                  }
                  // Nếu đang ở trang 1 và không có test order nào thì không cần gọi lại API
                }
              }
              setSuccessMsg("Delete test order successfully!");
              setDeleteError("");
              setShowDeleteModal(false);
              setDeleteId(null);
              setTimeout(() => setSuccessMsg(""), 3000);
            }
          } catch (err) {
            console.warn("Delete failed", err);
            setDeleteError("Network error or server error!");
            setDeleteMsg("");
            setTimeout(() => {
              setDeleteError("");
              setDeleteId(null);
            }, 3000);
          }
        }}
      />
      {/* Thông báo thành công xoá */}
      {deleteMsg && (
        <div className="fixed left-1/2 top-5 -translate-x-1/2 bg-green-100 text-green-700 px-6 py-2 shadow-lg rounded-lg z-50 font-semibold">
          {deleteMsg}
        </div>
      )}
      {deleteError && (
        <div className="fixed left-1/2 top-5 -translate-x-1/2 bg-red-100 text-red-600 px-6 py-2 shadow-lg rounded-lg z-50 font-semibold">
          {deleteError}
        </div>
      )}
    </div>
  );
}
