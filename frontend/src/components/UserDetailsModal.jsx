import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { motion, AnimatePresence } from "framer-motion";
import { X, Check, Edit2 } from "lucide-react";
import { FaUser } from "react-icons/fa";
import { getUserFromToken } from "../utils/jwtUtils";
import UpdateUserModal from "./UpdateUserModal";

export default function UserDetailsModal({ open, onClose }) {
  const [userData, setUserData] = useState(null);
  const [isEditingPassword, setIsEditingPassword] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);

  // Lock scroll when modal is open
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  // Load user data from token or mock data
  useEffect(() => {
    if (!open) return;
    
    // Try to get user info from token
    const tokenUser = getUserFromToken();
    
    // Mock user data (replace with API call later)
    const mockUserData = {
      userName: tokenUser?.userName || "Nguyen Van Admin",
      role: "ADMIN",
      identityNumber: "012345678901",
      phoneNumber: "0901234567",
      gender: "Male",
      email: "admin@example.com",
      dateOfBirth: "12/05/1995",
      age: 30,
      address: "Hanoi, Vietnam",
      password: "**********",
    };

    // Calculate age from date of birth if needed
    if (mockUserData.dateOfBirth && !mockUserData.age) {
      const dobParts = mockUserData.dateOfBirth.split("/");
      if (dobParts.length === 3) {
        const dob = new Date(`${dobParts[2]}-${dobParts[1]}-${dobParts[0]}`);
        const today = new Date();
        let age = today.getFullYear() - dob.getFullYear();
        const monthDiff = today.getMonth() - dob.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
          age--;
        }
        mockUserData.age = age;
      }
    }

    setUserData(mockUserData);
  }, [open]);

  const handleUpdate = () => {
    setShowUpdateModal(true);
  };

  const handleUpdateModalClose = () => {
    setShowUpdateModal(false);
    // Optionally refresh user data after update
    // You can add a callback here to refresh data
  };

  if (!open || !userData) {
    return (
      <UpdateUserModal
        open={showUpdateModal}
        onClose={handleUpdateModalClose}
        initialData={userData}
      />
    );
  }

  return (
    <>
      {createPortal(
        <AnimatePresence>
          {open && !showUpdateModal && (
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
                borderRadius: "8px"
              }}
              onClick={(e) => e.stopPropagation()}
            >
              {/* Close Button */}
              <button
                onClick={onClose}
                className="absolute top-4 right-4 z-10 p-1 text-[#FF5A5A] hover:bg-red-50 rounded transition-colors duration-200"
                aria-label="Close"
              >
                <X size={20} />
              </button>

              <div className="flex flex-col md:flex-row">
                {/* Left Panel - Red (25-30% width - nhỏ hơn phần trắng) */}
                <motion.div
                  initial={{ x: -50, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.1, duration: 0.3 }}
                  className="bg-[#FF5A5A] text-white p-6 md:w-[200px] flex flex-col items-center justify-center relative flex-shrink-0"
                  style={{ borderRadius: "8px 0 0 8px" }}
                >
                  {/* Green Checkmark - overlap border */}
                  <div className="absolute top-4 right-0 transform translate-x-1/2 z-10">
                    <div className="w-6 h-6 bg-green-500 rounded-full flex items-center justify-center shadow-md">
                      <Check
                        className="text-white"
                        size={14}
                      />
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
                    {userData.userName}
                  </h2>

                  {/* Role */}
                  <p className="text-xs text-white opacity-90">{userData.role}</p>
                </motion.div>

                {/* Right Panel - White (70-75% width - lớn hơn phần đỏ) */}
                <motion.div
                  initial={{ x: 50, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.2, duration: 0.3 }}
                  className="flex-1 p-10 overflow-y-auto bg-white min-w-0"
                  style={{ borderRadius: "0 8px 8px 0" }}
                >
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-8">
                    {/* Left Column */}
                    <div className="space-y-8">
                      {/* Identity Number */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            IDENTITY NUMBER
                          </label>
                          <div className="w-4 h-4 rounded-full bg-gray-400 flex items-center justify-center cursor-pointer hover:bg-gray-500 transition-colors ml-1">
                            <span className="text-[10px] text-white font-bold">i</span>
                          </div>
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.identityNumber}</p>
                      </div>

                      {/* Phone Number */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            PHONE NUMBER
                          </label>
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.phoneNumber}</p>
                      </div>

                      {/* Gender */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            GENDER
                          </label>
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.gender}</p>
                      </div>

                      {/* Email */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            EMAIL
                          </label>
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.email}</p>
                      </div>
                    </div>

                    {/* Right Column */}
                    <div className="space-y-8">
                      {/* Date of Birth */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            DATE OF BIRTH
                          </label>
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.dateOfBirth}</p>
                      </div>

                      {/* Age */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            AGE
                          </label>
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.age} years old</p>
                      </div>

                      {/* Address */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            ADDRESS
                          </label>
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.address}</p>
                      </div>

                      {/* Password */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className="w-1 h-4 bg-[#FF5A5A]"></div>
                          <label className="text-xs font-bold text-gray-700 uppercase tracking-wide">
                            PASSWORD
                          </label>
                          <Edit2
                            size={14}
                            className="text-gray-500 cursor-pointer hover:text-[#FF5A5A] transition-colors ml-1"
                            onClick={() => setIsEditingPassword(!isEditingPassword)}
                          />
                        </div>
                        <p className="text-sm text-gray-800 ml-3">{userData.password}</p>
                      </div>
                    </div>
                  </div>

                  {/* Update Button */}
                  <div className="mt-10 flex justify-end">
                    <motion.button
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={handleUpdate}
                      className="px-8 py-2.5 bg-[#FF5A5A] text-white rounded-md font-semibold text-sm hover:bg-[#FF3A3A] transition-colors duration-200"
                    >
                      Update
                    </motion.button>
                  </div>
                </motion.div>
              </div>
            </motion.div>
          </div>
        </>
      )}
        </AnimatePresence>,
        document.body
      )}

      {/* Update User Modal */}
      <UpdateUserModal
        open={showUpdateModal}
        onClose={handleUpdateModalClose}
        initialData={userData}
      />
    </>
  );
}

