import axios from 'axios';

/**
 * Public axios instance for authentication endpoints
 * Không tự động thêm Authorization header
 * Dùng cho login, register, refresh token
 */
const publicApi = axios.create({
  baseURL: import.meta.env.VITE_API_GATEWAY_URL || 'https://gateway.smokingcessationsupport.space/iam/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, 
});

export default publicApi;
