import { QueryClient } from "@tanstack/react-query";

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
        // Stale time: 5 minutes
        staleTime: 5 * 60 * 1000,
        
        // Cache time: 10 minutes
        gcTime: 10 * 60 * 1000,
        
        // Retry failed requests 1 time
        retry: 1,
        
        // Refetch on window focus (useful for real-time updates)
        refetchOnWindowFocus: false,
        
        // Refetch on reconnect
        refetchOnReconnect: true,
        },
        mutations: {
        // Retry failed mutations 0 times
        retry: 0,
        },
    },
});

// Query keys for organization and easy invalidation
export const queryKeys = {
    // Auth
    auth: {
        me: ["auth", "me"] as const,
    },

    // Products
    products: {
        all: ["products"] as const,
        list: (filters?: any) => ["products", "list", filters] as const,
        detail: (id: string) => ["products", "detail", id] as const,
        bySlug: (slug: string) => ["products", "slug", slug] as const,
        search: (query: string, filters?: any) =>
        ["products", "search", query, filters] as const,
    },

    // Categories
    categories: {
        all: ["categories"] as const,
        detail: (id: string) => ["categories", "detail", id] as const
    },

    // Cart
    cart: {
        current: ["cart", "current"] as const,
        count: ["cart", "count"] as const,
    },

    // Orders
    orders: {
        all: ["orders"] as const,
        list: (filters?: any) => ["orders", "list", filters] as const,
        detail: (id: string) => ["orders", "detail", id] as const,
        byNumber: (orderNumber: string) =>
        ["orders", "number", orderNumber] as const,
        stats: (filters?: any) => ["orders", "stats", filters] as const,
        revenue: (filters?: any) => ["orders", "revenue", filters] as const,
    },

    // Reviews
    reviews: {
        all: ["reviews"] as const,
        list: (filters?: any) => ["reviews", "list", filters] as const,
        detail: (id: string) => ["reviews", "detail", id] as const,
        productStats: (productId: number) =>
        ["reviews", "product-stats", productId] as const,
    },
};