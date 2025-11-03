import { useNavigate } from "react-router-dom";
import { useCart, useUpdateCartItem, useRemoveCartItem, useClearCart } from "../../lib/hooks";
import { ShoppingCart, Minus, Plus, ArrowRight, X, ShoppingBag } from "lucide-react";
import { useState } from "react";

export const CartPage = () => {
  const navigate = useNavigate();
  const { data: cart, isLoading } = useCart();
  const updateItemMutation = useUpdateCartItem();
  const removeItemMutation = useRemoveCartItem();
  const clearCartMutation = useClearCart();
  // console.log(cart)

  const [updatingItems, setUpdatingItems] = useState<Set<string>>(new Set());

  const handleUpdateQuantity = async (itemId: string, newQuantity: number) => {
    if (newQuantity < 1) return;

    setUpdatingItems(prev => new Set(prev).add(itemId));

    try {
      await updateItemMutation.mutateAsync({
        itemId,
        data: { quantity: newQuantity },
      });
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to update quantity");
    } finally {
      setUpdatingItems(prev => {
        const next = new Set(prev);
        next.delete(itemId);
        return next;
      });
    }
  };

  const handleRemoveItem = async (itemId: string, productName: string) => {
    if (!confirm(`Remove "${productName}" from cart?`)) return;

    try {
      await removeItemMutation.mutateAsync(itemId);
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to remove item");
    }
  };

  const handleClearCart = async () => {
    if (!confirm("Are you sure you want to clear your entire cart?")) return;

    try {
      await clearCartMutation.mutateAsync();
      alert("Cart cleared successfully");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to clear cart");
    }
  };

  const handleCheckout = () => {
    if (!cart || cart.items.length === 0) return;
    navigate("/customer/checkout");
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading cart...</div>
      </div>
    );
  }

  const isEmpty = !cart || cart.items.length === 0;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
            <ShoppingCart className="w-8 h-8" />
            Shopping Cart
          </h1>
          <p className="text-gray-600 mt-1">
            {isEmpty ? "Your cart is empty" : `${cart.items.length} item${cart.items.length > 1 ? 's' : ''} in cart`}
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {isEmpty ? (
          /* Empty Cart State */
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <div className="text-gray-400 mb-4">
              <ShoppingBag className="w-24 h-24 mx-auto" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Your cart is empty</h2>
            <p className="text-gray-600 mb-6">
              Looks like you haven't added any items to your cart yet
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
          <div className="flex flex-col lg:flex-row gap-8">
            {/* Cart Items */}
            <div className="flex-1">
              <div className="bg-white rounded-lg shadow">
                {/* Cart Header */}
                <div className="px-6 py-4 border-b flex items-center justify-between">
                  <h2 className="text-lg font-bold text-gray-900">
                    Cart Items ({cart.items.length})
                  </h2>
                  {cart.items.length > 0 && (
                    <button
                      onClick={handleClearCart}
                      disabled={clearCartMutation.isPending}
                      className="text-sm text-red-600 hover:text-red-700 font-medium disabled:opacity-50"
                    >
                      Clear Cart
                    </button>
                  )}
                </div>

                {/* Cart Items List */}
                <div className="divide-y">
                  {cart.items.map((item) => {
                    const isUpdating = updatingItems.has(item.id);

                    return (
                      <div key={item.id} className="p-6 flex gap-4">
                        {/* Product Image */}
                        <div className="flex-shrink-0 w-24 h-24 bg-gray-100 rounded-lg overflow-hidden">
                          <img
                            src={item.primaryImage || "https://via.placeholder.com/150"}
                            alt={item.productName}
                            className="w-full h-full object-cover"
                          />
                        </div>

                        {/* Product Info */}
                        <div className="flex-1 min-w-0">
                          <h3 className="text-lg font-semibold text-gray-900 mb-1 line-clamp-2">
                            {item.productName}
                          </h3>
                          <p className="text-sm text-gray-600 mb-2">
                            {item.variantName}
                          </p>
                          <p className="text-xl font-bold text-blue-600">
                            ${item.priceAtAdd.toFixed(2)}
                          </p>
                        </div>

                        {/* Quantity Controls */}
                        <div className="flex flex-col items-end justify-between">
                          <button
                            onClick={() => handleRemoveItem(item.id, item.productName)}
                            disabled={removeItemMutation.isPending}
                            className="text-gray-400 hover:text-red-600 disabled:opacity-50"
                            title="Remove from cart"
                          >
                            <X className="w-5 h-5" />
                          </button>

                          <div className="flex items-center gap-3">
                            <div className="flex items-center border border-gray-300 rounded-lg">
                              <button
                                onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}
                                disabled={isUpdating || item.quantity <= 1}
                                className="px-3 py-2 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                <Minus className="w-4 h-4" />
                              </button>
                              <span className="px-4 py-2 min-w-[3rem] text-center font-medium">
                                {isUpdating ? "..." : item.quantity}
                              </span>
                              <button
                                onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
                                disabled={isUpdating || item.quantity >= item.availableStock}
                                className="px-3 py-2 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                <Plus className="w-4 h-4" />
                              </button>
                            </div>
                          </div>

                          <div className="text-right">
                            <p className="text-sm text-gray-500">Subtotal</p>
                            <p className="text-lg font-bold text-gray-900">
                              ${item.subtotal.toFixed(2)}
                            </p>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Continue Shopping Button */}
              <button
                onClick={() => navigate("/products")}
                className="mt-4 text-blue-600 hover:text-blue-700 font-medium flex items-center gap-2"
              >
                ← Continue Shopping
              </button>
            </div>

            {/* Order Summary */}
            <div className="lg:w-96">
              <div className="bg-white rounded-lg shadow p-6 sticky top-4">
                <h2 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>

                <div className="space-y-3 mb-4">
                  <div className="flex justify-between text-gray-600">
                    <span>Subtotal ({cart.totalItems} items)</span>
                    <span>${cart.subtotal.toFixed(2)}</span>
                  </div>

                  {cart.discount > 0 && (
                    <div className="flex justify-between text-green-600">
                      <span>Discount</span>
                      <span>-${cart.discount.toFixed(2)}</span>
                    </div>
                  )}

                  <div className="flex justify-between text-gray-600">
                    <span>Shipping</span>
                    <span className="text-gray-400">Calculated at checkout</span>
                  </div>

                  <div className="flex justify-between text-gray-600">
                    <span>Tax</span>
                    <span className="text-gray-400">Calculated at checkout</span>
                  </div>

                  <div className="border-t pt-3">
                    <div className="flex justify-between text-xl font-bold text-gray-900">
                      <span>Total</span>
                      <span>${cart.totalPrice.toFixed(2)}</span>
                    </div>
                  </div>
                </div>

                <button
                  onClick={handleCheckout}
                  className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium flex items-center justify-center gap-2"
                >
                  Proceed to Checkout
                  <ArrowRight className="w-5 h-5" />
                </button>

                <p className="text-sm text-gray-500 text-center mt-4">
                  Shipping & taxes calculated at checkout
                </p>

                {/* Trust Badges */}
                <div className="mt-6 pt-6 border-t">
                  <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
                    <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    Secure checkout
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
                    <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    Free shipping on orders over $50
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    30-day return policy
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
