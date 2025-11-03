import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useOrders } from "../../lib/hooks";
import { OrderStatus } from "../../lib/types";
import {
  Package,
  Calendar,
  DollarSign,
  Eye,
  ShoppingBag,
  Clock,
  CheckCircle,
  Truck,
  XCircle,
  PackageCheck,
} from "lucide-react";
import { format } from "date-fns";

const statusConfig = {
  [OrderStatus.PENDING]: {
    label: "Pending",
    color: "bg-yellow-100 text-yellow-800 border-yellow-200",
    icon: Clock,
  },
  [OrderStatus.CONFIRMED]: {
    label: "Confirmed",
    color: "bg-blue-100 text-blue-800 border-blue-200",
    icon: CheckCircle,
  },
  [OrderStatus.PROCESSING]: {
    label: "Processing",
    color: "bg-purple-100 text-purple-800 border-purple-200",
    icon: Package,
  },
  [OrderStatus.SHIPPED]: {
    label: "Shipped",
    color: "bg-indigo-100 text-indigo-800 border-indigo-200",
    icon: Truck,
  },
  [OrderStatus.DELIVERED]: {
    label: "Delivered",
    color: "bg-green-100 text-green-800 border-green-200",
    icon: PackageCheck,
  },
  [OrderStatus.CANCELLED]: {
    label: "Cancelled",
    color: "bg-red-100 text-red-800 border-red-200",
    icon: XCircle,
  },
};

export const CustomerOrdersPage = () => {
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "ALL">("ALL");
  const [currentPage, setCurrentPage] = useState(0);

  const { data: ordersData, isLoading } = useOrders({
    orderStatus: statusFilter === "ALL" ? undefined : statusFilter,
    page: currentPage,
    size: 10,
    sort: "createdAt,desc",
  });

  const handleViewOrder = (orderId: string) => {
    navigate(`/customer/orders/${orderId}`);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading orders...</div>
      </div>
    );
  }

  const orders = ordersData?.content || [];
  const totalPages = ordersData?.totalPages || 0;
  const isEmpty = orders.length === 0;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
            <Package className="w-8 h-8" />
            My Orders
          </h1>
          <p className="text-gray-600 mt-1">
            {isEmpty
              ? "You haven't placed any orders yet"
              : `${ordersData?.totalElements || 0} order${
                  (ordersData?.totalElements || 0) > 1 ? "s" : ""
                } found`}
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Status Filter */}
        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => {
                setStatusFilter("ALL");
                setCurrentPage(0);
              }}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                statusFilter === "ALL"
                  ? "bg-blue-600 text-white"
                  : "bg-gray-100 text-gray-700 hover:bg-gray-200"
              }`}
            >
              All Orders
            </button>
            {Object.entries(statusConfig).map(([status, config]) => {
              const Icon = config.icon;
              return (
                <button
                  key={status}
                  onClick={() => {
                    setStatusFilter(status as OrderStatus);
                    setCurrentPage(0);
                  }}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-2 ${
                    statusFilter === status
                      ? "bg-blue-600 text-white"
                      : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {config.label}
                </button>
              );
            })}
          </div>
        </div>

        {isEmpty ? (
          /* Empty State */
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <ShoppingBag className="w-24 h-24 mx-auto text-gray-400 mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              No orders found
            </h2>
            <p className="text-gray-600 mb-6">
              {statusFilter === "ALL"
                ? "You haven't placed any orders yet"
                : `No ${
                    statusConfig[statusFilter as OrderStatus]?.label.toLowerCase()
                  } orders found`}
            </p>
            <button
              onClick={() => navigate("/products")}
              className="inline-flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
            >
              <ShoppingBag className="w-5 h-5" />
              Start Shopping
            </button>
          </div>
        ) : (
          <>
            {/* Orders List */}
            <div className="space-y-4">
              {orders.map((order) => {
                const statusInfo = statusConfig[order.orderStatus];
                const StatusIcon = statusInfo.icon;

                return (
                  <div
                    key={order.id}
                    className="bg-white rounded-lg shadow hover:shadow-md transition-shadow"
                  >
                    {/* Order Header */}
                    <div className="px-6 py-4 border-b bg-gray-50 flex flex-wrap items-center justify-between gap-4">
                      <div className="flex flex-wrap items-center gap-4">
                        <div>
                          <p className="text-sm text-gray-500">Order Number</p>
                          <p className="font-bold text-gray-900">
                            {order.orderNumber}
                          </p>
                        </div>
                        <div className="flex items-center gap-2 text-sm text-gray-600">
                          <Calendar className="w-4 h-4" />
                          {format(new Date(order.createdAt), "MMM dd, yyyy")}
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <span
                          className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium border ${statusInfo.color}`}
                        >
                          <StatusIcon className="w-4 h-4" />
                          {statusInfo.label}
                        </span>
                      </div>
                    </div>

                    {/* Order Items Preview */}
                    <div className="px-6 py-4">
                      <div className="space-y-3">
                        {order.items.slice(0, 3).map((item) => (
                          <div key={item.id} className="flex gap-3">
                            <div className="flex-shrink-0 w-16 h-16 bg-gray-100 rounded overflow-hidden">
                              <img
                                src={
                                  item.variantImageUrl ||
                                  "https://via.placeholder.com/150"
                                }
                                alt={item.productName}
                                className="w-full h-full object-cover"
                              />
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="font-medium text-gray-900 line-clamp-1">
                                {item.productName}
                              </p>
                              <p className="text-sm text-gray-500">
                                {item.variantName}
                              </p>
                              <p className="text-sm text-gray-600">
                                ${item.price.toFixed(2)} × {item.quantity}
                              </p>
                            </div>
                            <div className="text-sm font-medium text-gray-900">
                              ${item.subtotal.toFixed(2)}
                            </div>
                          </div>
                        ))}
                        {order.items.length > 3 && (
                          <p className="text-sm text-gray-500 text-center">
                            + {order.items.length - 3} more item
                            {order.items.length - 3 > 1 ? "s" : ""}
                          </p>
                        )}
                      </div>
                    </div>

                    {/* Order Footer */}
                    <div className="px-6 py-4 border-t bg-gray-50 flex flex-wrap items-center justify-between gap-4">
                      <div className="flex items-center gap-2">
                        <DollarSign className="w-5 h-5 text-gray-500" />
                        <div>
                          <p className="text-sm text-gray-500">Total Amount</p>
                          <p className="text-xl font-bold text-gray-900">
                            ${order.totalAmount.toFixed(2)}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <button
                          onClick={() => handleViewOrder(order.id)}
                          className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
                        >
                          <Eye className="w-4 h-4" />
                          View Details
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-6 flex items-center justify-center gap-2">
                <button
                  onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  disabled={currentPage === 0}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                >
                  Previous
                </button>

                <div className="flex items-center gap-1">
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    let pageNum;
                    if (totalPages <= 5) {
                      pageNum = i;
                    } else if (currentPage < 3) {
                      pageNum = i;
                    } else if (currentPage > totalPages - 3) {
                      pageNum = totalPages - 5 + i;
                    } else {
                      pageNum = currentPage - 2 + i;
                    }

                    return (
                      <button
                        key={pageNum}
                        onClick={() => setCurrentPage(pageNum)}
                        className={`w-10 h-10 rounded-lg font-medium ${
                          currentPage === pageNum
                            ? "bg-blue-600 text-white"
                            : "border border-gray-300 hover:bg-gray-50"
                        }`}
                      >
                        {pageNum + 1}
                      </button>
                    );
                  })}
                </div>

                <button
                  onClick={() =>
                    setCurrentPage((p) => Math.min(totalPages - 1, p + 1))
                  }
                  disabled={currentPage === totalPages - 1}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};
