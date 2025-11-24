import axios from 'axios';

/**
 * Public axios instance for authentication endpoints
 * Không tự động thêm Authorization header
 * Dùng cho login, register, refresh token
 */
const publicApi = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://54.225.178.199:8080/iam',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Quan trọng: để gửi/nhận HTTP-only cookies (refreshToken)
});

export default publicApi;
