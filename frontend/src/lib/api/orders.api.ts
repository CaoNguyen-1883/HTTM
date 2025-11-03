import { apiClient } from "./client";
import {
  Order,
  PageResponse,
  OrderFilterParams,
  CreateOrderRequest,
  CancelOrderRequest,
  OrderStats,
  RevenueStats,
} from "../types";

export const ordersApi = {
  // Create order from cart (Customer)
  createOrder: async (data: CreateOrderRequest): Promise<Order> => {
    const response = await apiClient.post<Order>("/orders", data);
    return response.data;
  },

  // Get orders with filters (Customer gets their own, Admin/Staff get all)
  getOrders: async (params?: OrderFilterParams): Promise<PageResponse<Order>> => {
    const response = await apiClient.get<PageResponse<Order>>("/orders", {
      params,
    });
    return response.data;
  },

  // Get order by ID
  getOrderById: async (id: string): Promise<Order> => {
    const response = await apiClient.get<Order>(`/orders/${id}`);
    return response.data;
  },

  // Get order by order number
  getOrderByNumber: async (orderNumber: string): Promise<Order> => {
    const response = await apiClient.get<Order>(
      `/orders/number/${orderNumber}`
    );
    return response.data;
  },

  // Cancel order (Customer)
  cancelOrder: async (data: CancelOrderRequest): Promise<Order> => {
    const response = await apiClient.post<Order>(
      `/orders/${data.orderId}/cancel`,
      { reason: data.reason }
    );
    return response.data;
  },

  // Confirm order (Admin/Staff)
  confirmOrder: async (id: string): Promise<Order> => {
    const response = await apiClient.post<Order>(`/orders/${id}/confirm`);
    return response.data;
  },

  // Ship order (Admin/Staff)
  shipOrder: async (id: string): Promise<Order> => {
    const response = await apiClient.post<Order>(`/orders/${id}/ship`);
    return response.data;
  },

  // Deliver order (Admin/Staff)
  deliverOrder: async (id: string): Promise<Order> => {
    const response = await apiClient.post<Order>(`/orders/${id}/deliver`);
    return response.data;
  },

  // Get order statistics (Admin/Seller)
  getOrderStats: async (params?: {
    fromDate?: string;
    toDate?: string;
  }): Promise<OrderStats> => {
    const response = await apiClient.get<OrderStats>("/orders/stats", {
      params,
    });
    return response.data;
  },

  // Get revenue statistics (Admin/Seller)
  getRevenueStats: async (params?: {
    fromDate?: string;
    toDate?: string;
  }): Promise<RevenueStats> => {
    const response = await apiClient.get<RevenueStats>("/orders/revenue", {
      params,
    });
    return response.data;
  },
};