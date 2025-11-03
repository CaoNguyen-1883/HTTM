import { useState } from "react";
import { Product, ProductStatus } from "../../lib/types";
import { useApproveProduct, useRejectProduct } from "../../lib/hooks/useProducts";
import { X, CheckCircle, XCircle, Package, Tag, Star, Eye } from "lucide-react";

interface ProductDetailModalProps {
  product: Product;
  onClose: () => void;
}

export const ProductDetailModal = ({ product, onClose }: ProductDetailModalProps) => {
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [rejectReason, setRejectReason] = useState("");

  const approveMutation = useApproveProduct();
  const rejectMutation = useRejectProduct();

  const handleApprove = async () => {
    try {
      await approveMutation.mutateAsync(product.id);
      alert("Product approved successfully!");
      onClose();
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to approve product");
    }
  };

  const handleReject = async () => {
    if (!rejectReason.trim()) {
      alert("Please provide a reason for rejection");
      return;
    }

    try {
      await rejectMutation.mutateAsync({ id: product.id, reason: rejectReason });
      alert("Product rejected successfully!");
      onClose();
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
      <span className={`px-3 py-1 text-sm font-medium rounded-full ${styles[status]}`}>
        {status.replace("_", " ")}
      </span>
    );
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <h2 className="text-2xl font-bold text-gray-900">Product Details</h2>
            {getStatusBadge(product.status)}
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Images */}
          {product.images && product.images.length > 0 && (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {product.images.map((image, index) => (
                <div key={image.id} className="relative aspect-square bg-gray-100 rounded-lg overflow-hidden">
                  <img
                    src={image.imageUrl}
                    alt={image.altText || `Product image ${index + 1}`}
                    className="w-full h-full object-cover"
                  />
                  {image.isPrimary && (
                    <div className="absolute top-2 left-2 bg-blue-600 text-white text-xs px-2 py-1 rounded">
                      Primary
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {/* Basic Info */}
          <div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">{product.name}</h3>
            {product.description && (
              <p className="text-gray-600">{product.description}</p>
            )}
          </div>

          {/* Details Grid */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex items-center gap-2 text-gray-600 mb-1">
                <Tag className="w-4 h-4" />
                <span className="text-sm">Base Price</span>
              </div>
              <p className="text-xl font-bold text-gray-900">
                {product.basePrice != null ? `$${product.basePrice.toFixed(2)}` : 'Not set'}
              </p>
            </div>

            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex items-center gap-2 text-gray-600 mb-1">
                <Package className="w-4 h-4" />
                <span className="text-sm">Total Stock</span>
              </div>
              <p className="text-xl font-bold text-gray-900">{product.totalStock || 0}</p>
            </div>

            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex items-center gap-2 text-gray-600 mb-1">
                <Star className="w-4 h-4" />
                <span className="text-sm">Rating</span>
              </div>
              <p className="text-xl font-bold text-gray-900">
                {product.averageRating != null ? product.averageRating.toFixed(1) : '0.0'} ({product.totalReviews || 0})
              </p>
            </div>

            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex items-center gap-2 text-gray-600 mb-1">
                <Eye className="w-4 h-4" />
                <span className="text-sm">Views</span>
              </div>
              <p className="text-xl font-bold text-gray-900">{product.viewCount}</p>
            </div>
          </div>

          {/* Category & Brand */}
          <div className="flex gap-4">
            <div className="flex-1 bg-gray-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600 mb-1">Category</p>
              <p className="font-semibold text-gray-900">{product.category?.name || 'N/A'}</p>
            </div>
            <div className="flex-1 bg-gray-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600 mb-1">Brand</p>
              <p className="font-semibold text-gray-900">{product.brand?.name || 'N/A'}</p>
            </div>
            <div className="flex-1 bg-gray-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600 mb-1">Seller</p>
              <p className="font-semibold text-gray-900">{product.sellerName || 'N/A'}</p>
            </div>
          </div>

          {/* Variants */}
          {product.variants && product.variants.length > 0 && (
            <div>
              <h4 className="font-bold text-gray-900 mb-3">Product Variants ({product.variants.length})</h4>
              <div className="space-y-3">
                {product.variants.map((variant) => (
                  <div key={variant.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <h5 className="font-semibold text-gray-900">{variant.name}</h5>
                        {variant.isDefault && (
                          <span className="bg-blue-100 text-blue-700 text-xs px-2 py-1 rounded">
                            Default
                          </span>
                        )}
                      </div>
                      <span className="text-lg font-bold text-blue-600">
                        {variant.price != null ? `$${variant.price.toFixed(2)}` : 'Not set'}
                      </span>
                    </div>
                    <div className="grid grid-cols-3 gap-4 text-sm">
                      <div>
                        <span className="text-gray-600">SKU:</span>
                        <span className="ml-2 font-medium text-gray-900">{variant.sku}</span>
                      </div>
                      <div>
                        <span className="text-gray-600">Stock:</span>
                        <span className="ml-2 font-medium text-gray-900">{variant.stock}</span>
                      </div>
                      <div>
                        <span className="text-gray-600">Available:</span>
                        <span className="ml-2 font-medium text-gray-900">{variant.availableStock}</span>
                      </div>
                    </div>
                    {variant.attributes && Object.keys(variant.attributes).length > 0 && (
                      <div className="mt-2 flex flex-wrap gap-2">
                        {Object.entries(variant.attributes).map(([key, value]) => (
                          <span key={key} className="bg-gray-100 text-gray-700 text-xs px-2 py-1 rounded">
                            {key}: {value}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Tags */}
          {product.tags && product.tags.length > 0 && (
            <div>
              <h4 className="font-bold text-gray-900 mb-2">Tags</h4>
              <div className="flex flex-wrap gap-2">
                {product.tags.map((tag, index) => (
                  <span key={index} className="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm">
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Rejection Form */}
          {showRejectForm && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <h4 className="font-bold text-red-900 mb-2">Reject Product</h4>
              <textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Please provide a reason for rejection..."
                className="w-full px-3 py-2 border border-red-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                rows={4}
              />
              <div className="flex gap-2 mt-3">
                <button
                  onClick={handleReject}
                  disabled={rejectMutation.isPending}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium disabled:opacity-50"
                >
                  {rejectMutation.isPending ? "Rejecting..." : "Confirm Rejection"}
                </button>
                <button
                  onClick={() => setShowRejectForm(false)}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium"
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Actions */}
        {product.status === ProductStatus.PENDING && !showRejectForm && (
          <div className="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4 flex items-center justify-end gap-3">
            <button
              onClick={() => setShowRejectForm(true)}
              className="flex items-center gap-2 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium"
            >
              <XCircle className="w-5 h-5" />
              Reject Product
            </button>
            <button
              onClick={handleApprove}
              disabled={approveMutation.isPending}
              className="flex items-center gap-2 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium disabled:opacity-50"
            >
              <CheckCircle className="w-5 h-5" />
              {approveMutation.isPending ? "Approving..." : "Approve Product"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
