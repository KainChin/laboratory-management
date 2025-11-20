import React from 'react';
import { useAuthToken } from '../hooks/useAuthToken';
import { getUserFromToken, isTokenExpired } from '../utils/jwtUtils';
import { CheckCircle2, XCircle, Clock } from 'lucide-react';

/**
 * Component hiển thị trạng thái authentication
 * Có thể sử dụng để debug hoặc hiển thị cho admin
 */
export default function AuthStatus() {
  const { isAuthenticated, getToken, clearToken } = useAuthToken();
  const authenticated = isAuthenticated();
  const token = getToken();
  const userInfo = getUserFromToken();
  const expired = isTokenExpired();

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
        <button
          onClick={clearToken}
          className="text-xs px-3 py-1 bg-red-100 hover:bg-red-200 text-red-700 rounded transition-colors"
        >
          Clear Token
        </button>
      </div>
    </div>
  );
}
