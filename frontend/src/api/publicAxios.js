import axios from 'axios';

/**
 * Public axios instance for authentication endpoints
 * Không tự động thêm Authorization header
 * Dùng cho login, register, refresh token
 */
const publicApi = axios.create({
  baseURL: 'http://35.172.58.177:8089',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Quan trọng: để gửi/nhận HTTP-only cookies (refreshToken)
});

export default publicApi;
