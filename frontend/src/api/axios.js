import axios from 'axios';

const instance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  }
});
// Interceptor để tự động thêm Bearer token vào mọi request
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor để xử lý response errors (optional - xử lý 401 Unauthorized)
instance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token hết hạn hoặc không hợp lệ
      localStorage.removeItem('accessToken');
      // Có thể redirect về trang login nếu cần
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default instance;