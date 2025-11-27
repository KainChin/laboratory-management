import { useState, useEffect, useCallback, useRef } from "react";
import { createPortal } from "react-dom";
import { motion } from "framer-motion";
import { Search, ChevronLeft, ChevronRight, X, User } from "lucide-react";
import axios from "../api/axios";

/**
 * PatientSelector Component
 * Hiển thị modal để chọn bệnh nhân từ danh sách có sẵn
 * Hỗ trợ tìm kiếm và phân trang
 */
export default function PatientSelector({ onSelect, onClose }) {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [page, setPage] = useState(0); // 0-based page index
  const [totalPatients, setTotalPatients] = useState(0);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const pageSize = 10;
  const searchInputRef = useRef(null);

  const totalPages = Math.ceil(totalPatients / pageSize);

  // Fetch patients từ API
  const fetchPatients = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        pageSize: pageSize.toString(),
        sort: "patientId,desc",
      });

      if (searchKeyword.trim()) {
        params.append("keyword", searchKeyword.trim());
      }

      const response = await axios.get(`http://54.206.211.154:8687/api/patients?${params}`);
      const result = response.data;

      setPatients(result.patients || []);
      setTotalPatients(result.total || 0);
    } catch (err) {
      console.error("Error fetching patients:", err);
      setError(err.response?.data?.message || "Failed to load patients");
      setPatients([]);
      setTotalPatients(0);
    } finally {
      setLoading(false);
    }
  }, [page, searchKeyword]);

  useEffect(() => {
    fetchPatients();
  }, [fetchPatients]);

  // Focus vào search input khi component mount
  useEffect(() => {
    if (searchInputRef.current) {
      setTimeout(() => searchInputRef.current.focus(), 100);
    }
  }, []);

  // Handle search với debounce
  const handleSearchChange = (e) => {
    setSearchKeyword(e.target.value);
    setPage(0); // Reset về trang đầu khi search
  };

  // Handle select patient
  const handleSelectPatient = (patient) => {
    setSelectedPatient(patient);
  };

  // Handle confirm selection
  const handleConfirm = () => {
    if (selectedPatient) {
      onSelect(selectedPatient);
    }
  };

  // Handle keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        e.preventDefault();
        onClose();
      } else if (e.key === "Enter" && selectedPatient) {
        e.preventDefault();
        handleConfirm();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [selectedPatient, onClose]);

  // Format date
  const formatDate = (dateStr) => {
    if (!dateStr) return "";
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString("vi-VN");
    } catch {
      return dateStr;
    }
  };

  // Gender display
  const getGenderDisplay = (gender) => {
    if (!gender) return "";
    if (gender === "MALE") return "Male";
    if (gender === "FEMALE") return "Female";
    return gender;
  };

  // Lock body scroll
  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, []);

  return createPortal(
    <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4">
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="absolute inset-0 bg-black/40"
        onClick={onClose}
      />
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95, y: 20 }}
        transition={{ duration: 0.2 }}
        className="relative bg-white rounded-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden shadow-2xl flex flex-col z-10"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-2xl font-bold text-[#FF5A5A]">
                Select Patient
              </h2>
              <p className="text-sm text-gray-600 mt-1">
                Choose a patient from the list below to create a test order
              </p>
            </div>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
              aria-label="Close"
            >
              <X size={24} />
            </button>
          </div>

          {/* Search Box */}
          <div className="relative">
            <Search
              size={20}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />
            <input
              ref={searchInputRef}
              type="text"
              value={searchKeyword}
              onChange={handleSearchChange}
              placeholder="Search by name, email, phone, or ID number..."
              className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#FF5A5A] focus:border-transparent"
            />
          </div>
        </div>

        {/* Patient List */}
        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#FF5A5A]"></div>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <p className="text-red-500 mb-2">{error}</p>
              <button
                onClick={fetchPatients}
                className="text-[#FF5A5A] hover:underline text-sm"
              >
                Try again
              </button>
            </div>
          ) : patients.length === 0 ? (
            <div className="text-center py-12">
              <User size={48} className="mx-auto text-gray-300 mb-4" />
              <p className="text-gray-500">No patients found</p>
              {searchKeyword && (
                <p className="text-sm text-gray-400 mt-2">
                  Try a different search term
                </p>
              )}
            </div>
          ) : (
            <div className="space-y-2">
              {patients.map((patient) => (
                <div
                  key={patient.patientId}
                  onClick={() => handleSelectPatient(patient)}
                  className={`p-4 border rounded-lg cursor-pointer transition-all ${
                    selectedPatient?.patientId === patient.patientId
                      ? "border-[#FF5A5A] bg-red-50 shadow-md"
                      : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"
                  }`}
                >
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <User size={18} className="text-[#FF5A5A]" />
                        <span className="font-bold text-gray-900">
                          {patient.fullName}
                        </span>
                      </div>
                      <div className="space-y-1 text-sm">
                        <p className="text-gray-600">
                          <span className="font-medium">Citizen ID:</span>{" "}
                          {patient.identityNumber || "N/A"}
                        </p>
                        <p className="text-gray-600">
                          <span className="font-medium">DOB:</span>{" "}
                          {formatDate(patient.dateOfBirth)}
                        </p>
                        <p className="text-gray-600">
                          <span className="font-medium">Gender:</span>{" "}
                          {getGenderDisplay(patient.gender)}
                        </p>
                      </div>
                    </div>
                    <div>
                      <div className="space-y-1 text-sm">
                        <p className="text-gray-600">
                          <span className="font-medium">Phone:</span>{" "}
                          {patient.phone || "N/A"}
                        </p>
                        <p className="text-gray-600">
                          <span className="font-medium">Email:</span>{" "}
                          {patient.email || "N/A"}
                        </p>
                        <p className="text-gray-600">
                          <span className="font-medium">Address:</span>{" "}
                          {patient.address || "N/A"}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer with Pagination */}
        <div className="p-6 border-t border-gray-200">
          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mb-4">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="p-2 rounded-lg border border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronLeft size={20} />
              </button>

              <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages} ({totalPatients} patients)
              </span>

              <button
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="p-2 rounded-lg border border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronRight size={20} />
              </button>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-end gap-3">
            <button
              onClick={onClose}
              className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirm}
              disabled={!selectedPatient}
              className="px-6 py-2 bg-[#FF5A5A] text-white rounded-lg hover:bg-[#FF3A3A] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Select Patient
            </button>
          </div>
        </div>
      </motion.div>
    </div>,
    document.body
  );
}

