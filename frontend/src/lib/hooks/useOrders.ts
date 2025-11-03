import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ordersApi } from "../api/orders.api";
import {
  OrderFilterParams,
  CreateOrderRequest,
  CancelOrderRequest,
} from "../types";

// Query keys
export const orderKeys = {
  all: ["orders"] as const,
  lists: () => [...orderKeys.all, "list"] as const,
  list: (params?: OrderFilterParams) =>
    [...orderKeys.lists(), params] as const,
  details: () => [...orderKeys.all, "detail"] as const,
  detail: (id: string) => [...orderKeys.details(), id] as const,
  byNumber: (orderNumber: string) =>
    [...orderKeys.all, "number", orderNumber] as const,
  stats: () => [...orderKeys.all, "stats"] as const,
};

// Get orders with filters
export const useOrders = (params?: OrderFilterParams) => {
  return useQuery({
    queryKey: orderKeys.list(params),
    queryFn: () => ordersApi.getOrders(params),
  });
};

// Get order by ID
export const useOrder = (id: string) => {
  return useQuery({
    queryKey: orderKeys.detail(id),
    queryFn: () => ordersApi.getOrderById(id),
    enabled: !!id,
  });
};

// Get order by number
export const useOrderByNumber = (orderNumber: string) => {
  return useQuery({
    queryKey: orderKeys.byNumber(orderNumber),
    queryFn: () => ordersApi.getOrderByNumber(orderNumber),
    enabled: !!orderNumber,
  });
};

// Get order statistics
export const useOrderStats = (params?: {
  fromDate?: string;
  toDate?: string;
}) => {
  return useQuery({
    queryKey: [...orderKeys.stats(), params],
    queryFn: () => ordersApi.getOrderStats(params),
  });
};

// Create order mutation
export const useCreateOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateOrderRequest) => ordersApi.createOrder(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
    },
  });
};

// Cancel order mutation
export const useCancelOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CancelOrderRequest) => ordersApi.cancelOrder(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
      queryClient.invalidateQueries({ queryKey: orderKeys.details() });
      queryClient.invalidateQueries({ queryKey: orderKeys.stats() });
    },
  });
};

// Confirm order mutation (Staff/Admin)
export const useConfirmOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => ordersApi.confirmOrder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
      queryClient.invalidateQueries({ queryKey: orderKeys.details() });
      queryClient.invalidateQueries({ queryKey: orderKeys.stats() });
    },
  });
};

// Ship order mutation (Staff/Admin)
export const useShipOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => ordersApi.shipOrder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
      queryClient.invalidateQueries({ queryKey: orderKeys.details() });
      queryClient.invalidateQueries({ queryKey: orderKeys.stats() });
    },
  });
};

// Deliver order mutation (Staff/Admin)
export const useDeliverOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => ordersApi.deliverOrder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
      queryClient.invalidateQueries({ queryKey: orderKeys.details() });
      queryClient.invalidateQueries({ queryKey: orderKeys.stats() });
    },
  });
};
