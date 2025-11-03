import { createBrowserRouter, Navigate } from "react-router-dom";
import { ProtectedRoute } from "./ProtectedRoute";



import { PublicLayout } from "../components/layout/PublicLayout";
import { CustomerLayout } from "../components/layout/CustomerLayout";
import { SellerLayout } from "../components/layout/SellerLayout";
import { AdminLayout } from "../components/layout/AdminLayout";
import { StaffLayout } from "../components/layout/StaffLayout";


import { LoginPage } from "../pages/auth/LoginPage";
import { RegisterPage } from "../pages/auth/RegisterPage";


import { HomePage } from "../pages/public/HomePage";
import { ProductListPage } from "../pages/public/ProductListPage";
import { ProductDetailPage } from "../pages/public/ProductDetailPage";


import { CustomerDashboard } from "../pages/customer/Dashboard";
import { CartPage } from "../pages/customer/CartPage";
import { CheckoutPage } from "../pages/customer/CheckoutPage";
import { CustomerOrdersPage } from "../pages/customer/OrdersPage";
import { CustomerOrderDetailPage } from "../pages/customer/OrderDetailPage";
import { CustomerProfilePage } from "../pages/customer/ProfilePage";
import { MyReviewsPage } from "../pages/customer/MyReviewsPage";


import { SellerDashboard } from "../pages/seller/Dashboard";
import { SellerProductsPage } from "../pages/seller/ProductsPage";
import { SellerProductFormPage } from "../pages/seller/ProductFormPage";
import { SellerProductEditPage } from "../pages/seller/ProductEditPage";
import { SellerOrdersPage } from "../pages/seller/OrdersPage";
import { SellerProfilePage } from "../pages/seller/ProfilePage";


import { AdminDashboard } from "../pages/admin/Dashboard";
import { AdminUsersPage } from "../pages/admin/UsersPage";
import { AdminProductsPage } from "../pages/admin/ProductsPage";
import { AdminBrandsPage } from "../pages/admin/BrandsPage";
import { AdminCategoriesPage } from "../pages/admin/CategoriesPage";
import { AdminOrdersPage } from "../pages/admin/OrdersPage";
import { AdminReviewsPage } from "../pages/admin/ReviewsPage";


import { StaffDashboard } from "../pages/staff/Dashboard";
import { StaffOrdersPage } from "../pages/staff/OrdersPage";
import { StaffProductsPage } from "../pages/staff/ProductsPage";
import { StaffReviewsPage } from "../pages/staff/ReviewsPage";


export const router = createBrowserRouter([
  // ===================================
  // PUBLIC ROUTES
  // ===================================
  {
    path: "/",
    element: <PublicLayout />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: "products",
        element: <ProductListPage />,
      },
      {
        path: "products/:id",
        element: <ProductDetailPage />,
      },
    ],
  },

  // ===================================
  // AUTH ROUTES
  // ===================================
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/admin/login",
    element: <LoginPage expectedRole="ROLE_ADMIN" portalName="Admin" />,
  },
  {
    path: "/seller/login",
    element: <LoginPage expectedRole="ROLE_SELLER" portalName="Seller" />,
  },
  {
    path: "/staff/login",
    element: <LoginPage expectedRole="ROLE_STAFF" portalName="Staff" />,
  },
  {
    path: "/register",
    element: <RegisterPage />,
  },

  // ===================================
  // CUSTOMER ROUTES
  // ===================================
  {
    path: "/customer",
    element: (
      <ProtectedRoute allowedRoles={["ROLE_CUSTOMER"]}>
        <CustomerLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <CustomerDashboard />,
      },
      {
        path: "cart",
        element: <CartPage />,
      },
      {
        path: "checkout",
        element: <CheckoutPage />,
      },
      {
        path: "orders",
        element: <CustomerOrdersPage />,
      },
      {
        path: "orders/:id",
        element: <CustomerOrderDetailPage />,
      },
      {
        path: "reviews",
        element: <MyReviewsPage />,
      },
      {
        path: "profile",
        element: <CustomerProfilePage />,
      },
    ],
  },

  // ===================================
  // SELLER ROUTES
  // ===================================
  {
    path: "/seller",
    element: (
      <ProtectedRoute allowedRoles={["ROLE_SELLER"]}>
        <SellerLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <SellerDashboard />,
      },
      {
        path: "products",
        element: <SellerProductsPage />,
      },
      {
        path: "products/new",
        element: <SellerProductFormPage />,
      },
      {
        path: "products/:id/edit",
        element: <SellerProductEditPage />,
      },
      {
        path: "orders",
        element: <SellerOrdersPage />,
      },
      {
        path: "profile",
        element: <SellerProfilePage />,
      },
    ],
  },

  // ===================================
  // ADMIN ROUTES
  // ===================================
  {
    path: "/admin",
    element: (
      <ProtectedRoute allowedRoles={["ROLE_ADMIN"]}>
        <AdminLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <AdminDashboard />,
      },
      {
        path: "users",
        element: <AdminUsersPage />,
      },
      {
        path: "products",
        element: <AdminProductsPage />,
      },
      {
        path: "brands",
        element: <AdminBrandsPage />,
      },
      {
        path: "categories",
        element: <AdminCategoriesPage />,
      },
      {
        path: "orders",
        element: <AdminOrdersPage />,
      },
      {
        path: "reviews",
        element: <AdminReviewsPage />,
      },
    ],
  },

  // ===================================
  // STAFF ROUTES
  // ===================================
  {
    path: "/staff",
    element: (
      <ProtectedRoute allowedRoles={["ROLE_STAFF"]}>
        <StaffLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <StaffDashboard />,
      },
      {
        path: "products",
        element: <StaffProductsPage />,
      },
      {
        path: "orders",
        element: <StaffOrdersPage />,
      },
      {
        path: "reviews",
        element: <StaffReviewsPage />,
      },
    ],
  },

  // ===================================
  // 404 NOT FOUND
  // ===================================
  {
    path: "*",
    element: <Navigate to="/" replace />,
  },
]);