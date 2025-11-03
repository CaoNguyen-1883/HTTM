import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  useUsers,
  useSearchUsers,
  useUsersByRole,
  useCreateUser,
  useUpdateUser,
  useDeleteUser,
  useAssignRoles,
  useVerifyEmail,
  useRoles,
} from "../../lib/hooks/useUsers";
import { UserSummary, CreateUserRequest, UpdateUserRequest } from "../../lib/types";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Label } from "../../components/ui/Label";
import { Search, Plus, Edit, Trash2, CheckCircle, X } from "lucide-react";
import { formatDate } from "../../lib/utils";

// Validation schema
const userSchema = z.object({
  email: z.string().email("Email không hợp lệ"),
  fullName: z.string().min(2, "Tên phải có ít nhất 2 ký tự").max(100),
  password: z.string().min(8, "Mật khẩu phải có ít nhất 8 ký tự").optional(),
  phone: z.string().regex(/^[0-9]{10,15}$/, "Số điện thoại phải 10-15 chữ số").optional().or(z.literal("")),
  avatarUrl: z.string().url("URL không hợp lệ").optional().or(z.literal("")),
});

type UserFormData = z.infer<typeof userSchema>;

export const AdminUsersPage = () => {
  const [searchKeyword, setSearchKeyword] = useState("");
  const [selectedRole, setSelectedRole] = useState<string>("");
  const [page, setPage] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserSummary | null>(null);
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [selectedRoleIds, setSelectedRoleIds] = useState<string[]>([]);

  // Queries
  const { data: rolesData } = useRoles();
  const roles = rolesData || [];

  // Determine which query to use based on filters
  const shouldSearch = searchKeyword.length > 0;
  const shouldFilterByRole = !!selectedRole && !shouldSearch;

  const { data: usersData, isLoading } = useUsers(
    { page, size: 20 }
  );

  const { data: searchData } = useSearchUsers(
    searchKeyword,
    { page, size: 20 }
  );

  const { data: roleFilterData } = useUsersByRole(
    selectedRole,
    { page, size: 20 }
  );

  // Get the appropriate data
  const currentData = shouldSearch
    ? searchData
    : shouldFilterByRole
    ? roleFilterData
    : usersData;

  const users = currentData?.content || [];
  const totalPages = currentData?.totalPages || 0;

  // Mutations
  const createUserMutation = useCreateUser();
  const updateUserMutation = useUpdateUser();
  const deleteUserMutation = useDeleteUser();
  const assignRolesMutation = useAssignRoles();
  const verifyEmailMutation = useVerifyEmail();

  // Form
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
  });

  // Handlers
  const handleOpenCreateModal = () => {
    setEditingUser(null);
    reset({
      email: "",
      fullName: "",
      password: "",
      phone: "",
      avatarUrl: "",
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (user: UserSummary) => {
    setEditingUser(user);
    reset({
      email: user.email,
      fullName: user.fullName,
      phone: "",
      avatarUrl: user.avatarUrl || "",
      password: undefined,
    });
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingUser(null);
    reset();
  };

  const handleOpenRoleModal = (userId: string, currentRoles: string[]) => {
    setSelectedUserId(userId);
    // Convert role names to role IDs
    const roleIds = roles
      .filter((role) => currentRoles.includes(role.name))
      .map((role) => role.id);
    setSelectedRoleIds(roleIds);
    setIsRoleModalOpen(true);
  };

  const handleCloseRoleModal = () => {
    setIsRoleModalOpen(false);
    setSelectedUserId(null);
    setSelectedRoleIds([]);
  };

  const onSubmit = async (data: UserFormData) => {
    try {
      if (editingUser) {
        // Update user
        const updateData: UpdateUserRequest = {
          fullName: data.fullName,
          phone: data.phone || undefined,
          avatarUrl: data.avatarUrl || undefined,
        };
        await updateUserMutation.mutateAsync({ id: editingUser.id, data: updateData });
        alert("Cập nhật user thành công!");
      } else {
        // Create user
        const createData: CreateUserRequest = {
          email: data.email,
          fullName: data.fullName,
          password: data.password || "",
          phone: data.phone || undefined,
          avatarUrl: data.avatarUrl || undefined,
        };
        await createUserMutation.mutateAsync(createData);
        alert("Tạo user thành công!");
      }
      handleCloseModal();
    } catch (error: any) {
      alert(error.response?.data?.message || "Thao tác thất bại");
    }
  };

  const handleDelete = async (userId: string) => {
    if (!confirm("Bạn có chắc muốn xóa user này?")) return;

    try {
      await deleteUserMutation.mutateAsync(userId);
      alert("Xóa user thành công!");
    } catch (error: any) {
      alert(error.response?.data?.message || "Xóa thất bại");
    }
  };

  const handleVerifyEmail = async (userId: string) => {
    try {
      await verifyEmailMutation.mutateAsync(userId);
      alert("Xác thực email thành công!");
    } catch (error: any) {
      alert(error.response?.data?.message || "Xác thực thất bại");
    }
  };

  const handleSaveRoles = async () => {
    if (!selectedUserId) return;

    try {
      await assignRolesMutation.mutateAsync({
        userId: selectedUserId,
        roleIds: selectedRoleIds,
      });
      alert("Cập nhật roles thành công!");
      handleCloseRoleModal();
    } catch (error: any) {
      alert(error.response?.data?.message || "Cập nhật thất bại");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Quản lý Users</h1>
        <Button onClick={handleOpenCreateModal}>
          <Plus className="w-4 h-4 mr-2" />
          Tạo User
        </Button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow p-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <Input
              type="text"
              placeholder="Tìm kiếm theo email hoặc tên..."
              value={searchKeyword}
              onChange={(e) => {
                setSearchKeyword(e.target.value);
                setPage(0);
              }}
              className="pl-10"
            />
          </div>

          {/* Role Filter */}
          <div>
            <select
              value={selectedRole}
              onChange={(e) => {
                setSelectedRole(e.target.value);
                setPage(0);
              }}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Tất cả vai trò</option>
              {roles.map((role) => (
                <option key={role.id} value={role.name}>
                  {role.name.replace("ROLE_", "")}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        ) : users.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            Không tìm thấy user nào
          </div>
        ) : (
          <>
            <table className="w-full">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    User
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Roles
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Email Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ngày tạo
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {users.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {user.avatarUrl && (
                          <img
                            src={user.avatarUrl}
                            alt={user.fullName}
                            className="h-10 w-10 rounded-full mr-3"
                          />
                        )}
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {user.fullName}
                          </div>
                          <div className="text-sm text-gray-500">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex flex-wrap gap-1">
                        {user.roleNames.map((roleName) => (
                          <span
                            key={roleName}
                            className="px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-700"
                          >
                            {roleName.replace("ROLE_", "")}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {user.emailVerified ? (
                        <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-700">
                          ✓ Verified
                        </span>
                      ) : (
                        <span className="px-2 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-700">
                          Chưa xác thực
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(user.createdAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleOpenEditModal(user)}
                        >
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleOpenRoleModal(user.id, user.roleNames)}
                        >
                          Roles
                        </Button>
                        {!user.emailVerified && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleVerifyEmail(user.id)}
                          >
                            <CheckCircle className="w-4 h-4" />
                          </Button>
                        )}
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleDelete(user.id)}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="px-6 py-4 border-t flex items-center justify-between">
                <div className="text-sm text-gray-500">
                  Trang {page + 1} / {totalPages}
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                  >
                    Trước
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                  >
                    Sau
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Create/Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between p-6 border-b">
              <h2 className="text-xl font-bold">
                {editingUser ? "Chỉnh sửa User" : "Tạo User mới"}
              </h2>
              <button onClick={handleCloseModal} className="text-gray-400 hover:text-gray-600">
                <X className="w-6 h-6" />
              </button>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-4">
              {/* Email */}
              <div>
                <Label htmlFor="email">Email *</Label>
                <Input
                  id="email"
                  type="email"
                  {...register("email")}
                  disabled={!!editingUser}
                  error={errors.email?.message}
                />
              </div>

              {/* Full Name */}
              <div>
                <Label htmlFor="fullName">Họ và tên *</Label>
                <Input
                  id="fullName"
                  {...register("fullName")}
                  error={errors.fullName?.message}
                />
              </div>

              {/* Password (only for create) */}
              {!editingUser && (
                <div>
                  <Label htmlFor="password">Mật khẩu *</Label>
                  <Input
                    id="password"
                    type="password"
                    {...register("password")}
                    error={errors.password?.message}
                  />
                </div>
              )}

              {/* Phone */}
              <div>
                <Label htmlFor="phone">Số điện thoại</Label>
                <Input
                  id="phone"
                  {...register("phone")}
                  placeholder="0123456789"
                  error={errors.phone?.message}
                />
              </div>

              {/* Avatar URL */}
              <div>
                <Label htmlFor="avatarUrl">Avatar URL</Label>
                <Input
                  id="avatarUrl"
                  {...register("avatarUrl")}
                  placeholder="https://..."
                  error={errors.avatarUrl?.message}
                />
              </div>

              {/* Actions */}
              <div className="flex justify-end gap-3 pt-4 border-t">
                <Button type="button" variant="outline" onClick={handleCloseModal}>
                  Hủy
                </Button>
                <Button
                  type="submit"
                  isLoading={createUserMutation.isPending || updateUserMutation.isPending}
                >
                  {editingUser ? "Cập nhật" : "Tạo mới"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Role Assignment Modal */}
      {isRoleModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="flex items-center justify-between p-6 border-b">
              <h2 className="text-xl font-bold">Quản lý Roles</h2>
              <button onClick={handleCloseRoleModal} className="text-gray-400 hover:text-gray-600">
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="p-6 space-y-3">
              {roles.map((role) => (
                <label key={role.id} className="flex items-center gap-3 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedRoleIds.includes(role.id)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedRoleIds([...selectedRoleIds, role.id]);
                      } else {
                        setSelectedRoleIds(selectedRoleIds.filter((id) => id !== role.id));
                      }
                    }}
                    className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                  />
                  <div>
                    <div className="font-medium">{role.name.replace("ROLE_", "")}</div>
                    {role.description && (
                      <div className="text-sm text-gray-500">{role.description}</div>
                    )}
                  </div>
                </label>
              ))}
            </div>

            <div className="flex justify-end gap-3 p-6 border-t">
              <Button variant="outline" onClick={handleCloseRoleModal}>
                Hủy
              </Button>
              <Button
                onClick={handleSaveRoles}
                isLoading={assignRolesMutation.isPending}
              >
                Lưu
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};