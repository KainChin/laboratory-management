import { Eye, Edit, Trash2, Filter, Search } from "lucide-react";
import {
  useState,
  useEffect,
  useCallback,
  useRef,
  useLayoutEffect,
} from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import axios from "../../api/axios";
import DeleteConfirmationModal from "./DeleteConfirmationModal";
import Loading from "../Loading";
import { showToast } from "../Toast";

// Modal portal so the overlay covers the whole viewport
function Modal({ children, onBackdropClick, contentRef }) {
  // Logic Focus Trapping & Escape key
  useEffect(() => {
    const modal = contentRef.current;
    if (!modal) return;

    // Tìm tất cả các phần tử có thể focus được
    const focusableElements = modal.querySelectorAll(
      'button:not([disabled]), [href], input:not([disabled]):not([type="hidden"]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
    );
    const firstFocusable = focusableElements[0];
    const lastFocusable = focusableElements[focusableElements.length - 1];

    // !!! ĐÃ LOẠI BỎ LOGIC TỰ ĐỘNG FOCUS Ở ĐÂY.
    // Việc focus ban đầu được thực hiện trong OrdersTable bằng patientNameRef.

    const handleKeyDown = (e) => {
      if (e.key === "Tab") {
        if (e.shiftKey) {
          if (document.activeElement === firstFocusable) {
            e.preventDefault();
            lastFocusable.focus();
          }
        } else {
          if (document.activeElement === lastFocusable) {
            e.preventDefault();
            firstFocusable.focus();
          }
        }
      }
      if (e.key === "Escape" && onBackdropClick) {
        onBackdropClick(e);
      }
    };

    document.body.style.overflow = "hidden";
    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "";
    };
  }, [onBackdropClick, contentRef]);

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-2"
      onClick={onBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
      style={{
        animation: "fadeIn 0.3s ease-out",
      }}
    >
      <style>
        {`
          @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
          }
          @keyframes slideUp {
            from { opacity: 0; transform: translateY(30px) scale(0.95); }
            to { opacity: 1; transform: translateY(0) scale(1); }
          }
        `}
      </style>
      <div
        ref={contentRef} // Áp dụng ref cho container nội dung
        onClick={(e) => e.stopPropagation()}
      >
        {children}
      </div>
    </div>,
    document.body
  );
}

export default function OrdersTable() {
  const navigate = useNavigate();
  const [isNavigating, setIsNavigating] = useState(false);
  const [totalPages, setTotalPages] = useState(1);
  const [keyword, setKeyword] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [debouncedKeyword, setDebouncedKeyword] = useState("");
  const [sortDir, setSortDir] = useState("asc");
  const PAGE_SIZE = 5;
  const [page, setPage] = useState(1);
  const [jumpInput, setJumpInput] = useState("");
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
          const res = await axios.get(
            `/test-orders?${params}`
          );
          const result = res.data;
          const items = result.result?.items || [];
          setTotalPages(result.result?.totalPages || 1);
          setOrders(
            items.map((order) => ({
              id: order.testOrderId,
              name: order.patientName,
              status: order.status || "Pending",
              date: order.dateOfBirth,
              creator: order.createdBy || "Unknown",
              dob: order.dateOfBirth || "",
              phone: order.phone,
              email: order.email,
              gender: order.gender,
              address: order.address,
              country: order.country,
              citizenId: order.citizenId,
            }))
          );
          try {
            const mapped = items.map((order) => ({
              id: order.testOrderId,
              status: order.status || "Pending",
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

    if (localStorage.getItem("scrollToTable")) {
      localStorage.removeItem("scrollToTable");

      setTimeout(() => {
        const tableSection = document.querySelector("table");
        const headerSection = document.querySelector(
          ".flex.justify-between.items-center.mb-3"
        );

        if (tableSection && headerSection) {
          const headerOffset =
            headerSection.getBoundingClientRect().top + window.pageYOffset;

          window.scrollTo({
            top: headerOffset - 100,
            behavior: "smooth",
          });
        }
      }, 100);
    }
  }, [fetchOrdersWrapper]);

  // Reset selectedIndex khi orders thay đổi
  useEffect(() => {
    setSelectedIndex(-1);
  }, [orders.length, page]);

  useEffect(() => {
    const h = setTimeout(() => {
      setDebouncedKeyword(searchInput.trim());
    }, 500);
    return () => clearTimeout(h);
  }, [searchInput]);

  useEffect(() => {
    if (debouncedKeyword !== keyword) {
      setKeyword(debouncedKeyword);
      setPage(1);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedKeyword]);

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
  const [isSubmitting, setIsSubmitting] = useState(false);
  const modalRef = useRef(null);
  const patientNameRef = useRef(null); // REF MỚI CHO INPUT PATIENT NAME
  const dobRef = useRef(null);
  const phoneRef = useRef(null);
  const emailRef = useRef(null);
  const genderRef = useRef(null);
  const statusRef = useRef(null);
  const addressRef = useRef(null);
  const countryRef = useRef(null);
  const citizenIdRef = useRef(null);
  const closeButtonRef = useRef(null);
  const createButtonRef = useRef(null);
  const saveButtonRef = useRef(null);
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
  const [localForm, setLocalForm] = useState(form);
  const [errors, setErrors] = useState({});
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    if (showModal) {
      setLocalForm(form);
      setErrors({});
      // Lệnh focus trực tiếp vào input Patient Name (chỉ khi create/edit)
      if ((mode === "create" || mode === "edit") && patientNameRef.current) {
        setTimeout(() => patientNameRef.current.focus(), 0);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showModal, mode, editingId]); // Chạy khi Modal mở

  // Keyboard navigation trong modal
  useEffect(() => {
    if (!showModal || mode === "view") return;

    // Mảng các refs theo thứ tự trong form (grid 2 cột)
    const formFields = [
      patientNameRef, // Cột 1, hàng 1
      dobRef, // Cột 2, hàng 1
      phoneRef, // Cột 1, hàng 2
      emailRef, // Cột 2, hàng 2
      genderRef, // Cột 1, hàng 3
      statusRef, // Cột 2, hàng 3
      addressRef, // Cột 1, hàng 4
      countryRef, // Cột 2, hàng 4
      citizenIdRef, // Cột 1, hàng 5 (full width)
    ];

    function handleModalKeyDown(e) {
      const activeElement = document.activeElement;
      const isTextarea = activeElement?.tagName === "TEXTAREA";
      const isInput = activeElement?.tagName === "INPUT";
      const isSelect = activeElement?.tagName === "SELECT";

      // Escape → Đóng modal (Close)
      if (e.key === "Escape") {
        e.preventDefault();
        if (closeButtonRef.current) {
          closeButtonRef.current.click();
        }
        return;
      }

      // ArrowDown → Chuyển sang input tiếp theo
      // Chỉ với text/email inputs (không phải date, không phải select)
      // Hoặc dùng Alt+Arrow để điều hướng trong date/select
      if (
        e.key === "ArrowDown" &&
        isInput &&
        (activeElement?.type === "text" || activeElement?.type === "email")
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        if (currentIndex >= 0 && currentIndex < formFields.length - 1) {
          const nextRef = formFields[currentIndex + 1];
          if (nextRef.current) {
            nextRef.current.focus();
          }
        }
        return;
      }
      // Alt+ArrowDown → Điều hướng với date/select/email
      if (
        (e.altKey || e.metaKey) &&
        e.key === "ArrowDown" &&
        (isInput || isSelect)
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        if (currentIndex >= 0 && currentIndex < formFields.length - 1) {
          const nextRef = formFields[currentIndex + 1];
          if (nextRef.current) {
            nextRef.current.focus();
          }
        }
        return;
      }

      // ArrowUp → Chuyển về input trước
      // Chỉ với text/email inputs
      if (
        e.key === "ArrowUp" &&
        isInput &&
        (activeElement?.type === "text" || activeElement?.type === "email")
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        if (currentIndex > 0) {
          const prevRef = formFields[currentIndex - 1];
          if (prevRef.current) {
            prevRef.current.focus();
          }
        }
        return;
      }
      // Alt+ArrowUp → Điều hướng với date/select/email
      if (
        (e.altKey || e.metaKey) &&
        e.key === "ArrowUp" &&
        (isInput || isSelect)
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        if (currentIndex > 0) {
          const prevRef = formFields[currentIndex - 1];
          if (prevRef.current) {
            prevRef.current.focus();
          }
        }
        return;
      }

      // ArrowRight → Chuyển sang input bên phải (trong cùng hàng)
      // Chỉ với text/email inputs
      if (
        e.key === "ArrowRight" &&
        isInput &&
        (activeElement?.type === "text" || activeElement?.type === "email")
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        // Trong grid 2 cột, input bên phải là index + 1 (nếu không phải cột cuối của hàng)
        // Logic: hàng 1 (0,1), hàng 2 (2,3), hàng 3 (4,5), hàng 4 (6,7), hàng 5 (8)
        if (currentIndex >= 0) {
          // Nếu là cột đầu tiên của hàng (index chẵn và không phải 8)
          if (currentIndex % 2 === 0 && currentIndex < 8) {
            const rightRef = formFields[currentIndex + 1];
            if (rightRef.current) {
              rightRef.current.focus();
            }
          }
        }
        return;
      }
      // Alt+ArrowRight → Điều hướng với date/select/email
      if (
        (e.altKey || e.metaKey) &&
        e.key === "ArrowRight" &&
        (isInput || isSelect)
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        if (currentIndex >= 0 && currentIndex % 2 === 0 && currentIndex < 8) {
          const rightRef = formFields[currentIndex + 1];
          if (rightRef.current) {
            rightRef.current.focus();
          }
        }
        return;
      }

      // ArrowLeft → Chuyển sang input bên trái (trong cùng hàng)
      // Chỉ với text/email inputs
      if (
        e.key === "ArrowLeft" &&
        isInput &&
        (activeElement?.type === "text" || activeElement?.type === "email")
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        // Nếu là cột thứ 2 của hàng (index lẻ)
        if (currentIndex > 0 && currentIndex % 2 === 1) {
          const leftRef = formFields[currentIndex - 1];
          if (leftRef.current) {
            leftRef.current.focus();
          }
        }
        return;
      }
      // Alt+ArrowLeft → Điều hướng với date/select/email
      if (
        (e.altKey || e.metaKey) &&
        e.key === "ArrowLeft" &&
        (isInput || isSelect)
      ) {
        e.preventDefault();
        const currentIndex = formFields.findIndex(
          (ref) => ref.current === activeElement
        );
        if (currentIndex > 0 && currentIndex % 2 === 1) {
          const leftRef = formFields[currentIndex - 1];
          if (leftRef.current) {
            leftRef.current.focus();
          }
        }
        return;
      }

      // Enter → Submit form (Create/Save) - chỉ khi focus vào button
      // (Input fields sẽ xử lý Enter riêng, đặc biệt là citizenId input)
      if (e.key === "Enter" && !e.shiftKey && !isTextarea) {
        // Chỉ submit khi focus vào button (không phải input/select)
        if (activeElement?.tagName === "BUTTON") {
          e.preventDefault();
          if (mode === "edit" && saveButtonRef.current && !isSubmitting) {
            saveButtonRef.current.click();
          } else if (
            mode === "create" &&
            createButtonRef.current &&
            !isSubmitting
          ) {
            createButtonRef.current.click();
          }
        }
        return;
      }

      // Ctrl+Enter hoặc Cmd+Enter → Luôn submit (bỏ qua focus check)
      if (
        (e.ctrlKey || e.metaKey) &&
        e.key === "Enter" &&
        !isSubmitting
      ) {
        e.preventDefault();
        if (mode === "edit" && saveButtonRef.current) {
          saveButtonRef.current.click();
        } else if (mode === "create" && createButtonRef.current) {
          createButtonRef.current.click();
        }
        return;
      }
    }

    window.addEventListener("keydown", handleModalKeyDown);
    return () => {
      window.removeEventListener("keydown", handleModalKeyDown);
    };
  }, [showModal, mode, isSubmitting]);

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

  function formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    if (isNaN(d)) return dateStr;
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  function formatDateForBackend(dateStr) {
    if (!dateStr) return "";
    const parts = dateStr.split("-");
    if (parts.length !== 3) return dateStr;
    const [yyyy, mm, dd] = parts;
    return `${dd}/${mm}/${yyyy}`;
  }

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
    return String(g).toUpperCase();
  }

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

  function parseBackendDateToInput(dateStr) {
    if (!dateStr) return "";
    const parts = dateStr.split("/");
    if (parts.length !== 3) return dateStr;
    const [dd, mm, yyyy] = parts;
    return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
  }

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
    } else if (!/^(\+\d{1,3}[- ]?)?\d{10}$/.test(f.phone.replace(/\s/g, ''))) {
      e.phone = "Phone number is invalid. Must be 10 digits with optional country code (e.g., +84 or +1)";
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
    if (!f.address || !String(f.address).trim()) {
      e.address = "Address is required";
    }
    if (!f.country || !String(f.country).trim()) {
      e.country = "Country is required";
    }
    return e;
  }

  const openCreateModal = useCallback(() => {
    setMode("create");
    setEditingId(null);
    resetForm();
    setShowModal(true);
  }, []);

  const openEditModal = useCallback((order) => {
    setMode("edit");
    setEditingId(order.id);
    setForm({
      patientName: order.name || "",
      dob: order.dob ? parseBackendDateToInput(order.dob) : "",
      phone: order.phone || "",
      email: order.email || "",
      gender: enumToSelectGender(order.gender),
      status: enumToSelectStatus(order.status),
      address: order.address || "",
      country: order.country || "",
      citizenId: order.citizenId || "",
    });
    setShowModal(true);
  }, []);

  const openViewModal = useCallback((order) => {
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
  }, []);

  async function handleCreate(currentForm) {
    if (isSubmitting) return;

    const f = currentForm || localForm;
    const validation = validateForm(f);
    if (Object.keys(validation).length > 0) {
      setErrors(validation);
      return;
    }

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

    setIsSubmitting(true);

    try {
      const res = await axios.post("/test-orders", payload);
      const result = res.data;
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
      showToast({
        type: "success",
        title: "Success",
        message: "Create test order successfully",
      });
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
      fetchOrdersWrapper(1);
      setPage(1);
    } catch (err) {
      showToast({
        type: "error",
        title: "Create Failed",
        message: err.message || "Failed to create test order",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleUpdate(currentForm) {
    if (!editingId) {
      alert("No order selected to edit");
      return;
    }
    const f = currentForm || localForm;
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
      country: f.country || undefined,
      email: f.email || undefined,
      citizenId: f.citizenId || undefined,
    };

    (async () => {
      try {
        const res = await axios.put(
          `/test-orders/${editingId}`,
          payload
        );
        const result = res.data;
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
                  country: updated.country || f.country || o.country,
                  citizenId: updated.citizenId || f.citizenId || o.citizenId,
                }
              : o
          )
        );

        setShowModal(false);
        setEditingId(null);
        showToast({
          type: "success",
          title: "Success",
          message: "Test order updated successfully",
        });
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
  }, [showModal, mode, modalScale]);

  // ---- Keyboard Navigation ----
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const searchInputRef = useRef(null);

  useEffect(() => {
    function handleKey(e) {
      // Nếu đang focus vào input/textarea/select → bỏ qua (trừ một số phím đặc biệt)
      const activeElement = document.activeElement;
      const isInputFocused =
        activeElement &&
        (activeElement.tagName === "INPUT" ||
          activeElement.tagName === "TEXTAREA" ||
          activeElement.tagName === "SELECT");

      // Nếu modal đang mở → ưu tiên modal
      if (showModal) return;

      // Ctrl+N hoặc N → Tạo mới (chỉ khi không focus vào input)
      if ((e.ctrlKey && e.key === "n") || (e.key === "n" && !isInputFocused)) {
        e.preventDefault();
        openCreateModal();
        return;
      }

      // Ctrl+F hoặc / → Focus vào search
      if (
        (e.ctrlKey && e.key === "f") ||
        (e.key === "/" && !isInputFocused)
      ) {
        e.preventDefault();
        if (searchInputRef.current) {
          searchInputRef.current.focus();
          searchInputRef.current.select();
        }
        return;
      }

      // Không có dữ liệu → bỏ qua các phím điều hướng
      if (!orders || orders.length === 0) {
        // Vẫn cho phép Ctrl+N và Ctrl+F
        return;
      }

      // ↓ Move down
      if (e.key === "ArrowDown" && !isInputFocused) {
        e.preventDefault();
        const newIndex = Math.min(selectedIndex + 1, orders.length - 1);
        setSelectedIndex(newIndex);
        // Scroll to selected row
        setTimeout(() => {
          const rowElement = document.querySelector(
            `tr[data-row-index="${newIndex}"]`
          );
          if (rowElement) {
            rowElement.scrollIntoView({ behavior: "smooth", block: "nearest" });
          }
        }, 0);
        return;
      }

      // ↑ Move up
      if (e.key === "ArrowUp" && !isInputFocused) {
        e.preventDefault();
        const newIndex = Math.max(selectedIndex - 1, 0);
        setSelectedIndex(newIndex);
        // Scroll to selected row
        setTimeout(() => {
          const rowElement = document.querySelector(
            `tr[data-row-index="${newIndex}"]`
          );
          if (rowElement) {
            rowElement.scrollIntoView({ behavior: "smooth", block: "nearest" });
          }
        }, 0);
        return;
      }

      // → Next page
      if (e.key === "ArrowRight" && !isInputFocused) {
        e.preventDefault();
        if (page < totalPages) {
          setPage(page + 1);
          setSelectedIndex(-1); // Reset selection khi chuyển trang
        }
        return;
      }

      // ← Previous page
      if (e.key === "ArrowLeft" && !isInputFocused) {
        e.preventDefault();
        if (page > 1) {
          setPage(page - 1);
          setSelectedIndex(-1); // Reset selection khi chuyển trang
        }
        return;
      }

      // Enter → view (khi có item được chọn)
      if (e.key === "Enter" && selectedIndex >= 0 && !isInputFocused) {
        e.preventDefault();
        openViewModal(orders[selectedIndex]);
        return;
      }

      // E → Edit (khi có item được chọn)
      if (e.key === "e" && selectedIndex >= 0 && !isInputFocused) {
        e.preventDefault();
        openEditModal(orders[selectedIndex]);
        return;
      }

      // V → View (khi có item được chọn)
      if (e.key === "v" && selectedIndex >= 0 && !isInputFocused) {
        e.preventDefault();
        openViewModal(orders[selectedIndex]);
        return;
      }

      // Delete hoặc D → open delete modal
      if (
        (e.key === "Delete" || (e.key === "d" && !isInputFocused)) &&
        selectedIndex >= 0
      ) {
        e.preventDefault();
        setDeleteId(orders[selectedIndex].id);
        setShowDeleteModal(true);
        return;
      }

      // Esc → clear selection
      if (e.key === "Escape" && !isInputFocused) {
        setSelectedIndex(-1);
        return;
      }

      // Home → First row
      if (e.key === "Home" && !isInputFocused) {
        e.preventDefault();
        setSelectedIndex(0);
        return;
      }

      // End → Last row
      if (e.key === "End" && !isInputFocused) {
        e.preventDefault();
        setSelectedIndex(orders.length - 1);
        return;
      }
    }

    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, [
    orders,
    selectedIndex,
    showModal,
    page,
    totalPages,
    openCreateModal,
    openViewModal,
    openEditModal,
  ]);

  return (
    <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 relative">
      {isNavigating && <Loading />}
      <div className="flex justify-between items-center mb-3">
        <div>
          <h2 className="text-lg text-[#FF5A5A] font-semibold">
            Test Order Lists
          </h2>
          <p className="text-xs text-gray-500 mt-1">
            Keyboard shortcuts: ↑↓ to navigate, Enter/V to view, E to edit, D/Delete to delete, N to create, / to search
          </p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={openCreateModal}
            className="bg-[#FF5A5A] text-white px-3 min-h-[40px] py-2 rounded-lg hover:bg-[#FF3A3A] transition-colors duration-300"
            title="Create new test order (Press N)"
          >
            + New Test Order
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 mb-3">
        <div className="relative flex-1">
          <Search size={24} className="absolute left-2 top-2 text-gray-400" />
          <input
            ref={searchInputRef}
            type="text"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                setDebouncedKeyword(searchInput.trim());
              }
            }}
            placeholder="Search patient name... (Press / to focus)"
            className="w-full pl-8 pr-3 min-h-[40px] py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-red-300"
          />
        </div>
        <button
          className="bg-red-100 text-[#FF5A5A] px-3 min-h-[40px] py-2 rounded-lg flex items-center gap-1 hover:bg-red-200 hover:text-[#FF3A3A] transition-colors duration-300 text-sm"
          onClick={() => {
            setDebouncedKeyword(searchInput.trim());
          }}
        >
          <Filter size={24} /> Search
        </button>
        <button
          className="bg-gray-100 text-gray-700 px-3 min-h-[40px] py-2 rounded-lg ml-2 border text-sm"
          onClick={() => {
            setSortDir(sortDir === "asc" ? "desc" : "asc");
            setPage(1);
          }}
        >
          Sort: {sortDir === "asc" ? "A-Z" : "Z-A"}
        </button>
      </div>

      <div className="overflow-x-auto">
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
            <tr className="bg-[#FF5A5A] text-white">
              <th className="min-h-[40px] py-3 px-3 rounded-tl-lg font-bold text-base">
                <div className="truncate">Name</div>
              </th>
              <th className="min-h-[40px] py-3 px-3 font-bold text-base">
                <div className="truncate">Status</div>
              </th>
              <th className="min-h-[40px] py-3 px-3 font-bold text-base">
                <div className="truncate">Date of Birth</div>
              </th>
              <th className="min-h-[40px] py-3 px-3 font-bold text-base">
                <div className="truncate">Created By</div>
              </th>
              <th className="min-h-[40px] py-3 px-3 rounded-tr-lg text-center font-bold text-base">
                <div className="truncate">Action</div>
              </th>
            </tr>
          </thead>
          <tbody>
            {orders.length === 0 ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-gray-500">
                  No patient data available
                </td>
              </tr>
            ) : (
              orders.map((row, index) => (
                <tr
                  key={row.id}
                  data-row-index={index}
                  onClick={() => setSelectedIndex(index)}
                  className={`border-b hover:bg-gray-50 transition-colors cursor-pointer ${
                    selectedIndex === index
                      ? "bg-blue-100 hover:bg-blue-200"
                      : ""
                  }`}
                >
                  <td className="min-h-[40px] py-3 px-3">
                    <div
                      className="truncate font-bold hover:text-[#FF5A5A] cursor-pointer transition-colors"
                      title={row.name}
                      role="button"
                      tabIndex={0}
                      onKeyDown={(e) => {
                        if (e.key === "Enter" || e.key === " ") {
                          setIsNavigating(true);
                          window.scrollTo({ top: 0, behavior: "smooth" });
                          setTimeout(() => {
                            navigate(`/test-orders/detail/${row.id}`);
                          }, 500);
                        }
                      }}
                      onClick={() => {
                        setIsNavigating(true);
                        window.scrollTo({ top: 0, behavior: "smooth" });
                        setTimeout(() => {
                          navigate(`/test-orders/detail/${row.id}`);
                        }, 500);
                      }}
                    >
                      {row.name}
                    </div>
                  </td>
                  <td className="min-h-[40px] py-3 px-3">
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
                  <td className="min-h-[40px] py-3 px-3">
                    <div className="truncate" title={row.dob}>
                      {row.dob || ""}
                    </div>
                  </td>
                  <td className="min-h-[40px] py-3 px-3">
                    <div className="truncate" title={row.creator}>
                      {row.creator}
                    </div>
                  </td>
                  <td className="min-h-[40px] py-3 px-3">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        className="text-blue-500 hover:text-[#FF3A3A] transition-colors duration-300"
                        onClick={() => openViewModal(row)}
                        aria-label="View Order Details"
                      >
                        <Eye size={24} />
                      </button>
                      <button
                        className="text-orange-500 hover:text-[#FF3A3A] transition-colors duration-300"
                        onClick={() => openEditModal(row)}
                        aria-label="Edit Order"
                      >
                        <Edit size={24} />
                      </button>
                      <button
                        className="text-[#FF5A5A] hover:text-[#FF3A3A] transition-colors duration-300"
                        onClick={() => {
                          setDeleteId(row.id);
                          setShowDeleteModal(true);
                        }}
                        aria-label={`Delete Order ${row.id}`}
                      >
                        <Trash2 size={24} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination controls */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center mt-4 gap-2">
          {page > 1 && (
            <button
              className="px-3 min-h-[40px] py-2 rounded border bg-gray-100 text-gray-700 hover:bg-gray-200 hover:border-[#FF3A3A]"
              onClick={() => setPage(page - 1)}
            >
              Prev
            </button>
          )}
          {(() => {
            function getItems(current, total) {
              if (total <= 7) {
                return Array.from({ length: total }, (_, i) => i + 1);
              }

              const items = [];
              const first = 1;
              const last = total;
              const left = Math.max(2, current - 1);
              const right = Math.min(total - 1, current + 1);

              items.push(first);

              if (left > 2) {
                items.push("...");
              } else {
                for (let i = 2; i < left; i++) {
                  items.push(i);
                }
              }

              for (let i = left; i <= right; i++) {
                if (i > first && i < last) {
                  items.push(i);
                }
              }

              if (right < last - 1) {
                if (right === last - 2) {
                  items.push(last - 1);
                } else {
                  items.push("...");
                }
              }

              items.push(last);

              return items.filter((value, index, self) => {
                return index === 0 || value !== self[index - 1];
              });
            }
            const items = getItems(page, totalPages);
            return items.map((it, idx) =>
              it === "..." ? (
                <span key={`el-${idx}`} className="px-2 text-gray-500">
                  …
                </span>
              ) : (
                <button
                  key={it}
                  className={`px-3 min-h-[40px] py-2 rounded border ${
                    page === it
                      ? "bg-[#FF5A5A] text-white"
                      : "bg-gray-100 text-gray-700"
                  }`}
                  onClick={() => setPage(it)}
                  aria-label={`Go to page ${it}`}
                >
                  {it}
                </button>
              )
            );
          })()}
          {page < totalPages && (
            <button
              className="px-3 min-h-[40px] py-2 rounded border bg-gray-100 text-gray-700 hover:bg-gray-200 hover:border-[#FF3A3A]"
              onClick={() => setPage(page + 1)}
            >
              Next
            </button>
          )}
          <div className="flex items-center gap-1 ml-2">
            <input
              type="number"
              min={1}
              max={totalPages}
              value={jumpInput}
              onChange={(e) => setJumpInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  const num = parseInt(jumpInput || "", 10);
                  if (!isNaN(num)) {
                    const nextVal = Math.min(Math.max(1, num), totalPages);
                    setPage(nextVal);
                  }
                }
              }}
              placeholder="Page"
              className="w-20 px-2 min-h-[40px] py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-1 focus:ring-red-300"
            />
            <button
              className="px-3 min-h-[40px] py-2 rounded border bg-gray-100 text-gray-700 hover:bg-gray-200 hover:border-[#FF3A3A]"
              onClick={() => {
                const num = parseInt(jumpInput || "", 10);
                if (!isNaN(num)) {
                  const nextVal = Math.min(Math.max(1, num), totalPages);
                  setPage(nextVal);
                }
              }}
            >
              Go
            </button>
          </div>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <Modal
          onBackdropClick={() => {
            setShowModal(false);
            setMode("create");
            setErrors({});
          }}
          contentRef={modalRef}
        >
          <div
            ref={modalRef}
            style={{
              transform: `scale(${modalScale})`,
              transformOrigin: "center",
              transition: "transform 120ms ease",
              animation: "slideUp 0.4s ease-out",
            }}
            className="bg-white rounded-2xl w-full max-w-3xl p-4 md:p-6 shadow-lg mx-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <h3
              id="modal-title"
              className="text-2xl text-[#FF5A5A] font-bold text-center"
            >
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
                  <label
                    htmlFor="patientName"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Patient Name
                  </label>
                  <input
                    ref={patientNameRef} // ÁP DỤNG REF CHO INPUT ĐẦU TIÊN
                    id="patientName"
                    name="patientName"
                    value={localForm.patientName}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white"
                    placeholder="Enter patient's full name (e.g., John Doe)"
                  />
                  {errors.patientName && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.patientName}
                    </div>
                  )}
                </div>
                <div>
                  <label
                    htmlFor="dob"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Date of Birth
                  </label>
                  {mode === "view" ? (
                    <div className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white flex items-center">
                      {localForm.dob}
                    </div>
                  ) : (
                    <input
                      ref={dobRef}
                      id="dob"
                      name="dob"
                      value={localForm.dob}
                      onChange={(e) => {
                        const { name, value } = e.target;
                        setLocalForm((s) => ({ ...s, [name]: value }));
                        setErrors((s) => ({ ...s, [name]: undefined }));
                      }}
                      type="date"
                      className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm"
                    />
                  )}
                  {errors.dob && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.dob}
                    </div>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="phone"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Phone Number
                  </label>
                  <input
                    ref={phoneRef}
                    id="phone"
                    name="phone"
                    value={localForm.phone}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white"
                    placeholder="Enter phone number (e.g., +84 123 456 789)"
                  />
                  {errors.phone && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.phone}
                    </div>
                  )}
                </div>
                <div>
                  <label
                    htmlFor="email"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Email
                  </label>
                  <input
                    ref={emailRef}
                    id="email"
                    name="email"
                    value={localForm.email}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    type="email"
                    readOnly={mode === "view"}
                    className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white"
                    placeholder="Enter email address (e.g., patient@example.com)"
                  />
                  {errors.email && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.email}
                    </div>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="gender"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Gender
                  </label>
                  {mode === "view" ? (
                    <div className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white flex items-center">
                      {localForm.gender
                        ? localForm.gender === "MALE" || localForm.gender === "Male"
                          ? "Male"
                          : localForm.gender === "FEMALE" || localForm.gender === "Female"
                          ? "Female"
                          : localForm.gender
                        : ""}
                    </div>
                  ) : (
                    <select
                      ref={genderRef}
                      id="gender"
                      name="gender"
                      value={localForm.gender}
                      onChange={(e) => {
                        const { name, value } = e.target;
                        setLocalForm((s) => ({ ...s, [name]: value }));
                        setErrors((s) => ({ ...s, [name]: undefined }));
                      }}
                      className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm"
                    >
                      <option value="">Select</option>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                  )}
                  {errors.gender && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.gender}
                    </div>
                  )}
                </div>
                <div>
                  <label
                    htmlFor="status"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Status
                  </label>
                  {mode === "view" ? (
                    <div className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white flex items-center">
                      {localForm.status || ""}
                    </div>
                  ) : (
                    <select
                      ref={statusRef}
                      id="status"
                      name="status"
                      value={localForm.status}
                      onChange={(e) => {
                        const { name, value } = e.target;
                        setLocalForm((s) => ({ ...s, [name]: value }));
                        setErrors((s) => ({ ...s, [name]: undefined }));
                      }}
                      className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm"
                    >
                      <option value="">Select</option>
                      <option value="Pending">Pending</option>
                      <option value="Completed">Completed</option>
                      <option value="Cancelled">Cancelled</option>
                    </select>
                  )}
                  {errors.status && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.status}
                    </div>
                  )}
                </div>
                <div>
                  <label
                    htmlFor="address"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Address
                  </label>
                  <input
                    ref={addressRef}
                    id="address"
                    name="address"
                    value={localForm.address}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white"
                    placeholder="Enter full address (e.g., 123 Main St, District 1)"
                  />
                  {errors.address && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.address}
                    </div>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="country"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Country
                  </label>
                  <input
                    ref={countryRef}
                    id="country"
                    name="country"
                    value={localForm.country}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white"
                    placeholder="Enter country (e.g., Vietnam)"
                  />
                  {errors.country && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.country}
                    </div>
                  )}
                </div>
                <div>
                  <label
                    htmlFor="citizenId"
                    className="text-[#FF5A5A] font-semibold text-sm"
                  >
                    Citizen ID
                  </label>
                  <input
                    ref={citizenIdRef}
                    id="citizenId"
                    name="citizenId"
                    value={localForm.citizenId}
                    onChange={(e) => {
                      const { name, value } = e.target;
                      setLocalForm((s) => ({ ...s, [name]: value }));
                      setErrors((s) => ({ ...s, [name]: undefined }));
                    }}
                    onKeyDown={(e) => {
                      // Enter trong input cuối cùng → Submit form
                      if (e.key === "Enter" && !e.shiftKey && mode !== "view") {
                        e.preventDefault();
                        if (mode === "edit" && saveButtonRef.current && !isSubmitting) {
                          saveButtonRef.current.click();
                        } else if (
                          mode === "create" &&
                          createButtonRef.current &&
                          !isSubmitting
                        ) {
                          createButtonRef.current.click();
                        }
                      }
                    }}
                    readOnly={mode === "view"}
                    className="w-full mt-2 min-h-[40px] p-2 border border-gray-200 rounded-lg text-sm bg-white"
                    placeholder="Enter citizen ID or passport number"
                  />
                  {errors.citizenId && (
                    <div className="text-sm mt-1" style={{ color: "#FF0000" }}>
                      {errors.citizenId}
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-4 mt-6">
              <button
                ref={closeButtonRef}
                onClick={() => {
                  setShowModal(false);
                  setMode("create");
                  setErrors({});
                  setIsSubmitting(false);
                }}
                className="px-4 min-h-[40px] py-2 border border-gray-200 rounded-lg bg-white hover:bg-gray-100 transition-colors duration-300"
                title="Close modal (Press Escape)"
              >
                Close
              </button>
              {mode === "edit" ? (
                <button
                  ref={saveButtonRef}
                  onClick={() => handleUpdate(localForm)}
                  className="px-4 min-h-[40px] py-2 bg-[#FF5A5A] text-white rounded-lg hover:bg-[#FF3A3A] transition-colors duration-300"
                  title="Save changes (Press Enter or Ctrl+Enter)"
                >
                  Save
                </button>
              ) : mode === "view" ? null : (
                <button
                  ref={createButtonRef}
                  onClick={() => handleCreate(localForm)}
                  className="px-4 min-h-[40px] py-2 bg-[#FF5A5A] text-white rounded-lg disabled:opacity-60 disabled:cursor-not-allowed hover:bg-[#FF3A3A] disabled:hover:bg-[#FF5A5A] transition-colors duration-300"
                  disabled={isSubmitting}
                  title="Create order (Press Enter or Ctrl+Enter)"
                >
                  {isSubmitting ? "Creating..." : "Create"}
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
            const res = await axios.delete(
              `/test-orders/${deleteId}?page=${page}&size=${PAGE_SIZE}&keyword=${keyword}&sortBy=patientName&sortDir=${sortDir}`
            );
            const data = res.data;
            if (data?.result) {
              const items = data.result.items || [];

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
                if (page > 1) {
                  setPage(page - 1);
                  fetchOrdersWrapper(page - 1);
                }
              }
            }
            showToast({
              type: "success",
              title: "Success",
              message: "Test order deleted successfully",
            });
            setDeleteError("");
            setShowDeleteModal(false);
            setDeleteId(null);
          } catch (err) {
            console.warn("Delete failed", err);
            const errorMsg = err.message || "Network error or server error!";
            setDeleteError(errorMsg);
            showToast({
              type: "error",
              title: "Delete Failed",
              message: errorMsg,
            });
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
        <div className="fixed left-1/2 top-5 -translate-x-1/2 bg-red-100 text-[#FF5A5A] px-6 py-2 shadow-lg rounded-lg z-50 font-semibold">
          {deleteError}
        </div>
      )}
    </div>
  );
}
