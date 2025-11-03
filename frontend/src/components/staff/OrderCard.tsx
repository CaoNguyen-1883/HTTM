import { Order } from "../../lib/types";
import {
  OrderStatusBadge,
  PaymentStatusBadge,
} from "./OrderStatusBadge";
import { Package, User, MapPin, Calendar } from "lucide-react";

interface OrderCardProps {
  order: Order;
  onViewDetails: (order: Order) => void;
}

export const OrderCard = ({ order, onViewDetails }: OrderCardProps) => {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow">
      {/* Header */}
      <div className="flex items-start justify-between mb-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">
            {order.orderNumber}
          </h3>
          <div className="flex items-center gap-2 mt-1">
            <OrderStatusBadge status={order.orderStatus} />
            <PaymentStatusBadge status={order.paymentStatus} />
          </div>
        </div>
        <div className="text-right">
          <div className="text-2xl font-bold text-gray-900">
            ${order.totalAmount.toFixed(2)}
          </div>
          <div className="text-xs text-gray-500 mt-1">
            {order.paymentMethod}
          </div>
        </div>
      </div>

      {/* Customer Info */}
      <div className="space-y-2 mb-4">
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <User className="w-4 h-4" />
          <span className="font-medium">{order.userFullName}</span>
          <span className="text-gray-400">•</span>
          <span>{order.userEmail}</span>
        </div>
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <MapPin className="w-4 h-4" />
          <span>{order.shippingAddress}</span>
        </div>
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <Calendar className="w-4 h-4" />
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

      {/* Order Items Preview */}
      <div className="border-t border-gray-200 pt-4 mb-4">
        <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
          <Package className="w-4 h-4" />
          <span className="font-medium">{order.items.length} items</span>
        </div>
        <div className="flex -space-x-2">
          {order.items.slice(0, 3).map((item, index) => (
            item.variantImageUrl && (
              <img
                key={item.id}
                src={item.variantImageUrl}
                alt={item.productName}
                className="w-10 h-10 rounded-full border-2 border-white object-cover"
                style={{ zIndex: 3 - index }}
              />
            )
          ))}
          {order.items.length > 3 && (
            <div className="w-10 h-10 rounded-full border-2 border-white bg-gray-200 flex items-center justify-center text-xs font-medium text-gray-600">
              +{order.items.length - 3}
            </div>
          )}
        </div>
      </div>

      {/* Action Button */}
      <button
        onClick={() => onViewDetails(order)}
        className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium text-sm"
      >
        View Details
      </button>
    </div>
  );
};
