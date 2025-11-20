import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

/**
 * Custom hook để xử lý authentication token từ URL
 * Tự động lấy token từ hash parameter và lưu vào localStorage
 * Sau đó xóa token khỏi URL để bảo mật
 */
export const useAuthToken = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Kiểm tra xem URL có chứa token trong hash không
    const hash = window.location.hash;
    
    if (hash) {
      try {
        // Parse hash để lấy code/token
        // Format: #?code=eyJhbGciOiJIUzI1NiJ9...
        const hashParams = new URLSearchParams(hash.substring(hash.indexOf('?')));
        const code = hashParams.get('code');
        
        if (code) {
          // Lưu token vào localStorage
          localStorage.setItem('accessToken', code);
          
          console.log('✅ Access token đã được lưu vào localStorage');
          
          // Xóa hash khỏi URL mà không reload trang
          // Giữ nguyên path hiện tại nhưng xóa hash
          const cleanPath = window.location.pathname;
          window.history.replaceState(null, '', cleanPath);
          
          // Nếu đang ở trang chính và vừa nhận token, có thể redirect đến test-orders
          // Tùy chọn: bạn có thể uncomment dòng dưới nếu muốn tự động chuyển hướng
          // if (location.pathname === '/' || location.pathname === '/home') {
          //   navigate('/test-orders', { replace: true });
          // }
        }
      } catch (error) {
        console.error('❌ Lỗi khi xử lý token từ URL:', error);
      }
    }
  }, [location, navigate]);

  /**
   * Kiểm tra xem user đã có token chưa
   */
  const isAuthenticated = () => {
    const token = localStorage.getItem('accessToken');
    return !!token;
  };

  /**
   * Lấy token hiện tại
   */
  const getToken = () => {
    return localStorage.getItem('accessToken');
  };

  /**
   * Xóa token (logout)
   */
  const clearToken = () => {
    localStorage.removeItem('accessToken');
    console.log('🔓 Token đã được xóa khỏi localStorage');
  };

  return {
    isAuthenticated,
    getToken,
    clearToken,
  };
};
