import { create } from "zustand";
import { persist } from "zustand/middleware";
import { Cart, AddToCartRequest, UpdateCartItemRequest } from "../types";
import { cartApi } from "../api";

interface CartState {
  cart: Cart | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchCart: () => Promise<void>;
  addToCart: (data: AddToCartRequest) => Promise<void>;
  updateCartItem: (itemId: string, data: UpdateCartItemRequest) => Promise<void>;
  removeCartItem: (itemId: string) => Promise<void>;
  clearCart: () => Promise<void>;
  clearError: () => void;

  // Computed
  getTotalItems: () => number;
  getTotalAmount: () => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      cart: null,
      isLoading: false,
      error: null,

      fetchCart: async () => {
        set({ isLoading: true, error: null });
        try {
          const cart = await cartApi.getCart();
          set({ cart, isLoading: false });
        } catch (error: any) {
          // If user not logged in or cart not found, set empty cart
          if (error.response?.status === 401 || error.response?.status === 404) {
            set({ cart: null, isLoading: false });
          } else {
            set({
              error: error.response?.data?.message || "Failed to fetch cart",
              isLoading: false,
            });
          }
        }
      },

      addToCart: async (data: AddToCartRequest) => {
        set({ isLoading: true, error: null });
        try {
          const cart = await cartApi.addToCart(data);
          set({ cart, isLoading: false });
        } catch (error: any) {
          set({
            error: error.response?.data?.message || "Failed to add to cart",
            isLoading: false,
          });
          throw error;
        }
      },

      updateCartItem: async (itemId: string, data: UpdateCartItemRequest) => {
        set({ isLoading: true, error: null });
        try {
          const cart = await cartApi.updateCartItem(itemId, data);
          set({ cart, isLoading: false });
        } catch (error: any) {
          set({
            error: error.response?.data?.message || "Failed to update cart item",
            isLoading: false,
          });
          throw error;
        }
      },

      removeCartItem: async (itemId: string) => {
        set({ isLoading: true, error: null });
        try {
          const cart = await cartApi.removeCartItem(itemId);
          set({ cart, isLoading: false });
        } catch (error: any) {
          set({
            error: error.response?.data?.message || "Failed to remove cart item",
            isLoading: false,
          });
          throw error;
        }
      },

      clearCart: async () => {
        set({ isLoading: true, error: null });
        try {
          await cartApi.clearCart();
          set({ cart: null, isLoading: false });
        } catch (error: any) {
          set({
            error: error.response?.data?.message || "Failed to clear cart",
            isLoading: false,
          });
          throw error;
        }
      },

      clearError: () => {
        set({ error: null });
      },

      getTotalItems: () => {
        const { cart } = get();
        return cart?.totalItems || 0;
      },

      getTotalAmount: () => {
        const { cart } = get();
        return cart?.totalPrice || 0;
      },
    }),
    {
      name: "cart-storage",
      partialize: (state) => ({
        cart: state.cart,
      }),
    }
  )
);