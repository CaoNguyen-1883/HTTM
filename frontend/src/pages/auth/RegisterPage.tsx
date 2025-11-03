import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate, Link } from "react-router-dom";
import { useAuthStore } from "../../lib/stores";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Label } from "../../components/ui/Label";
import { registerSchema, RegisterFormData } from "../../lib/schemas";
import { useState } from "react";
import { handleApiError } from "../../lib/api";

export const RegisterPage = () => {
  const navigate = useNavigate();
  const { register: registerUser } = useAuthStore();
  const [apiError, setApiError] = useState<string>("");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const password = watch("password");

  // Password strength indicator
  const getPasswordStrength = (pwd: string) => {
    if (!pwd) return { strength: 0, label: "", color: "" };
    let strength = 0;
    if (pwd.length >= 8) strength++;
    if (/[a-z]/.test(pwd)) strength++;
    if (/[A-Z]/.test(pwd)) strength++;
    if (/[0-9]/.test(pwd)) strength++;
    if (/[@$!%*?&]/.test(pwd)) strength++;

    const labels = ["Rất yếu", "Yếu", "Trung bình", "Mạnh", "Rất mạnh"];
    const colors = ["bg-red-500", "bg-orange-500", "bg-yellow-500", "bg-blue-500", "bg-green-500"];

    return {
      strength,
      label: labels[strength - 1] || "",
      color: colors[strength - 1] || "",
    };
  };

  const passwordStrength = getPasswordStrength(password);

  const onSubmit = async (data: RegisterFormData) => {
    try {
      setApiError("");
      await registerUser(data);

      // Backend auto-assigns ROLE_CUSTOMER
      navigate("/customer", { replace: true });
    } catch (error) {
      setApiError(handleApiError(error));
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Tạo tài khoản
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Hoặc{" "}
            <Link
              to="/login"
              className="font-medium text-blue-600 hover:text-blue-500"
            >
              đăng nhập với tài khoản có sẵn
            </Link>
          </p>
        </div>

        {/* Form */}
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            {/* API Error */}
            {apiError && (
              <div className="rounded-md bg-red-50 p-4">
                <div className="text-sm text-red-800">{apiError}</div>
              </div>
            )}

            {/* Info Notice */}
            <div className="rounded-md bg-blue-50 p-4">
              <div className="text-sm text-blue-800">
                Đăng ký để trở thành khách hàng. Tài khoản sẽ được tự động kích hoạt.
              </div>
            </div>

            {/* Email */}
            <div>
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="example@email.com"
                error={errors.email?.message}
                {...register("email")}
              />
            </div>

            {/* Full Name */}
            <div>
              <Label htmlFor="fullName">Họ và tên</Label>
              <Input
                id="fullName"
                type="text"
                autoComplete="name"
                placeholder="Nguyễn Văn A"
                error={errors.fullName?.message}
                {...register("fullName")}
              />
            </div>

            {/* Phone Number */}
            <div>
              <Label htmlFor="phone">Số điện thoại (tùy chọn)</Label>
              <Input
                id="phone"
                type="tel"
                autoComplete="tel"
                placeholder="0912345678 (10-15 chữ số)"
                error={errors.phone?.message}
                {...register("phone")}
              />
            </div>

            {/* Password */}
            <div>
              <Label htmlFor="password">Mật khẩu</Label>
              <Input
                id="password"
                type="password"
                autoComplete="new-password"
                placeholder="Tối thiểu 8 ký tự, có chữ hoa, số và ký tự đặc biệt"
                error={errors.password?.message}
                {...register("password")}
              />

              {/* Password Strength Indicator */}
              {password && password.length > 0 && (
                <div className="mt-2">
                  <div className="flex gap-1">
                    {[1, 2, 3, 4, 5].map((level) => (
                      <div
                        key={level}
                        className={`h-2 flex-1 rounded ${
                          level <= passwordStrength.strength
                            ? passwordStrength.color
                            : "bg-gray-200"
                        }`}
                      />
                    ))}
                  </div>
                  <p className="mt-1 text-xs text-gray-600">
                    Độ mạnh: {passwordStrength.label}
                  </p>
                </div>
              )}

              {/* Password Requirements */}
              <div className="mt-2 text-xs text-gray-500 space-y-1">
                <p>Mật khẩu phải có:</p>
                <ul className="list-disc list-inside pl-2">
                  <li>Ít nhất 8 ký tự</li>
                  <li>Ít nhất 1 chữ thường (a-z)</li>
                  <li>Ít nhất 1 chữ hoa (A-Z)</li>
                  <li>Ít nhất 1 số (0-9)</li>
                  <li>Ít nhất 1 ký tự đặc biệt (@$!%*?&)</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Terms */}
          <div className="text-sm text-gray-600">
            Bằng cách đăng ký, bạn đồng ý với{" "}
            <a href="#" className="text-blue-600 hover:text-blue-500">
              Điều khoản dịch vụ
            </a>{" "}
            và{" "}
            <a href="#" className="text-blue-600 hover:text-blue-500">
              Chính sách bảo mật
            </a>
          </div>

          {/* Submit Button */}
          <div>
            <Button
              type="submit"
              className="w-full"
              isLoading={isSubmitting}
            >
              Tạo tài khoản
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};