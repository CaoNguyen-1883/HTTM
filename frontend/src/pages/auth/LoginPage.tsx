import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useAuthStore } from "../../lib/stores";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Label } from "../../components/ui/Label";
import { loginSchema, LoginFormData } from "../../lib/schemas";
import { useState } from "react";
import { handleApiError } from "../../lib/api";

interface LoginPageProps {
  expectedRole?: string; // "ROLE_ADMIN", "ROLE_SELLER", etc.
  portalName?: string; // "Admin", "Seller", etc.
}

export const LoginPage = ({ expectedRole, portalName }: LoginPageProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuthStore();
  const [apiError, setApiError] = useState<string>("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const getRoleDisplayName = () => {
    if (portalName) return portalName;
    switch (expectedRole) {
      case "ROLE_ADMIN": return "Quản trị viên";
      case "ROLE_SELLER": return "Người bán";
      case "ROLE_STAFF": return "Nhân viên";
      default: return "";
    }
  };

  const getRedirectPath = (userRoles: string[]) => {
    // If portal-specific login, redirect to that portal
    if (expectedRole) {
      switch (expectedRole) {
        case "ROLE_ADMIN": return "/admin";
        case "ROLE_SELLER": return "/seller";
        case "ROLE_STAFF": return "/staff";
      }
    }

    // General login - redirect based on user's highest role
    if (userRoles.includes("ROLE_ADMIN")) return "/admin";
    if (userRoles.includes("ROLE_SELLER")) return "/seller";
    if (userRoles.includes("ROLE_STAFF")) return "/staff";
    return "/customer";
  };

  const onSubmit = async (data: LoginFormData) => {
    try {
      setApiError("");
      
      // Add expectedRole to request if provided
      const loginData = expectedRole 
        ? { ...data, expectedRole }
        : data;
      
      await login(loginData);
      
      // Get user after successful login
      const user = useAuthStore.getState().user;
      if (!user) {
        setApiError("Không thể lấy thông tin user");
        return;
      }

      // Redirect to appropriate portal
      const from = (location.state as any)?.from?.pathname;
      const redirectPath = from || getRedirectPath(user.roles);
      navigate(redirectPath, { replace: true });

    } catch (error) {
      setApiError(handleApiError(error));
    }
  };

  const roleDisplayName = getRoleDisplayName();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Đăng nhập {roleDisplayName}
          </h2>
          
          {!expectedRole && (
            <p className="mt-2 text-center text-sm text-gray-600">
              Hoặc{" "}
              <Link
                to="/register"
                className="font-medium text-blue-600 hover:text-blue-500"
              >
                tạo tài khoản mới
              </Link>
            </p>
          )}

          {/* Portal-specific notice */}
          {expectedRole && (
            <div className="mt-4 rounded-md bg-blue-50 p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3 flex-1">
                  <p className="text-sm text-blue-700">
                    Chỉ tài khoản <strong>{roleDisplayName}</strong> mới có thể đăng nhập tại đây.
                  </p>
                  <p className="mt-1 text-sm text-blue-600">
                    Khách hàng vui lòng{" "}
                    <Link to="/login" className="font-medium underline">
                      đăng nhập tại đây
                    </Link>
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Form */}
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="rounded-md shadow-sm space-y-4">
            {/* API Error */}
            {apiError && (
              <div className="rounded-md bg-red-50 p-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <p className="text-sm text-red-800">{apiError}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Email */}
            <div>
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="Nhập email"
                error={errors.email?.message}
                {...register("email")}
              />
            </div>

            {/* Password */}
            <div>
              <Label htmlFor="password">Mật khẩu</Label>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                placeholder="Nhập mật khẩu"
                error={errors.password?.message}
                {...register("password")}
              />
            </div>
          </div>

          {/* Remember & Forgot */}
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input
                id="remember-me"
                name="remember-me"
                type="checkbox"
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-900">
                Ghi nhớ đăng nhập
              </label>
            </div>

            <div className="text-sm">
              <a href="#" className="font-medium text-blue-600 hover:text-blue-500">
                Quên mật khẩu?
              </a>
            </div>
          </div>

          {/* Submit Button */}
          <div>
            <Button
              type="submit"
              className="w-full"
              isLoading={isSubmitting}
            >
              Đăng nhập
            </Button>
          </div>
        </form>

        {/* Portal Links */}
        {!expectedRole && (
          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-gray-50 text-gray-500">
                  Hoặc đăng nhập với
                </span>
              </div>
            </div>

            <div className="mt-4 grid grid-cols-3 gap-3">
              <Link
                to="/admin/login"
                className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Admin
              </Link>
              <Link
                to="/seller/login"
                className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Seller
              </Link>
              <Link
                to="/staff/login"
                className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Staff
              </Link>
            </div>
          </div>
        )}

        {/* Dev Tools */}
        {import.meta.env.DEV && (
          <div className="mt-6 border-t border-gray-200 pt-6">
            <p className="text-xs text-gray-500 text-center">
              Development Mode - Use existing accounts from backend
            </p>
          </div>
        )}
      </div>
    </div>
  );
};