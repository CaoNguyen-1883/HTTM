import { useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { useOrder, useCancelOrder } from "../../lib/hooks";
import { OrderStatus, PaymentStatus, PaymentMethod } from "../../lib/types";
import {
  ArrowLeft,
  Package,
  Truck,
  CheckCircle,
  XCircle,
  Clock,
  MapPin,
  Phone,
  User,
  CreditCard,
  Calendar,
  FileText,
  AlertCircle,
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
    icon: CheckCircle,
  },
  [OrderStatus.CANCELLED]: {
    label: "Cancelled",
    color: "bg-red-100 text-red-800 border-red-200",
    icon: XCircle,
  },
};

const paymentStatusConfig = {
  [PaymentStatus.PENDING]: { label: "Pending", color: "text-yellow-600" },
  [PaymentStatus.PAID]: { label: "Paid", color: "text-green-600" },
  [PaymentStatus.FAILED]: { label: "Failed", color: "text-red-600" },
  [PaymentStatus.REFUNDED]: { label: "Refunded", color: "text-blue-600" },
};

const paymentMethodConfig = {
  [PaymentMethod.COD]: "Cash on Delivery",
  [PaymentMethod.BANK_TRANSFER]: "Bank Transfer",
  [PaymentMethod.CREDIT_CARD]: "Credit/Debit Card",
  [PaymentMethod.E_WALLET]: "E-Wallet",
};

export const CustomerOrderDetailPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams<{ id: string }>();
  const orderId = id || "0";

  const { data: order, isLoading } = useOrder(orderId);
  const cancelOrderMutation = useCancelOrder();



  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  const fromCheckout = location.state?.fromCheckout;

  const handleCancelOrder = async () => {
    if (!cancelReason.trim()) {
      alert("Please provide a reason for cancellation");
      return;
    }

    if (!confirm("Are you sure you want to cancel this order?")) {
      return;
    }

    try {
      await cancelOrderMutation.mutateAsync({
        orderId,
        reason: cancelReason,
      });
      setShowCancelDialog(false);
      alert("Order cancelled successfully");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to cancel order");
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading order details...</div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            Order not found
          </h2>
          <button
            onClick={() => navigate("/customer/orders")}
            className="text-blue-600 hover:text-blue-700 font-medium"
          >
            Back to Orders
          </button>
        </div>
      </div>
    );
  }

  const statusInfo = statusConfig[order.orderStatus];
  const StatusIcon = statusInfo.icon;
  const canCancel = order.orderStatus === OrderStatus.PENDING;

  // Order timeline
  const timeline = [
    {
      status: "Order Placed",
      date: order.createdAt,
      completed: true,
      icon: Package,
    },
    {
      status: "Confirmed",
      date: order.confirmedAt,
      completed: !!order.confirmedAt || order.orderStatus !== OrderStatus.PENDING,
      icon: CheckCircle,
    },
    {
      status: "Shipped",
      date: order.shippedAt,
      completed: !!order.shippedAt,
      icon: Truck,
    },
    {
      status: "Delivered",
      date: order.deliveredAt,
      completed: !!order.deliveredAt,
      icon: CheckCircle,
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <button
            onClick={() => navigate("/customer/orders")}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
          >
            <ArrowLeft className="w-5 h-5" />
            Back to Orders
          </button>

          {fromCheckout && (
            <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-3">
              <CheckCircle className="w-6 h-6 text-green-600 flex-shrink-0 mt-0.5" />
              <div>
                <h3 className="font-semibold text-green-900">
                  Order placed successfully!
                </h3>
                <p className="text-sm text-green-700 mt-1">
                  Thank you for your order. We'll send you a confirmation email
                  shortly.
                </p>
              </div>
            </div>
          )}

          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
                <Package className="w-8 h-8" />
                Order Details
              </h1>
              <p className="text-gray-600 mt-1">
                Order #{order.orderNumber}
              </p>
            </div>
            <span
              className={`inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium border ${statusInfo.color}`}
            >
              <StatusIcon className="w-5 h-5" />
              {statusInfo.label}
            </span>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Order Timeline */}
            {order.orderStatus !== OrderStatus.CANCELLED && (
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6">
                  Order Timeline
                </h2>
                <div className="relative">
                  {timeline.map((item, index) => {
                    const TimelineIcon = item.icon;
                    const isLast = index === timeline.length - 1;

                    return (
                      <div key={index} className="relative pb-8">
                        {!isLast && (
                          <div
                            className={`absolute left-5 top-10 w-0.5 h-full ${
                              item.completed ? "bg-blue-600" : "bg-gray-200"
                            }`}
                          />
                        )}
                        <div className="flex items-start gap-4">
                          <div
                            className={`flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center ${
                              item.completed
                                ? "bg-blue-600 text-white"
                                : "bg-gray-200 text-gray-400"
                            }`}
                          >
                            <TimelineIcon className="w-5 h-5" />
                          </div>
                          <div className="flex-1 pt-1">
                            <p
                              className={`font-medium ${
                                item.completed
                                  ? "text-gray-900"
                                  : "text-gray-400"
                              }`}
                            >
                              {item.status}
                            </p>
                            {item.date && (
                              <p className="text-sm text-gray-500 mt-1">
                                {format(new Date(item.date), "MMM dd, yyyy 'at' h:mm a")}
                              </p>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Order Items */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4">
                Order Items ({order.items.length})
              </h2>
              <div className="space-y-4">
                {order.items.map((item) => (
                  <div
                    key={item.id}
                    className="flex gap-4 pb-4 border-b last:border-b-0"
                  >
                    <div className="flex-shrink-0 w-20 h-20 bg-gray-100 rounded overflow-hidden">
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
                      <h3 className="font-semibold text-gray-900 line-clamp-2">
                        {item.productName}
                      </h3>
                      <p className="text-sm text-gray-500 mt-1">
                        {item.variantName}
                      </p>
                      <p className="text-sm text-gray-600 mt-1">
                        SKU: {item.variantSku}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium text-gray-900">
                        ${item.price.toFixed(2)}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">Qty: {item.quantity}</p>
                      <p className="font-bold text-gray-900 mt-1">
                        ${item.subtotal.toFixed(2)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Shipping Information */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Truck className="w-6 h-6" />
                Shipping Information
              </h2>
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <User className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-sm text-gray-500">Recipient</p>
                    <p className="font-medium text-gray-900">
                      {order.shippingRecipient}
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <Phone className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-sm text-gray-500">Phone</p>
                    <p className="font-medium text-gray-900">
                      {order.shippingPhone}
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <MapPin className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-sm text-gray-500">Delivery Address</p>
                    <p className="font-medium text-gray-900">
                      {order.shippingAddress}
                    </p>
                  </div>
                </div>
                {order.notes && (
                  <div className="flex items-start gap-3">
                    <FileText className="w-5 h-5 text-gray-400 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-500">Order Note</p>
                      <p className="font-medium text-gray-900">{order.notes}</p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="lg:col-span-1 space-y-6">
            {/* Order Summary */}
            <div className="bg-white rounded-lg shadow p-6 sticky top-4">
              <h2 className="text-xl font-bold text-gray-900 mb-4">
                Order Summary
              </h2>

              <div className="space-y-3 mb-4">
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Calendar className="w-4 h-4" />
                  <span>
                    {format(new Date(order.createdAt), "MMM dd, yyyy")}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <CreditCard className="w-4 h-4 text-gray-600" />
                  <span className="text-gray-600">
                    {paymentMethodConfig[order.paymentMethod]}
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-600">Payment Status:</span>
                  <span
                    className={`font-medium ${
                      paymentStatusConfig[order.paymentStatus].color
                    }`}
                  >
                    {paymentStatusConfig[order.paymentStatus].label}
                  </span>
                </div>
              </div>

              <div className="border-t pt-4 space-y-3">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal</span>
                  <span>${order.subtotal.toFixed(2)}</span>
                </div>

                <div className="flex justify-between text-gray-600">
                  <span>Shipping Fee</span>
                  <span>${order.shippingFee.toFixed(2)}</span>
                </div>

                {order.discount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Discount</span>
                    <span>-${order.discount.toFixed(2)}</span>
                  </div>
                )}

                <div className="border-t pt-3">
                  <div className="flex justify-between text-xl font-bold text-gray-900">
                    <span>Total</span>
                    <span>${order.totalAmount.toFixed(2)}</span>
                  </div>
                </div>
              </div>

              {/* Actions */}
              {canCancel && (
                <div className="mt-6 pt-6 border-t">
                  <button
                    onClick={() => setShowCancelDialog(true)}
                    className="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium"
                  >
                    Cancel Order
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Cancel Order Dialog */}
      {showCancelDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-900 mb-4">
              Cancel Order
            </h3>
            <p className="text-gray-600 mb-4">
              Please provide a reason for cancelling this order:
            </p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              rows={4}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter cancellation reason..."
            />
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => {
                  setShowCancelDialog(false);
                  setCancelReason("");
                }}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
              >
                Keep Order
              </button>
              <button
                onClick={handleCancelOrder}
                disabled={cancelOrderMutation.isPending}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium disabled:opacity-50"
              >
                {cancelOrderMutation.isPending ? "Cancelling..." : "Cancel Order"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
