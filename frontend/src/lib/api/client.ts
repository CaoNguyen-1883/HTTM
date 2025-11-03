import axios, { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from "axios";
import { AuthResponse, RefreshTokenRequest, ApiResponse } from "../types";

// Base URL từ Vite proxy
const API_BASE_URL = "/api";

// Create Axios instance
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Token management
const TOKEN_KEY = "access_token";
const REFRESH_TOKEN_KEY = "refresh_token";

export const tokenManager = {
  getAccessToken: (): string | null => {
    return localStorage.getItem(TOKEN_KEY);
  },

  getRefreshToken: (): string | null => {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },

  setTokens: (accessToken: string, refreshToken: string): void => {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },

  clearTokens: (): void => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  },
};

// Flag to prevent multiple refresh requests
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// Request interceptor - Add JWT token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenManager.getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Unwrap ApiResponse and handle 401
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // Unwrap ApiResponse wrapper if present
    if (response.data && typeof response.data === 'object' && 'success' in response.data) {
      const apiResponse = response.data as ApiResponse;
      
      // If error in ApiResponse
      if (!apiResponse.success) {
        return Promise.reject({
          response: {
            data: apiResponse,
            status: apiResponse.statusCode || response.status,
          },
        });
      }
      
      // Return unwrapped data
      response.data = apiResponse.data;
    }
    
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // If error is 401 and we haven't retried yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = tokenManager.getRefreshToken();

      if (!refreshToken) {
        // No refresh token, logout
        tokenManager.clearTokens();
        window.location.href = "/login";
        return Promise.reject(error);
      }

      try {
        // Call refresh token API - FIXED ENDPOINT
        const response = await axios.post<ApiResponse<AuthResponse>>(
          `${API_BASE_URL}/auth/refresh-token`, // Changed from /auth/refresh
          {
            refreshToken,
          } as RefreshTokenRequest
        );

        // Unwrap ApiResponse
        const authResponse = response.data.data!;
        const { accessToken, refreshToken: newRefreshToken } = authResponse;

        // Save new tokens
        tokenManager.setTokens(accessToken, newRefreshToken);

        // Process queued requests
        processQueue(null, accessToken);

        // Retry original request
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }

        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed, logout
        processQueue(refreshError as Error, null);
        tokenManager.clearTokens();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Helper function to handle API errors
export const handleApiError = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiResponse>;
    
    // Check for ApiResponse error structure
    if (axiosError.response?.data?.error) {
      const errorData = axiosError.response.data.error;
      
      // If error is a string
      if (typeof errorData === 'string') {
        return errorData;
      }
      
      // If error is an object with message
      if (typeof errorData === 'object' && 'message' in errorData) {
        return (errorData as any).message;
      }
      
      return JSON.stringify(errorData);
    }
    
    if (axiosError.response?.data?.message) {
      return axiosError.response.data.message;
    }

    if (axiosError.response?.status === 401) {
      return "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.";
    }

    if (axiosError.response?.status === 403) {
      return "Bạn không có quyền thực hiện thao tác này.";
    }

    if (axiosError.response?.status === 404) {
      return "Không tìm thấy tài nguyên.";
    }

    if (axiosError.response?.status === 500) {
      return "Lỗi máy chủ. Vui lòng thử lại sau.";
    }

    if (axiosError.message) {
      return axiosError.message;
    }
  }

  return "Đã xảy ra lỗi không xác định.";
};
