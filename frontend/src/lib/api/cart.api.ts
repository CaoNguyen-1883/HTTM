import { apiClient } from "./client";
import { Cart, AddToCartRequest, UpdateCartItemRequest } from "../types";

export const cartApi = {
  // Get current user's cart
  getCart: async (): Promise<Cart> => {
    const response = await apiClient.get<Cart>("/cart");
    return response.data;
  },

  // Add item to cart
  addToCart: async (data: AddToCartRequest): Promise<Cart> => {
    const response = await apiClient.post<Cart>("/cart/items", data);
    return response.data;
  },

  // Update cart item quantity
  updateCartItem: async (
    itemId: string,
    data: UpdateCartItemRequest
  ): Promise<Cart> => {
    const response = await apiClient.put<Cart>(`/cart/items/${itemId}`, data);
    return response.data;
  },

  // Remove item from cart
  removeCartItem: async (itemId: string): Promise<Cart> => {
    const response = await apiClient.delete<Cart>(`/cart/items/${itemId}`);
    return response.data;
  },

  // Clear cart
  clearCart: async (): Promise<void> => {
    await apiClient.delete("/cart");
  },

  // Get cart item count
  getCartCount: async (): Promise<number> => {
    const response = await apiClient.get<{ count: number }>("/cart/count");
    return response.data.count;
  },
};