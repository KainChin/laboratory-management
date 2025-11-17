import React, { useEffect } from 'react';
import { createPortal } from 'react-dom';

function DeleteConfirmationModal({ open, onConfirm, onCancel }) {
  // khóa scroll khi modal mở
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  if (!open) return null;

  return createPortal(
    <>
      {/* overlay phủ toàn màn hình */}
      <div className="fixed inset-0 bg-black bg-opacity-60 z-[9999] pointer-events-auto" />

      {/* container modal chính */}
      <div className="fixed inset-0 flex items-center justify-center z-[10000] p-4">
        <div className="bg-white p-6 rounded-lg shadow-2xl max-w-md w-full">
          <h2 className="text-lg font-semibold mb-4">Confirm to eliminate</h2>
          <p>You want to remove test order?</p>
          <div className="mt-6 flex justify-end gap-2">
            <button
              onClick={onCancel}
              className="px-4 min-h-[40px] py-2 bg-gray-200 rounded hover:bg-gray-300"
            >
              Cancel
            </button>
            <button
              onClick={onConfirm}
              className="px-4 min-h-[40px] py-2 bg-[#FF5A5A] text-white rounded hover:bg-[#FF3A3A] transition-colors duration-300"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </>,
    document.body
  );
}

export default DeleteConfirmationModal;
