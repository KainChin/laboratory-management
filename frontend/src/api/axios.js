import axios from 'axios';

const instance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Để gửi cookie cho refresh token
});

// Interceptor để tự động thêm Bearer token vào mọi request
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor để xử lý response errors
instance.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token hết hạn hoặc không hợp lệ
      console.log('❌ Token không hợp lệ hoặc hết hạn');
      localStorage.removeItem('token');
      
      // Có thể redirect về trang chủ hoặc hiển thị thông báo
      window.dispatchEvent(new Event('token-expired'));
    }
    return Promise.reject(error);
  }
);

export default instance;