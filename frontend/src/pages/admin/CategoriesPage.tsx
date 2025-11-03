import { useState } from "react";
import {
  useAllCategories,
  useCreateCategory,
  useUpdateCategory,
  useDeleteCategory,
} from "../../lib/hooks/useCategories";
import { Category } from "../../lib/types";
import { Plus, Edit, Trash2, X, ChevronRight, FolderTree } from "lucide-react";
import { CategoryRequest } from "../../lib/api/categories.api";

export const AdminCategoriesPage = () => {
  const { data: categories = [], isLoading } = useAllCategories();
  const createMutation = useCreateCategory();
  const updateMutation = useUpdateCategory();
  const deleteMutation = useDeleteCategory();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  // Form state
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [parentId, setParentId] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [displayOrder, setDisplayOrder] = useState("");

  const resetForm = () => {
    setName("");
    setDescription("");
    setParentId("");
    setImageUrl("");
    setDisplayOrder("");
    setEditingCategory(null);
  };

  const openCreateModal = (parent?: Category) => {
    resetForm();
    if (parent) {
      setParentId(parent.id);
    }
    setIsModalOpen(true);
  };

  const openEditModal = (category: Category) => {
    setEditingCategory(category);
    setName(category.name);
    setDescription(category.description || "");
    setParentId(category.parentId || "");
    setImageUrl(category.imageUrl || "");
    setDisplayOrder(category.displayOrder?.toString() || "");
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    resetForm();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      alert("Category name is required");
      return;
    }

    const categoryData: CategoryRequest = {
      name: name.trim(),
      description: description.trim() || undefined,
      parentId: parentId || undefined,
      imageUrl: imageUrl.trim() || undefined,
      displayOrder: displayOrder ? parseInt(displayOrder) : undefined,
    };

    try {
      if (editingCategory) {
        await updateMutation.mutateAsync({ id: editingCategory.id, data: categoryData });
        alert("Category updated successfully!");
      } else {
        await createMutation.mutateAsync(categoryData);
        alert("Category created successfully!");
      }
      closeModal();
    } catch (error: any) {
      alert(error.response?.data?.message || `Failed to ${editingCategory ? "update" : "create"} category`);
    }
  };

  const handleDelete = async (category: Category) => {
    if (!confirm(`Are you sure you want to delete "${category.name}"?`)) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(category.id);
      alert("Category deleted successfully!");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to delete category");
    }
  };

  // Organize categories by parent-child relationship
  const rootCategories = categories.filter((cat) => !cat.parentId);
  const getCategoryChildren = (parentId: string) =>
    categories.filter((cat) => cat.parentId === parentId);

  const renderCategory = (category: Category, level: number = 0) => {
    const children = getCategoryChildren(category.id);
    const hasChildren = children.length > 0;

    return (
      <div key={category.id} className="mb-2">
        <div
          className="bg-white rounded-lg shadow hover:shadow-md transition-shadow p-4"
          style={{ marginLeft: `${level * 24}px` }}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3 flex-1">
              {level > 0 && <ChevronRight className="w-4 h-4 text-gray-400" />}
              {category.imageUrl && (
                <img
                  src={category.imageUrl}
                  alt={category.name}
                  className="w-12 h-12 rounded object-cover"
                />
              )}
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <h3 className="font-semibold text-gray-900">{category.name}</h3>
                  {hasChildren && (
                    <span className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded-full">
                      {children.length} sub
                    </span>
                  )}
                </div>
                {category.description && (
                  <p className="text-sm text-gray-600 line-clamp-1">{category.description}</p>
                )}
                <div className="flex items-center gap-3 mt-1 text-xs text-gray-500">
                  {category.parentName && (
                    <span>Parent: {category.parentName}</span>
                  )}
                  {category.displayOrder !== null && category.displayOrder !== undefined && (
                    <span>Order: {category.displayOrder}</span>
                  )}
                </div>
              </div>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => openCreateModal(category)}
                className="p-2 text-green-600 hover:bg-green-50 rounded-lg"
                title="Add sub-category"
              >
                <Plus className="w-4 h-4" />
              </button>
              <button
                onClick={() => openEditModal(category)}
                className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg"
              >
                <Edit className="w-4 h-4" />
              </button>
              <button
                onClick={() => handleDelete(category)}
                disabled={deleteMutation.isPending || hasChildren}
                className="p-2 text-red-600 hover:bg-red-50 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
                title={hasChildren ? "Delete sub-categories first" : "Delete category"}
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
        {children.map((child) => renderCategory(child, level + 1))}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Category Management</h1>
          <p className="text-gray-600 mt-1">Manage product categories and sub-categories</p>
        </div>
        <button
          onClick={() => openCreateModal()}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
        >
          <Plus className="w-5 h-5" />
          Add Root Category
        </button>
      </div>

      {/* Categories List */}
      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading categories...</div>
        </div>
      ) : categories.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <FolderTree className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <div className="text-gray-500 text-lg mb-4">No categories found</div>
          <button
            onClick={() => openCreateModal()}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
          >
            Create Your First Category
          </button>
        </div>
      ) : (
        <div className="space-y-2">
          {rootCategories.map((category) => renderCategory(category))}
        </div>
      )}

      {/* Create/Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-2xl font-bold text-gray-900">
                {editingCategory ? "Edit Category" : "Create New Category"}
              </h2>
              <button onClick={closeModal} className="text-gray-400 hover:text-gray-600">
                <X className="w-6 h-6" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Category Name *
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="e.g., Electronics"
                  required
                  maxLength={100}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Description
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Category description..."
                  rows={3}
                  maxLength={500}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Parent Category
                </label>
                <select
                  value={parentId}
                  onChange={(e) => setParentId(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  disabled={!!editingCategory && getCategoryChildren(editingCategory.id).length > 0}
                >
                  <option value="">None (Root Category)</option>
                  {categories
                    .filter((cat) => cat.id !== editingCategory?.id)
                    .map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.parentName ? `${cat.parentName} > ` : ""}{cat.name}
                      </option>
                    ))}
                </select>
                {editingCategory && getCategoryChildren(editingCategory.id).length > 0 && (
                  <p className="text-xs text-gray-500 mt-1">
                    Cannot change parent category when sub-categories exist
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Image URL
                </label>
                <input
                  type="url"
                  value={imageUrl}
                  onChange={(e) => setImageUrl(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="https://example.com/image.jpg"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Display Order
                </label>
                <input
                  type="number"
                  value={displayOrder}
                  onChange={(e) => setDisplayOrder(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="0"
                  min="0"
                />
                <p className="text-xs text-gray-500 mt-1">
                  Lower numbers appear first in lists
                </p>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50"
                >
                  {createMutation.isPending || updateMutation.isPending
                    ? "Saving..."
                    : editingCategory
                    ? "Update Category"
                    : "Create Category"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
