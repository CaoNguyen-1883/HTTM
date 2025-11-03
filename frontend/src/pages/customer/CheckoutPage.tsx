// CheckoutPage.tsx - Updated component
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../../lib/hooks";
import { useCreateOrder } from "../../lib/hooks";
import { PaymentMethod } from "../../lib/types";
import {
  CreditCard,
  Wallet,
  Banknote,
  TruckIcon,
  ShoppingCart,
  CheckCircle,
  AlertCircle,
  ArrowLeft,
} from "lucide-react";

export const CheckoutPage = () => {
  const navigate = useNavigate();
  const { data: cart, isLoading: isLoadingCart } = useCart();
  const createOrderMutation = useCreateOrder();

  // Form state - Updated to match backend
  const [shippingRecipient, setShippingRecipient] = useState("");
  const [shippingPhone, setShippingPhone] = useState("");
  const [shippingAddress, setShippingAddress] = useState("");
  const [shippingCity, setShippingCity] = useState("");
  const [shippingDistrict, setShippingDistrict] = useState("");
  const [shippingWard, setShippingWard] = useState("");
  const [billingAddress, setBillingAddress] = useState("");
  const [billingCity, setBillingCity] = useState("");
  const [billingDistrict, setBillingDistrict] = useState("");
  const [billingWard, setBillingWard] = useState("");
  const [notes, setNotes] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(
    PaymentMethod.COD
  );
  const [useShippingForBilling, setUseShippingForBilling] = useState(true);

  // Form validation state
  const [errors, setErrors] = useState<{
    shippingRecipient?: string;
    shippingPhone?: string;
    shippingAddress?: string;
    shippingCity?: string;
    billingCity?: string;
  }>({});

  const validateForm = () => {
    const newErrors: typeof errors = {};

    if (!shippingRecipient.trim()) {
      newErrors.shippingRecipient = "Recipient name is required";
    } else if (shippingRecipient.length > 100) {
      newErrors.shippingRecipient = "Recipient name must not exceed 100 characters";
    }

    if (!shippingPhone.trim()) {
      newErrors.shippingPhone = "Phone number is required";
    } else if (!/^[0-9]{10,11}$/.test(shippingPhone.replace(/\s/g, ""))) {
      newErrors.shippingPhone = "Phone must be 10-11 digits";
    }

    if (!shippingAddress.trim()) {
      newErrors.shippingAddress = "Shipping address is required";
    } else if (shippingAddress.length > 200) {
      newErrors.shippingAddress = "Shipping address must not exceed 200 characters";
    }

    if (!shippingCity.trim()) {
      newErrors.shippingCity = "Shipping city is required";
    } else if (shippingCity.length > 100) {
      newErrors.shippingCity = "City must not exceed 100 characters";
    }

    // Validate billing city if provided and not using shipping address
    if (!useShippingForBilling && billingCity && billingCity.length > 100) {
      newErrors.billingCity = "City must not exceed 100 characters";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handlePlaceOrder = async () => {
    if (!validateForm()) {
      return;
    }

    if (!cart || cart.items.length === 0) {
      alert("Your cart is empty");
      return;
    }

    try {
      const orderData = {
        shippingRecipient: shippingRecipient.trim(),
        shippingPhone: shippingPhone.trim(),
        shippingAddress: shippingAddress.trim(),
        shippingCity: shippingCity.trim(),
        shippingDistrict: shippingDistrict.trim() || undefined,
        shippingWard: shippingWard.trim() || undefined,
        paymentMethod,
        notes: notes.trim() || undefined,
        // Billing address - use shipping address if checkbox is checked or billing fields are empty
        billingAddress: useShippingForBilling ? undefined : billingAddress.trim() || undefined,
        billingCity: useShippingForBilling ? undefined : billingCity.trim() || undefined,
        billingDistrict: useShippingForBilling ? undefined : billingDistrict.trim() || undefined,
        billingWard: useShippingForBilling ? undefined : billingWard.trim() || undefined,
      };

      const order = await createOrderMutation.mutateAsync(orderData);

      navigate(`/customer/orders/${order.id}`, {
        state: { fromCheckout: true },
      });
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to place order. Please try again.");
    }
  };

  if (isLoadingCart) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading checkout...</div>
      </div>
    );
  }

  const isEmpty = !cart || cart.items.length === 0;

  if (isEmpty) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <AlertCircle className="w-24 h-24 mx-auto text-gray-400 mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Your cart is empty
            </h2>
            <p className="text-gray-600 mb-6">
              Please add items to your cart before checking out
            </p>
            <button
              onClick={() => navigate("/products")}
              className="inline-flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
            >
              <ShoppingCart className="w-5 h-5" />
              Continue Shopping
            </button>
          </div>
        </div>
      </div>
    );
  }

  const shippingFee = cart.totalPrice >= 50 ? 0 : 5.99;
  const finalTotal = cart.totalPrice + shippingFee;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <button
            onClick={() => navigate("/customer/cart")}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
          >
            <ArrowLeft className="w-5 h-5" />
            Back to Cart
          </button>
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
            <CheckCircle className="w-8 h-8" />
            Checkout
          </h1>
          <p className="text-gray-600 mt-1">Complete your order</p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Main Form */}
          <div className="flex-1 space-y-6">
            {/* Shipping Information */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                <TruckIcon className="w-6 h-6" />
                Shipping Information
              </h2>

              <div className="space-y-4">
                {/* Recipient Name */}
                <div>
                  <label
                    htmlFor="shippingRecipient"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Recipient Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    id="shippingRecipient"
                    value={shippingRecipient}
                    onChange={(e) => setShippingRecipient(e.target.value)}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                      errors.shippingRecipient ? "border-red-500" : "border-gray-300"
                    }`}
                    placeholder="Enter recipient's full name"
                    maxLength={100}
                  />
                  {errors.shippingRecipient && (
                    <p className="text-red-500 text-sm mt-1">
                      {errors.shippingRecipient}
                    </p>
                  )}
                </div>

                {/* Phone Number */}
                <div>
                  <label
                    htmlFor="shippingPhone"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Phone Number <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="tel"
                    id="shippingPhone"
                    value={shippingPhone}
                    onChange={(e) => setShippingPhone(e.target.value)}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                      errors.shippingPhone ? "border-red-500" : "border-gray-300"
                    }`}
                    placeholder="Enter phone number (10-11 digits)"
                    pattern="[0-9]{10,11}"
                  />
                  {errors.shippingPhone && (
                    <p className="text-red-500 text-sm mt-1">
                      {errors.shippingPhone}
                    </p>
                  )}
                </div>

                {/* Shipping Address */}
                <div>
                  <label
                    htmlFor="shippingAddress"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Shipping Address <span className="text-red-500">*</span>
                  </label>
                  <textarea
                    id="shippingAddress"
                    value={shippingAddress}
                    onChange={(e) => setShippingAddress(e.target.value)}
                    rows={2}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                      errors.shippingAddress
                        ? "border-red-500"
                        : "border-gray-300"
                    }`}
                    placeholder="Street address, building, apartment number"
                    maxLength={200}
                  />
                  {errors.shippingAddress && (
                    <p className="text-red-500 text-sm mt-1">
                      {errors.shippingAddress}
                    </p>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {/* City */}
                  <div>
                    <label
                      htmlFor="shippingCity"
                      className="block text-sm font-medium text-gray-700 mb-1"
                    >
                      City <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      id="shippingCity"
                      value={shippingCity}
                      onChange={(e) => setShippingCity(e.target.value)}
                      className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                        errors.shippingCity ? "border-red-500" : "border-gray-300"
                      }`}
                      placeholder="City"
                      maxLength={100}
                    />
                    {errors.shippingCity && (
                      <p className="text-red-500 text-sm mt-1">
                        {errors.shippingCity}
                      </p>
                    )}
                  </div>

                  {/* District */}
                  <div>
                    <label
                      htmlFor="shippingDistrict"
                      className="block text-sm font-medium text-gray-700 mb-1"
                    >
                      District
                    </label>
                    <input
                      type="text"
                      id="shippingDistrict"
                      value={shippingDistrict}
                      onChange={(e) => setShippingDistrict(e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="District"
                      maxLength={100}
                    />
                  </div>

                  {/* Ward */}
                  <div>
                    <label
                      htmlFor="shippingWard"
                      className="block text-sm font-medium text-gray-700 mb-1"
                    >
                      Ward
                    </label>
                    <input
                      type="text"
                      id="shippingWard"
                      value={shippingWard}
                      onChange={(e) => setShippingWard(e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Ward"
                      maxLength={100}
                    />
                  </div>
                </div>

                {/* Billing Address */}
                <div className="pt-4 border-t">
                  <div className="flex items-center gap-2 mb-4">
                    <input
                      type="checkbox"
                      id="useShippingForBilling"
                      checked={useShippingForBilling}
                      onChange={(e) => setUseShippingForBilling(e.target.checked)}
                      className="w-4 h-4 text-blue-600 rounded"
                    />
                    <label
                      htmlFor="useShippingForBilling"
                      className="text-sm font-medium text-gray-700"
                    >
                      Use shipping address for billing
                    </label>
                  </div>

                  {!useShippingForBilling && (
                    <div className="space-y-4 pl-4 border-l-2 border-gray-200">
                      <h3 className="font-medium text-gray-900">Billing Address</h3>
                      
                      <div>
                        <label
                          htmlFor="billingAddress"
                          className="block text-sm font-medium text-gray-700 mb-1"
                        >
                          Billing Address
                        </label>
                        <textarea
                          id="billingAddress"
                          value={billingAddress}
                          onChange={(e) => setBillingAddress(e.target.value)}
                          rows={2}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                          placeholder="Billing street address"
                          maxLength={200}
                        />
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                          <label
                            htmlFor="billingCity"
                            className="block text-sm font-medium text-gray-700 mb-1"
                          >
                            City
                          </label>
                          <input
                            type="text"
                            id="billingCity"
                            value={billingCity}
                            onChange={(e) => setBillingCity(e.target.value)}
                            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                              errors.billingCity ? "border-red-500" : "border-gray-300"
                            }`}
                            placeholder="City"
                            maxLength={100}
                          />
                          {errors.billingCity && (
                            <p className="text-red-500 text-sm mt-1">
                              {errors.billingCity}
                            </p>
                          )}
                        </div>

                        <div>
                          <label
                            htmlFor="billingDistrict"
                            className="block text-sm font-medium text-gray-700 mb-1"
                          >
                            District
                          </label>
                          <input
                            type="text"
                            id="billingDistrict"
                            value={billingDistrict}
                            onChange={(e) => setBillingDistrict(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="District"
                            maxLength={100}
                          />
                        </div>

                        <div>
                          <label
                            htmlFor="billingWard"
                            className="block text-sm font-medium text-gray-700 mb-1"
                          >
                            Ward
                          </label>
                          <input
                            type="text"
                            id="billingWard"
                            value={billingWard}
                            onChange={(e) => setBillingWard(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="Ward"
                            maxLength={100}
                          />
                        </div>
                      </div>
                    </div>
                  )}
                </div>

                {/* Notes */}
                <div>
                  <label
                    htmlFor="notes"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Order Notes (Optional)
                  </label>
                  <textarea
                    id="notes"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    rows={2}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Any special instructions for your order"
                    maxLength={500}
                  />
                </div>
              </div>
            </div>

            {/* Payment Method - Keep the same as before */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Wallet className="w-6 h-6" />
                Payment Method
              </h2>

              <div className="space-y-3">
                {/* Cash on Delivery */}
                <label
                  className={`flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                    paymentMethod === PaymentMethod.COD
                      ? "border-blue-600 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={PaymentMethod.COD}
                    checked={paymentMethod === PaymentMethod.COD}
                    onChange={(e) =>
                      setPaymentMethod(e.target.value as PaymentMethod)
                    }
                    className="w-5 h-5 text-blue-600"
                  />
                  <TruckIcon className="w-6 h-6 text-gray-600" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">Cash on Delivery</p>
                    <p className="text-sm text-gray-500">
                      Pay when you receive your order
                    </p>
                  </div>
                </label>

                {/* Bank Transfer */}
                <label
                  className={`flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                    paymentMethod === PaymentMethod.BANK_TRANSFER
                      ? "border-blue-600 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={PaymentMethod.BANK_TRANSFER}
                    checked={paymentMethod === PaymentMethod.BANK_TRANSFER}
                    onChange={(e) =>
                      setPaymentMethod(e.target.value as PaymentMethod)
                    }
                    className="w-5 h-5 text-blue-600"
                  />
                  <Banknote className="w-6 h-6 text-gray-600" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">Bank Transfer</p>
                    <p className="text-sm text-gray-500">
                      Transfer to our bank account
                    </p>
                  </div>
                </label>

                {/* Credit Card */}
                <label
                  className={`flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                    paymentMethod === PaymentMethod.CREDIT_CARD
                      ? "border-blue-600 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={PaymentMethod.CREDIT_CARD}
                    checked={paymentMethod === PaymentMethod.CREDIT_CARD}
                    onChange={(e) =>
                      setPaymentMethod(e.target.value as PaymentMethod)
                    }
                    className="w-5 h-5 text-blue-600"
                  />
                  <CreditCard className="w-6 h-6 text-gray-600" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">
                      Credit/Debit Card
                    </p>
                    <p className="text-sm text-gray-500">
                      Visa, Mastercard, JCB, Amex
                    </p>
                  </div>
                </label>

                {/* E-Wallet */}
                <label
                  className={`flex items-center gap-3 p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                    paymentMethod === PaymentMethod.E_WALLET
                      ? "border-blue-600 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={PaymentMethod.E_WALLET}
                    checked={paymentMethod === PaymentMethod.E_WALLET}
                    onChange={(e) =>
                      setPaymentMethod(e.target.value as PaymentMethod)
                    }
                    className="w-5 h-5 text-blue-600"
                  />
                  <Wallet className="w-6 h-6 text-gray-600" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">E-Wallet</p>
                    <p className="text-sm text-gray-500">
                      Momo, ZaloPay, VNPay
                    </p>
                  </div>
                </label>
              </div>
            </div>
          </div>

          {/* Order Summary Sidebar - Keep the same as before */}
          <div className="lg:w-96">
            <div className="bg-white rounded-lg shadow p-6 sticky top-4">
              <h2 className="text-xl font-bold text-gray-900 mb-4">
                Order Summary
              </h2>

              {/* Cart Items */}
              <div className="max-h-64 overflow-y-auto mb-4 -mx-2 px-2">
                <div className="space-y-3">
                  {cart.items.map((item) => (
                    <div key={item.id} className="flex gap-3">
                      <div className="flex-shrink-0 w-16 h-16 bg-gray-100 rounded overflow-hidden">
                        <img
                          src={
                            item.primaryImage ||
                            "https://via.placeholder.com/150"
                          }
                          alt={item.productName}
                          className="w-full h-full object-cover"
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 line-clamp-1">
                          {item.productName}
                        </p>
                        <p className="text-xs text-gray-500">{item.variantName}</p>
                        <p className="text-sm text-gray-600">
                          ${item.priceAtAdd.toFixed(2)} × {item.quantity}
                        </p>
                      </div>
                      <div className="text-sm font-medium text-gray-900">
                        ${item.subtotal.toFixed(2)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Pricing Summary */}
              <div className="border-t pt-4 space-y-3">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal ({cart.totalItems} items)</span>
                  <span>${cart.totalPrice.toFixed(2)}</span>
                </div>

                {cart.discount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Discount</span>
                    <span>-${cart.discount.toFixed(2)}</span>
                  </div>
                )}

                <div className="flex justify-between text-gray-600">
                  <span>Shipping Fee</span>
                  <span>
                    {shippingFee === 0 ? (
                      <span className="text-green-600 font-medium">FREE</span>
                    ) : (
                      `$${shippingFee.toFixed(2)}`
                    )}
                  </span>
                </div>

                {shippingFee === 0 && (
                  <p className="text-sm text-green-600 flex items-center gap-1">
                    <CheckCircle className="w-4 h-4" />
                    Free shipping on orders over $50
                  </p>
                )}

                <div className="border-t pt-3">
                  <div className="flex justify-between text-xl font-bold text-gray-900">
                    <span>Total</span>
                    <span>${finalTotal.toFixed(2)}</span>
                  </div>
                </div>
              </div>

              {/* Place Order Button */}
              <button
                onClick={handlePlaceOrder}
                disabled={createOrderMutation.isPending}
                className="w-full mt-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {createOrderMutation.isPending ? (
                  <span className="flex items-center justify-center gap-2">
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    Processing...
                  </span>
                ) : (
                  "Place Order"
                )}
              </button>

              {/* Trust Badges */}
              <div className="mt-6 pt-6 border-t">
                <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
                  <CheckCircle className="w-5 h-5 text-green-600" />
                  Secure checkout
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
                  <CheckCircle className="w-5 h-5 text-green-600" />
                  30-day return policy
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <CheckCircle className="w-5 h-5 text-green-600" />
                  24/7 customer support
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};