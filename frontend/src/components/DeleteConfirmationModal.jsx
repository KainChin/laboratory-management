import React from 'react';

function DeleteConfirmationModal({ open, onConfirm, onCancel }) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-50">
      <div className="bg-white p-6 rounded-md shadow-md min-w-[300px]">
        <h2 className="text-lg font-semibold mb-4">Xác nhận xoá</h2>
        <p>Bạn có chắc chắn muốn xoá mục này?</p>
        <div className="mt-6 flex justify-end gap-2">
          <button
            onClick={onCancel}
            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
          >
            Huỷ
          </button>
          <button
            onClick={onConfirm}
            className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
          >
            Xoá
          </button>
        </div>
      </div>
    </div>
  );
}

export default DeleteConfirmationModal;
