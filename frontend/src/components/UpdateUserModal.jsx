import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { motion, AnimatePresence } from "framer-motion";
import { X, Check, Calendar } from "lucide-react";
import { FaUser, FaInfoCircle } from "react-icons/fa";

// Mock function - thay thế bằng utility thực tế của bạn
const getUserFromToken = () => {
  return { userName: "Nguyen Van Admin" };
};

export default function UpdateUserModal({ open, onClose, initialData }) {
  const [formData, setFormData] = useState({
    fullName: "",
    phoneNumber: "",
    gender: "Male",
    dateOfBirth: "",
    address: "",
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Load initial data when modal opens
  useEffect(() => {
    if (!open) return;

    // Try to get user info from token
    const tokenUser = getUserFromToken();

    // Mock user data (replace with API call later)
    const mockUserData = {
      fullName: initialData?.fullName || tokenUser?.userName || "Nguyen Van Admin",
      phoneNumber: initialData?.phoneNumber || "0901234567",
      gender: initialData?.gender || "Male",
      dateOfBirth: initialData?.dateOfBirth || "12/05/1995",
      address: initialData?.address || "Hanoi, Vietnam",
      role: initialData?.role || "ADMIN",
    };

    setFormData(mockUserData);
    setErrors({});
  }, [open, initialData]);

  // Lock scroll when modal is open
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
  };

  const handleGenderChange = (gender) => {
    setFormData((prev) => ({
      ...prev,
      gender,
    }));
    if (errors.gender) {
      setErrors((prev) => ({
        ...prev,
        gender: "",
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.fullName?.trim()) {
      newErrors.fullName = "Full Name is required";
    }
    if (!formData.phoneNumber?.trim()) {
      newErrors.phoneNumber = "Phone Number is required";
    }
    if (!formData.gender) {
      newErrors.gender = "Gender is required";
    }
    if (!formData.dateOfBirth?.trim()) {
      newErrors.dateOfBirth = "Date of Birth is required";
    }
    if (!formData.address?.trim()) {
      newErrors.address = "Address is required";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      // TODO: Implement API call to update user
      console.log("Update user data:", formData);
      
      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 500));
      
      // Close modal after successful update
      onClose();
    } catch (error) {
      console.error("Update failed:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    onClose();
  };

  if (!open) return null;

  const userRole = initialData?.role || formData.role || "ADMIN";

  return createPortal(
    <AnimatePresence>
      {open && (
        <>
          {/* Overlay */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 bg-black bg-opacity-60 z-[9999] pointer-events-auto"
            onClick={onClose}
          />

          {/* Modal Container */}
          <div className="fixed inset-0 flex items-center justify-center z-[10000] p-4 pointer-events-none">
            <motion.div
              initial={{ opacity: 0, scale: 0.9, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 20 }}
              transition={{ duration: 0.3, ease: "easeOut" }}
              className="bg-white rounded-lg shadow-2xl w-full pointer-events-auto overflow-hidden"
              style={{
                maxWidth: "900px",
                maxHeight: "90vh",
                borderRadius: "8px",
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex flex-col md:flex-row h-full">
                {/* Left Panel - Red (25-30% width) */}
                <motion.div
                  initial={{ x: -50, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.1, duration: 0.3 }}
                  className="bg-[#FF5A5A] text-white p-6 md:w-[200px] flex flex-col items-center justify-center relative flex-shrink-0"
                  style={{ borderRadius: "8px 0 0 8px" }}
                >
                  {/* Green Checkmark - overlap border */}
                  <div className="absolute top-4 right-0 transform translate-x-1/2 z-[100]">
                    <div className="w-6 h-6 bg-green-500 rounded-full flex items-center justify-center shadow-lg">
                      <Check className="text-white" size={14} />
                    </div>
                  </div>

                  {/* User Icon */}
                  <div className="mb-6">
                    <div className="w-24 h-24 rounded-full bg-white flex items-center justify-center">
                      <FaUser size={48} className="text-[#FF5A5A]" />
                    </div>
                  </div>

                  {/* User Name */}
                  <h2 className="text-lg font-bold mb-1.5 text-center text-white px-2">
                    {formData.fullName || "Nguyen Van Admin"}
                  </h2>

                  {/* Role */}
                  <p className="text-xs text-white opacity-90">{userRole}</p>
                </motion.div>

                {/* Right Panel - White (70-75% width) */}
                <motion.div
                  initial={{ x: 50, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.2, duration: 0.3 }}
                  className="flex-1 overflow-y-auto bg-white min-w-0 flex flex-col"
                  style={{ borderRadius: "0 8px 8px 0" }}
                >
                  {/* Title - Only on white panel */}
                  <div className="px-10 pt-6 pb-4 border-b border-gray-200 flex items-center justify-between bg-white sticky top-0 z-[50]">
                    <div className="flex items-center gap-2">
                      <FaInfoCircle className="text-[#FF5A5A]" size={16} />
                      <h2 className="text-xl font-bold text-[#FF5A5A] uppercase">UPDATE USER</h2>
                    </div>
                    {/* Close Button */}
                    <button
                      onClick={onClose}
                      className="p-1 text-[#FF5A5A] hover:bg-red-50 rounded transition-colors duration-200"
                      aria-label="Close"
                    >
                      <X size={20} />
                    </button>
                  </div>

                  {/* Form Content */}
                  <div className="p-10">
                    <div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-6">
                        {/* Left Column */}
                        <div className="space-y-6">
                          {/* Full Name */}
                          <div>
                            <div className="flex items-center gap-2 mb-2">
                              <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                              <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                                Full Name <span className="text-[#FF5A5A]">*</span>
                              </label>
                            </div>
                            <input
                              type="text"
                              name="fullName"
                              value={formData.fullName}
                              onChange={handleChange}
                              className={`w-full px-3 py-2 border rounded-md text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#FF5A5A] focus:border-transparent ml-3 ${
                                errors.fullName
                                  ? "border-red-500"
                                  : "border-gray-300"
                              }`}
                              placeholder="Enter full name"
                            />
                            {errors.fullName && (
                              <p className="text-red-500 text-xs mt-1 ml-3">
                                {errors.fullName}
                              </p>
                            )}
                          </div>

                          {/* Date of Birth */}
                          <div>
                            <div className="flex items-center gap-2 mb-2">
                              <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                              <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                                Date of Birth <span className="text-[#FF5A5A]">*</span>
                              </label>
                            </div>
                            <div className="relative ml-3">
                              <input
                                type="text"
                                name="dateOfBirth"
                                value={formData.dateOfBirth}
                                onChange={handleChange}
                                className={`w-full px-3 py-2 pr-10 border rounded-md text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#FF5A5A] focus:border-transparent ${
                                  errors.dateOfBirth
                                    ? "border-red-500"
                                    : "border-gray-300"
                                }`}
                                placeholder="dd/mm/yyyy"
                              />
                              <Calendar
                                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none"
                                size={16}
                              />
                            </div>
                            {errors.dateOfBirth && (
                              <p className="text-red-500 text-xs mt-1 ml-3">
                                {errors.dateOfBirth}
                              </p>
                            )}
                          </div>
                        </div>

                        {/* Right Column */}
                        <div className="space-y-6">
                          {/* Phone Number */}
                          <div>
                            <div className="flex items-center gap-2 mb-2">
                              <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                              <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                                Phone Number <span className="text-[#FF5A5A]">*</span>
                              </label>
                            </div>
                            <input
                              type="text"
                              name="phoneNumber"
                              value={formData.phoneNumber}
                              onChange={handleChange}
                              className={`w-full px-3 py-2 border rounded-md text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#FF5A5A] focus:border-transparent ml-3 ${
                                errors.phoneNumber
                                  ? "border-red-500"
                                  : "border-gray-300"
                              }`}
                              placeholder="Enter phone number"
                            />
                            {errors.phoneNumber && (
                              <p className="text-red-500 text-xs mt-1 ml-3">
                                {errors.phoneNumber}
                              </p>
                            )}
                          </div>

                          {/* Address */}
                          <div>
                            <div className="flex items-center gap-2 mb-2">
                              <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                              <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                                Address <span className="text-[#FF5A5A]">*</span>
                              </label>
                            </div>
                            <input
                              type="text"
                              name="address"
                              value={formData.address}
                              onChange={handleChange}
                              className={`w-full px-3 py-2 border rounded-md text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#FF5A5A] focus:border-transparent ml-3 ${
                                errors.address
                                  ? "border-red-500"
                                  : "border-gray-300"
                              }`}
                              placeholder="Enter address"
                            />
                            {errors.address && (
                              <p className="text-red-500 text-xs mt-1 ml-3">
                                {errors.address}
                              </p>
                            )}
                          </div>

                          {/* Gender */}
                          <div>
                            <div className="flex items-center gap-2 mb-2">
                              <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                              <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                                Gender <span className="text-[#FF5A5A]">*</span>
                              </label>
                            </div>
                            <div className="flex gap-6 ml-3">
                              <label className="flex items-center cursor-pointer">
                                <input
                                  type="radio"
                                  name="gender"
                                  value="Male"
                                  checked={formData.gender === "Male"}
                                  onChange={() => handleGenderChange("Male")}
                                  className="w-4 h-4 accent-[#FF5A5A] focus:ring-[#FF5A5A] focus:ring-2 border-gray-300"
                                  style={{ accentColor: "#FF5A5A" }}
                                />
                                <span className="ml-2 text-sm text-gray-800">Male</span>
                              </label>
                              <label className="flex items-center cursor-pointer">
                                <input
                                  type="radio"
                                  name="gender"
                                  value="Female"
                                  checked={formData.gender === "Female"}
                                  onChange={() => handleGenderChange("Female")}
                                  className="w-4 h-4 accent-[#FF5A5A] focus:ring-[#FF5A5A] focus:ring-2 border-gray-300"
                                  style={{ accentColor: "#FF5A5A" }}
                                />
                                <span className="ml-2 text-sm text-gray-800">Female</span>
                              </label>
                            </div>
                            {errors.gender && (
                              <p className="text-red-500 text-xs mt-1 ml-3">
                                {errors.gender}
                              </p>
                            )}
                          </div>
                        </div>
                      </div>

                      {/* Buttons */}
                      <div className="mt-10 flex justify-end gap-3">
                        <button
                          type="button"
                          onClick={handleCancel}
                          className="px-6 py-2.5 bg-white border border-gray-300 text-gray-600 rounded-md font-semibold text-sm hover:bg-gray-50 hover:border-gray-400 transition-colors duration-200"
                        >
                          Cancel
                        </button>
                        <motion.button
                          type="button"
                          onClick={handleSubmit}
                          disabled={isSubmitting}
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          className="px-8 py-2.5 bg-[#FF5A5A] text-white rounded-md font-semibold text-sm hover:bg-[#FF3A3A] transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {isSubmitting ? "Updating..." : "Update"}
                        </motion.button>
                      </div>
                    </div>
                  </div>
                </motion.div>
              </div>
            </motion.div>
          </div>
        </>
      )}
    </AnimatePresence>,
    document.body
  );
}

// Demo component to test the modal
function App() {
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <button
        onClick={() => setIsModalOpen(true)}
        className="px-6 py-3 bg-[#FF5A5A] text-white rounded-lg font-semibold hover:bg-[#FF3A3A] transition-colors"
      >
        Open Update User Modal
      </button>

      <UpdateUserModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        initialData={{
          fullName: "Nguyen Van Admin",
          phoneNumber: "0901234567",
          gender: "Male",
          dateOfBirth: "12/05/1995",
          address: "Hanoi, Vietnam",
          role: "ADMIN",
        }}
      />
    </div>
  );
}