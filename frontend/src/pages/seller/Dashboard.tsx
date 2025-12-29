import { Link } from "react-router-dom";
import {
  Package,
  DollarSign,
  ShoppingBag,
  Star,
  TrendingUp,
  AlertCircle,
} from "lucide-react";

export const SellerDashboard = () => {
  // TODO: Replace with actual API calls when backend endpoints are ready
  const stats = {
    totalProducts: 0,
    pendingProducts: 0,
    approvedProducts: 0,
    rejectedProducts: 0,
    totalOrders: 0,
    totalRevenue: 0,
    averageRating: 0,
    totalReviews: 0,
  };

  const statCards = [
    {
      title: "Total Products",
      value: stats.totalProducts,
      icon: Package,
      color: "text-blue-600",
      bgColor: "bg-blue-100",
      link: "/seller/products",
    },
    {
      title: "Pending Approval",
      value: stats.pendingProducts,
      icon: AlertCircle,
      color: "text-yellow-600",
      bgColor: "bg-yellow-100",
      link: "/seller/products?status=PENDING",
      urgent: true,
    },
    {
      title: "Total Revenue",
      value: `$${stats.totalRevenue.toFixed(2)}`,
      icon: DollarSign,
      color: "text-green-600",
      bgColor: "bg-green-100",
    },
    {
      title: "Average Rating",
      value: stats.averageRating.toFixed(1),
      icon: Star,
      color: "text-purple-600",
      bgColor: "bg-purple-100",
      subtitle: `${stats.totalReviews} reviews`,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Seller Dashboard</h1>
        <p className="text-gray-600 mt-1">
          Manage your products and track your sales performance
        </p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat) => {
          const Icon = stat.icon;
          const content = (
            <div className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-600">
                    {stat.title}
                  </p>
                  <p className="text-3xl font-bold text-gray-900 mt-2">
                    {stat.value}
                  </p>
                  {stat.subtitle && (
                    <p className="text-xs text-gray-500 mt-1">
                      {stat.subtitle}
                    </p>
                  )}
                  {stat.urgent && (
                    <p className="text-xs text-yellow-600 mt-1">
                      Awaiting review
                    </p>
                  )}
                </div>
                <div className={`${stat.bgColor} p-3 rounded-lg`}>
                  <Icon className={`w-6 h-6 ${stat.color}`} />
                </div>
              </div>
            </div>
          );

          return stat.link ? (
            <Link key={stat.title} to={stat.link}>
              {content}
            </Link>
          ) : (
            <div key={stat.title}>{content}</div>
          );
        })}
      </div>

      {/* Product Status Overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Link
          to="/seller/products?status=APPROVED"
          className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">
                Approved Products
              </p>
              <p className="text-2xl font-bold text-green-600 mt-2">
                {stats.approvedProducts}
              </p>
            </div>
            <div className="bg-green-100 p-3 rounded-lg">
              <ShoppingBag className="w-6 h-6 text-green-600" />
            </div>
          </div>
        </Link>

        <Link
          to="/seller/products?status=PENDING"
          className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">
                Pending Approval
              </p>
              <p className="text-2xl font-bold text-yellow-600 mt-2">
                {stats.pendingProducts}
              </p>
            </div>
            <div className="bg-yellow-100 p-3 rounded-lg">
              <AlertCircle className="w-6 h-6 text-yellow-600" />
            </div>
          </div>
        </Link>

        <Link
          to="/seller/products?status=REJECTED"
          className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">
                Rejected Products
              </p>
              <p className="text-2xl font-bold text-red-600 mt-2">
                {stats.rejectedProducts}
              </p>
            </div>
            <div className="bg-red-100 p-3 rounded-lg">
              <Package className="w-6 h-6 text-red-600" />
            </div>
          </div>
        </Link>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link
            to="/seller/products/new"
            className="flex items-center gap-3 p-4 border-2 border-dashed border-blue-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors"
          >
            <div className="bg-blue-100 p-2 rounded-lg">
              <Package className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <p className="font-semibold text-gray-900">Add New Product</p>
              <p className="text-xs text-gray-500">List a new item for sale</p>
            </div>
          </Link>

          <Link
            to="/seller/products"
            className="flex items-center gap-3 p-4 border-2 border-gray-200 rounded-lg hover:border-gray-300 hover:bg-gray-50 transition-colors"
          >
            <div className="bg-gray-100 p-2 rounded-lg">
              <ShoppingBag className="w-5 h-5 text-gray-600" />
            </div>
            <div>
              <p className="font-semibold text-gray-900">Manage Products</p>
              <p className="text-xs text-gray-500">Edit your listings</p>
            </div>
          </Link>

          <Link
            to="/seller/orders"
            className="flex items-center gap-3 p-4 border-2 border-gray-200 rounded-lg hover:border-gray-300 hover:bg-gray-50 transition-colors"
          >
            <div className="bg-gray-100 p-2 rounded-lg">
              <TrendingUp className="w-5 h-5 text-gray-600" />
            </div>
            <div>
              <p className="font-semibold text-gray-900">View Orders</p>
              <p className="text-xs text-gray-500">Track your sales</p>
            </div>
          </Link>
        </div>
      </div>

      {/* Tips for Sellers */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-blue-900 mb-2">
          Tips for Success
        </h3>
        <ul className="space-y-2 text-sm text-blue-800">
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">•</span>
            <span>
              Add clear, high-quality images to your products to increase sales
            </span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">•</span>
            <span>
              Write detailed descriptions including specifications and features
            </span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">•</span>
            <span>Respond to customer reviews promptly to build trust</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-600 mt-0.5">•</span>
            <span>Keep your inventory updated to avoid overselling</span>
          </li>
        </ul>
      </div>
    </div>
  );
};
