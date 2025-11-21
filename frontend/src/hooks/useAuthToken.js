import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { isTokenExpired, parseClaims } from '../utils/jwtUtils';
import publicApi from '../api/publicAxios';

/**
 * Custom hook để xử lý authentication với silent login
 * Tự động:
 * 1. Lấy token từ URL hash
 * 2. Kiểm tra token expired
 * 3. Tự động refresh token nếu expired
 * 4. Parse user info từ token
 */
export const useAuthToken = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(true);
  const [userInfo, setUserInfo] = useState(null);
  const [error, setError] = useState(null);

  // Hàm xử lý token từ URL (hỗ trợ cả query params và hash)
  const handleTokenFromUrl = () => {
    try {
      // Kiểm tra query parameter: ?code=...
      const urlParams = new URLSearchParams(window.location.search);
      let code = urlParams.get('code');
      
      // Nếu không có trong query, kiểm tra hash: #?code=...
      if (!code) {
        const hash = window.location.hash;
        if (hash && hash.includes('?')) {
          const hashParams = new URLSearchParams(hash.substring(hash.indexOf('?')));
          code = hashParams.get('code');
        }
      }
      
      if (code) {
        localStorage.setItem('token', code);
        console.log('✅ Token đã được lưu từ URL');
        
        // Parse user info từ token
        const payload = parseClaims(code);
        if (payload) {
          setUserInfo(payload);
          console.log('✅ User info:', payload.userName || payload.sub);
        }
        
        // Xóa code khỏi URL mà không reload trang
        const cleanUrl = window.location.pathname;
        window.history.replaceState(null, '', cleanUrl);
        
        return true;
      }
    } catch (error) {
      console.error('❌ Lỗi khi xử lý token từ URL:', error);
      setError(error.message);
    }
    return false;
  };

  // Hàm refresh token
  const refreshToken = async () => {
    try {
      console.log('🔄 Đang refresh token...');
      const response = await publicApi.post('/api/auth/refresh');
      
      if (response.data?.data?.accessToken) {
        const newToken = response.data.data.accessToken;
        localStorage.setItem('token', newToken);
        
        // Parse user info từ token mới
        const payload = parseClaims(newToken);
        if (payload) {
          setUserInfo(payload);
        }
        
        console.log('✅ Token đã được refresh thành công');
        return true;
      }
      return false;
    } catch (error) {
      console.error('❌ Lỗi khi refresh token:', error);
      // Nếu refresh thất bại, xóa token cũ
      localStorage.removeItem('token');
      setUserInfo(null);
      return false;
    }
  };

  // Silent login effect
  useEffect(() => {
    const handleSilentLogin = async () => {
      try {
        setLoading(true);
        setError(null);

        // Bước 1: Kiểm tra token từ URL
        const hasTokenFromUrl = handleTokenFromUrl();
        if (hasTokenFromUrl) {
          setLoading(false);
          return;
        }

        // Bước 2: Kiểm tra token trong localStorage
        const token = localStorage.getItem('token');
        if (!token) {
          console.log('ℹ️ Không tìm thấy token');
          setLoading(false);
          return;
        }

        // Bước 3: Kiểm tra token expired
        if (isTokenExpired(token)) {
          console.log('⏰ Token đã hết hạn, đang refresh...');
          const refreshed = await refreshToken();
          if (!refreshed) {
            setError('Không thể refresh token');
          }
        } else {
          // Token còn hạn, parse user info
          const payload = parseClaims(token);
          if (payload) {
            setUserInfo(payload);
            console.log('✅ Token hợp lệ, user:', payload.userName || payload.sub);
          }
        }
      } catch (error) {
        console.error('❌ Lỗi silent login:', error);
        setError(error.message);
      } finally {
        setLoading(false);
      }
    };

    handleSilentLogin();
  }, [location]);

  // Listen to token expired event
  useEffect(() => {
    const handleTokenExpired = () => {
      setUserInfo(null);
      setError('Token đã hết hạn');
    };

    window.addEventListener('token-expired', handleTokenExpired);
    return () => window.removeEventListener('token-expired', handleTokenExpired);
  }, []);

  /**
   * Kiểm tra xem user đã được authenticated chưa
   */
  const isAuthenticated = () => {
    const token = localStorage.getItem('token');
    return !!token && !isTokenExpired(token);
  };

  /**
   * Lấy token hiện tại
   */
  const getToken = () => {
    return localStorage.getItem('token');
  };

  /**
   * Xóa token (logout)
   */
  const clearToken = () => {
    localStorage.removeItem('token');
    setUserInfo(null);
    console.log('🔓 Token đã được xóa');
  };

  /**
   * Manual refresh token
   */
  const manualRefresh = async () => {
    return await refreshToken();
  };

  return {
    loading,
    error,
    userInfo,
    isAuthenticated,
    getToken,
    clearToken,
    refreshToken: manualRefresh,
  };
};
