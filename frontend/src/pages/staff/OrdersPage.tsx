import { useState, useEffect } from "react";
import { useOrders, useOrderByNumber } from "../../lib/hooks";
import { OrderCard } from "../../components/staff/OrderCard";
import { OrderDetailModal } from "../../components/staff/OrderDetailModal";
import { Order, OrderStatus, PaymentStatus } from "../../lib/types";
import { Search, Filter, ChevronLeft, ChevronRight } from "lucide-react";
import { useSearchParams } from "react-router-dom";

export const StaffOrdersPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

  // Get state from URL params or use defaults
  const currentPage = parseInt(searchParams.get("page") || "0");
  const statusFilter = (searchParams.get("status") || "") as OrderStatus | "";
  const paymentStatusFilter = (searchParams.get("paymentStatus") || "") as PaymentStatus | "";

  // Local state for order number search input
  const [orderNumberSearch, setOrderNumberSearch] = useState(
    searchParams.get("orderNumber") || ""
  );

  // Search by order number if provided in URL params
  const { data: orderByNumber, isLoading: isLoadingByNumber } =
    useOrderByNumber(searchParams.get("orderNumber") || "");

  // Get orders with filters
  const { data: ordersData, isLoading: isLoadingOrders } = useOrders({
    page: currentPage,
    size: 12,
    orderStatus: statusFilter || undefined,
    paymentStatus: paymentStatusFilter || undefined,
    sort: "createdAt,desc",
  });

  // If order number search is active and we have results, show that order
  useEffect(() => {
    if (orderByNumber && searchParams.get("orderNumber")) {
      setSelectedOrder(orderByNumber);
    }
  }, [orderByNumber, searchParams]);

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
    if (orderNumberSearch.trim()) {
      setSearchParams({ orderNumber: orderNumberSearch.trim() });
    } else {
      setSearchParams({});
    }
  };

  const clearSearch = () => {
    setOrderNumberSearch("");
    setSearchParams({});
  };

  const handlePageChange = (newPage: number) => {
    updateParams({ page: newPage });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleStatusChange = (status: OrderStatus | "") => {
    updateParams({ status: status || undefined, page: 0 });
  };

  const handlePaymentStatusChange = (paymentStatus: PaymentStatus | "") => {
    updateParams({ paymentStatus: paymentStatus || undefined, page: 0 });
  };

  const clearFilters = () => {
    updateParams({ status: undefined, paymentStatus: undefined, page: 0 });
  };

  const isLoading = isLoadingOrders || isLoadingByNumber;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Orders Management</h1>
        <p className="text-gray-600 mt-1">
          View and manage all customer orders
        </p>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow p-6">
        <form onSubmit={handleSearch} className="space-y-4">
          {/* Search by Order Number */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Search by Order Number
            </label>
            <div className="flex gap-2">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  value={orderNumberSearch}
                  onChange={(e) => setOrderNumberSearch(e.target.value)}
                  placeholder="Enter order number (e.g., ORD-20250101-ABC123)"
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <button
                type="submit"
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
              >
                Search
              </button>
              {searchParams.get("orderNumber") && (
                <button
                  type="button"
                  onClick={clearSearch}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium"
                >
                  Clear
                </button>
              )}
            </div>
          </div>

          {/* Filters */}
          {!searchParams.get("orderNumber") && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Filter className="inline w-4 h-4 mr-1" />
                  Order Status
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => handleStatusChange(e.target.value as OrderStatus | "")}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">All Statuses</option>
                  {Object.values(OrderStatus).map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Filter className="inline w-4 h-4 mr-1" />
                  Payment Status
                </label>
                <select
                  value={paymentStatusFilter}
                  onChange={(e) => handlePaymentStatusChange(e.target.value as PaymentStatus | "")}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">All Payment Statuses</option>
                  {Object.values(PaymentStatus).map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )}
        </form>
      </div>

      {/* Orders Grid */}
      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading orders...</div>
        </div>
      ) : searchParams.get("orderNumber") && !orderByNumber ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <div className="text-gray-500 text-lg">
            No order found with number "{searchParams.get("orderNumber")}"
          </div>
          <button
            onClick={clearSearch}
            className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            View All Orders
          </button>
        </div>
      ) : ordersData?.content.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <div className="text-gray-500 text-lg">No orders found</div>
          {(statusFilter || paymentStatusFilter) && (
            <button
              onClick={clearFilters}
              className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Clear Filters
            </button>
          )}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {ordersData?.content.map((order) => (
              <OrderCard
                key={order.id}
                order={order}
                onViewDetails={setSelectedOrder}
              />
            ))}
          </div>

          {/* Pagination */}
          {ordersData && ordersData.totalPages > 1 && (
            <div className="flex items-center justify-between bg-white rounded-lg shadow px-6 py-4">
              <div className="text-sm text-gray-600">
                Showing page {currentPage + 1} of {ordersData.totalPages} (
                {ordersData.totalElements} total orders)
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
                  disabled={currentPage >= ordersData.totalPages - 1}
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

      {/* Order Detail Modal */}
      {selectedOrder && (
        <OrderDetailModal
          order={selectedOrder}
          onClose={() => setSelectedOrder(null)}
        />
      )}
    </div>
  );
};