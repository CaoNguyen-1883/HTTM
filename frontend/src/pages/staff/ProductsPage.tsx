import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useAdminProducts, useProduct, useApproveProduct, useRejectProduct } from "../../lib/hooks/useProducts";
import { ProductStatus } from "../../lib/types";
import { Search, Filter, ChevronLeft, ChevronRight, Eye, CheckCircle, XCircle, X } from "lucide-react";
import { ProductDetailModal } from "../../components/admin/ProductDetailModal";

export const StaffProductsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  // Get state from URL params or use defaults
  const currentPage = parseInt(searchParams.get("page") || "0");
  const keyword = searchParams.get("keyword") || "";
  const statusFilter = (searchParams.get("status") || "") as ProductStatus | "";

  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);
  const [rejectingProductId, setRejectingProductId] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState("");

  // Local state for search input (only updates URL on submit)
  const [searchInput, setSearchInput] = useState(keyword);

  const { data: productsData, isLoading } = useAdminProducts({
    keyword: keyword || undefined,
    status: statusFilter || undefined,
    page: currentPage,
    size: 12,
    sort: "createdAt,desc",
  });

  const { data: selectedProduct } = useProduct(selectedProductId || "");
  const approveMutation = useApproveProduct();
  const rejectMutation = useRejectProduct();

  // Helper function to update URL params
  const updateParams = (updates: Record<string, string | number | undefined>) => {
    const newParams = new URLSearchParams(searchParams);

    Object.entries(updates).forEach(([key, value]) => {
      if (value === undefined || value === "" || value === null) {
        newParams.delete(key);
      } else {
        newParams.set(key, String(value));
      }
    });

    setSearchParams(newParams);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    updateParams({ keyword: searchInput, page: 0 });
  };

  const handlePageChange = (newPage: number) => {
    updateParams({ page: newPage });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleStatusChange = (status: ProductStatus | "") => {
    updateParams({ status: status || undefined, page: 0 });
  };

  const handleApprove = async (productId: string, productName: string) => {
    if (!confirm(`Are you sure you want to approve "${productName}"?`)) {
      return;
    }

    try {
      await approveMutation.mutateAsync(productId);
      alert("Product approved successfully!");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to approve product");
    }
  };

  const openRejectModal = (productId: string) => {
    setRejectingProductId(productId);
    setRejectReason("");
  };

  const handleReject = async () => {
    if (!rejectReason.trim()) {
      alert("Please provide a reason for rejection");
      return;
    }

    try {
      await rejectMutation.mutateAsync({ id: rejectingProductId!, reason: rejectReason });
      alert("Product rejected successfully!");
      setRejectingProductId(null);
      setRejectReason("");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to reject product");
    }
  };

  const getStatusBadge = (status: ProductStatus) => {
    const styles = {
      [ProductStatus.PENDING]: "bg-yellow-100 text-yellow-700",
      [ProductStatus.APPROVED]: "bg-green-100 text-green-700",
      [ProductStatus.REJECTED]: "bg-red-100 text-red-700",
      [ProductStatus.ACTIVE]: "bg-blue-100 text-blue-700",
      [ProductStatus.INACTIVE]: "bg-gray-100 text-gray-700",
      [ProductStatus.OUT_OF_STOCK]: "bg-orange-100 text-orange-700",
    };

    return (
      <span className={`px-2 py-1 text-xs font-medium rounded-full ${styles[status]}`}>
        {status.replace("_", " ")}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Product Approval</h1>
        <p className="text-gray-600 mt-1">
          Review and approve products submitted by sellers
        </p>
      </div>

      {/* Quick Stats */}
      {productsData && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div
            className="bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => handleStatusChange(ProductStatus.PENDING)}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Pending Approval</p>
                <p className="text-2xl font-bold text-yellow-600">
                  {productsData.content.filter(p => p.status === ProductStatus.PENDING).length}
                </p>
              </div>
              <div className="bg-yellow-100 p-3 rounded-full">
                <Filter className="w-6 h-6 text-yellow-600" />
              </div>
            </div>
          </div>
          <div
            className="bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => handleStatusChange(ProductStatus.APPROVED)}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Approved</p>
                <p className="text-2xl font-bold text-green-600">
                  {productsData.content.filter(p => p.status === ProductStatus.APPROVED).length}
                </p>
              </div>
              <div className="bg-green-100 p-3 rounded-full">
                <CheckCircle className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </div>
          <div
            className="bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => handleStatusChange(ProductStatus.REJECTED)}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Rejected</p>
                <p className="text-2xl font-bold text-red-600">
                  {productsData.content.filter(p => p.status === ProductStatus.REJECTED).length}
                </p>
              </div>
              <div className="bg-red-100 p-3 rounded-full">
                <XCircle className="w-6 h-6 text-red-600" />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow p-6">
        <form onSubmit={handleSearch} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Search */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Search Products
              </label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  placeholder="Search by product name..."
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            {/* Status Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                <Filter className="inline w-4 h-4 mr-1" />
                Product Status
              </label>
              <select
                value={statusFilter}
                onChange={(e) => handleStatusChange(e.target.value as ProductStatus | "")}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">All Statuses</option>
                {Object.values(ProductStatus).map((status) => (
                  <option key={status} value={status}>
                    {status.replace("_", " ")}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </form>
      </div>

      {/* Products Grid */}
      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading products...</div>
        </div>
      ) : productsData?.content.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <div className="text-gray-500 text-lg">No products found</div>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {productsData?.content.map((product) => (
              <div
                key={product.id}
                className="bg-white rounded-lg shadow overflow-hidden hover:shadow-md transition-shadow"
              >
                <div className="aspect-w-16 aspect-h-9 bg-gray-200">
                  <img
                    src={product.primaryImage || "https://via.placeholder.com/400x300"}
                    alt={product.name}
                    className="w-full h-48 object-cover"
                  />
                </div>
                <div className="p-4">
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <h3 className="font-semibold text-gray-900 line-clamp-2 flex-1">
                      {product.name}
                    </h3>
                    {getStatusBadge(product.status)}
                  </div>

                  <p className="text-sm text-gray-600 line-clamp-2 mb-3">
                    {product.shortDescription}
                  </p>

                  <div className="flex items-center gap-2 text-sm text-gray-500 mb-3">
                    {product.minPrice != null ? (
                      <>
                        <span className="font-medium text-blue-600">
                          ${product.minPrice.toFixed(2)}
                        </span>
                        {product.maxPrice != null && product.minPrice !== product.maxPrice && (
                          <>
                            <span>-</span>
                            <span className="font-medium text-blue-600">
                              ${product.maxPrice.toFixed(2)}
                            </span>
                          </>
                        )}
                      </>
                    ) : (
                      <span className="text-gray-400">Price not set</span>
                    )}
                  </div>

                  <div className="flex items-center justify-between text-xs text-gray-500 mb-3">
                    <span>{product.categoryName}</span>
                    <span>{product.brandName}</span>
                  </div>

                  <div className="flex items-center justify-between text-xs text-gray-500 mb-4">
                    <span>⭐ {product.averageRating != null ? product.averageRating.toFixed(1) : '0.0'} ({product.totalReviews || 0})</span>
                    <span>Stock: {product.totalStock || 0}</span>
                  </div>

                  {product.status === ProductStatus.PENDING ? (
                    <div className="space-y-2">
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleApprove(product.id, product.name)}
                          disabled={approveMutation.isPending}
                          className="flex-1 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm font-medium flex items-center justify-center gap-1 disabled:opacity-50"
                        >
                          <CheckCircle className="w-4 h-4" />
                          Approve
                        </button>
                        <button
                          onClick={() => openRejectModal(product.id)}
                          className="flex-1 px-3 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-medium flex items-center justify-center gap-1"
                        >
                          <XCircle className="w-4 h-4" />
                          Reject
                        </button>
                      </div>
                      <button
                        onClick={() => setSelectedProductId(product.id)}
                        className="w-full px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium flex items-center justify-center gap-1"
                      >
                        <Eye className="w-4 h-4" />
                        View Details
                      </button>
                    </div>
                  ) : (
                    <div className="flex gap-2">
                      <button
                        onClick={() => setSelectedProductId(product.id)}
                        className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium flex items-center justify-center gap-1"
                      >
                        <Eye className="w-4 h-4" />
                        View
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {productsData && productsData.totalPages > 1 && (
            <div className="flex items-center justify-between bg-white rounded-lg shadow px-6 py-4">
              <div className="text-sm text-gray-600">
                Showing page {currentPage + 1} of {productsData.totalPages}
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  <ChevronLeft className="w-4 h-4" />
                  Previous
                </button>
                <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage >= productsData.totalPages - 1}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  Next
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Reject Reason Modal */}
      {rejectingProductId && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="flex items-center justify-between p-6 border-b border-gray-200">
              <h3 className="text-xl font-bold text-gray-900">Reject Product</h3>
              <button
                onClick={() => setRejectingProductId(null)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-6 h-6" />
              </button>
            </div>
            <div className="p-6 space-y-4">
              <p className="text-gray-600">
                Please provide a reason for rejecting this product. This will be sent to the seller.
              </p>
              <textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Enter rejection reason..."
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                rows={5}
                autoFocus
              />
            </div>
            <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200">
              <button
                onClick={() => setRejectingProductId(null)}
                className="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium"
              >
                Cancel
              </button>
              <button
                onClick={handleReject}
                disabled={rejectMutation.isPending || !rejectReason.trim()}
                className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium disabled:opacity-50"
              >
                {rejectMutation.isPending ? "Rejecting..." : "Confirm Rejection"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Product Detail Modal */}
      {selectedProduct && (
        <ProductDetailModal
          product={selectedProduct}
          onClose={() => setSelectedProductId(null)}
        />
      )}
    </div>
  );
};
