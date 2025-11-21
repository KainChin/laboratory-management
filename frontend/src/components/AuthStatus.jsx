import React from 'react';
import { useAuthToken } from '../hooks/useAuthToken';
import { isTokenExpired } from '../utils/jwtUtils';
import { CheckCircle2, XCircle, Clock, RefreshCw } from 'lucide-react';

/**
 * Component hiển thị trạng thái authentication
 * Có thể sử dụng để debug hoặc hiển thị cho admin
 */
export default function AuthStatus() {
  const { loading, error, userInfo, isAuthenticated, getToken, clearToken, refreshToken } = useAuthToken();
  const [refreshing, setRefreshing] = React.useState(false);
  
  const authenticated = isAuthenticated();
  const token = getToken();
  const expired = token ? isTokenExpired(token) : true;

  // Hàm handle refresh
  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await refreshToken();
    } finally {
      setRefreshing(false);
    }
  };

  if (loading) {
    return (
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 flex items-center gap-3">
        <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-500"></div>
        <div>
          <p className="text-sm font-medium text-blue-800">Đang kiểm tra...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <div className="flex items-center gap-3">
          <XCircle className="text-red-500" size={20} />
          <div>
            <p className="text-sm font-medium text-red-800">Lỗi xác thực</p>
            <p className="text-xs text-red-600">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!authenticated) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center gap-3">
        <XCircle className="text-red-500" size={20} />
        <div>
          <p className="text-sm font-medium text-red-800">Not Authenticated</p>
          <p className="text-xs text-red-600">No access token found</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`border rounded-lg p-4 ${
      expired 
        ? 'bg-yellow-50 border-yellow-200' 
        : 'bg-green-50 border-green-200'
    }`}>
      <div className="flex items-start justify-between">
        <div className="flex items-start gap-3">
          {expired ? (
            <Clock className="text-yellow-600 mt-0.5" size={20} />
          ) : (
            <CheckCircle2 className="text-green-600 mt-0.5" size={20} />
          )}
          <div>
            <p className={`text-sm font-medium ${
              expired ? 'text-yellow-800' : 'text-green-800'
            }`}>
              {expired ? 'Token Expired' : 'Authenticated'}
            </p>
            {userInfo && (
              <div className="mt-2 space-y-1">
                <p className="text-xs text-gray-600">
                  <span className="font-medium">User:</span> {userInfo.userName}
                </p>
                <p className="text-xs text-gray-600">
                  <span className="font-medium">Email:</span> {userInfo.email}
                </p>
                <p className="text-xs text-gray-600">
                  <span className="font-medium">Role:</span> {userInfo.role}
                </p>
                {userInfo.exp && (
                  <p className="text-xs text-gray-600">
                    <span className="font-medium">Expires:</span>{' '}
                    {new Date(userInfo.exp * 1000).toLocaleString()}
                  </p>
                )}
              </div>
            )}
          </div>
        </div>
        <div className="flex gap-2">
          {expired && (
            <button
              onClick={handleRefresh}
              disabled={refreshing}
              className="text-xs px-3 py-1 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded transition-colors flex items-center gap-1 disabled:opacity-50"
            >
              <RefreshCw size={12} className={refreshing ? 'animate-spin' : ''} />
              Refresh
            </button>
          )}
          <button
            onClick={clearToken}
            className="text-xs px-3 py-1 bg-red-100 hover:bg-red-200 text-red-700 rounded transition-colors"
          >
            Clear Token
          </button>
        </div>
      </div>
    </div>
  );
}
