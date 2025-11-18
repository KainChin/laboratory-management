/**
 * Decode JWT token without verification
 * @param {string} token - JWT token to decode
 * @returns {object|null} - Decoded token payload or null if invalid
 */
export const decodeJWT = (token) => {
  try {
    if (!token) return null;
    
    // JWT token có 3 phần: header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    
    // Decode phần payload (phần thứ 2)
    const payload = parts[1];
    
    // Base64 decode
    const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    
    // Parse JSON
    return JSON.parse(decodedPayload);
  } catch (error) {
    console.error('Error decoding JWT:', error);
    return null;
  }
};

/**
 * Get user info from access token in localStorage
 * @returns {object|null} - User info from token or null
 */
export const getUserFromToken = () => {
  try {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) return null;
    
    return decodeJWT(accessToken);
  } catch (error) {
    console.error('Error getting user from token:', error);
    return null;
  }
};

/**
 * Get username from token
 * @returns {string} - Username or 'User' as fallback
 */
export const getUserName = () => {
  const userInfo = getUserFromToken();
  return userInfo?.userName || 'User';
};

/**
 * Check if token is expired
 * @returns {boolean} - True if expired or invalid
 */
export const isTokenExpired = () => {
  const userInfo = getUserFromToken();
  if (!userInfo || !userInfo.exp) return true;
  
  const currentTime = Math.floor(Date.now() / 1000);
  return userInfo.exp < currentTime;
};
