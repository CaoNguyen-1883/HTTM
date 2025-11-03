import { useOrderStats, useOrders } from "../../lib/hooks";
import { OrderStatusBadge, PaymentStatusBadge } from "../../components/staff/OrderStatusBadge";
import { Link } from "react-router-dom";
import { Package, TrendingUp, CheckCircle, Truck } from "lucide-react";

export const StaffDashboard = () => {
  const { data: stats, isLoading: statsLoading } = useOrderStats();
  const { data: recentOrders, isLoading: ordersLoading } = useOrders({
    page: 0,
    size: 5,
    sort: "createdAt,desc",
  });

  if (statsLoading || ordersLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading dashboard...</div>
      </div>
    );
  }

  const statCards = [
    {
      title: "Pending Orders",
      value: stats?.pending || 0,
      icon: Package,
      color: "text-yellow-600",
      bgColor: "bg-yellow-100",
    },
    {
      title: "Confirmed Orders",
      value: stats?.confirmed || 0,
      icon: CheckCircle,
      color: "text-blue-600",
      bgColor: "bg-blue-100",
    },
    {
      title: "Shipped Orders",
      value: stats?.shipped || 0,
      icon: Truck,
      color: "text-indigo-600",
      bgColor: "bg-indigo-100",
    },
    {
      title: "Delivered Orders",
      value: stats?.delivered || 0,
      icon: TrendingUp,
      color: "text-green-600",
      bgColor: "bg-green-100",
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Staff Dashboard</h1>
        <p className="text-gray-600 mt-1">Manage and track orders</p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.title}
              className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">
                    {stat.title}
                  </p>
                  <p className="text-3xl font-bold text-gray-900 mt-2">
                    {stat.value}
                  </p>
                </div>
                <div className={`${stat.bgColor} p-3 rounded-lg`}>
                  <Icon className={`w-6 h-6 ${stat.color}`} />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Recent Orders */}
      <div className="bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <h2 className="text-xl font-bold text-gray-900">Recent Orders</h2>
          <Link
            to="/staff/orders"
            className="text-sm text-blue-600 hover:text-blue-700 font-medium"
          >
            View All
          </Link>
        </div>
        <div className="divide-y divide-gray-200">
          {recentOrders?.content.length === 0 ? (
            <div className="px-6 py-8 text-center text-gray-500">
              No orders found
            </div>
          ) : (
            recentOrders?.content.map((order) => (
              <div
                key={order.id}
                className="px-6 py-4 hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3">
                      <Link
                        to={`/staff/orders?orderNumber=${order.orderNumber}`}
                        className="text-sm font-semibold text-blue-600 hover:text-blue-700"
                      >
                        {order.orderNumber}
                      </Link>
                      <OrderStatusBadge status={order.orderStatus} />
                      <PaymentStatusBadge status={order.paymentStatus} />
                    </div>
                    <div className="mt-2 text-sm text-gray-600">
                      <span className="font-medium">{order.userFullName}</span>
                      <span className="mx-2">•</span>
                      <span>{order.items.length} items</span>
                      <span className="mx-2">•</span>
                      <span>
                        {new Date(order.createdAt).toLocaleDateString("en-US", {
                          year: "numeric",
                          month: "short",
                          day: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </span>
                    </div>
                  </div>
                  <div className="text-right ml-4">
                    <div className="text-lg font-bold text-gray-900">
                      ${order.totalAmount.toFixed(2)}
                    </div>
                    <div className="text-xs text-gray-500">
                      {order.paymentMethod}
                    </div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};