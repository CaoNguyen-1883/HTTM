import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "../lib/stores";

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[]; // Changed from UserRole[] to string[]
  requireAuth?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles,
  requireAuth = true,
}) => {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  // If route requires authentication and user is not authenticated
  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If route has role restrictions and user doesn't have required role
  if (allowedRoles && user) {
    // Check if user has ANY of the allowed roles
    const hasAllowedRole = allowedRoles.some(allowedRole => 
      user.roles.includes(allowedRole)
    );

    if (!hasAllowedRole) {
      // Redirect to appropriate dashboard based on user's PRIMARY role
      // Priority: ADMIN > SELLER > STAFF > CUSTOMER
      if (user.roles.includes("ROLE_ADMIN")) {
        return <Navigate to="/admin" replace />;
      }
      if (user.roles.includes("ROLE_SELLER")) {
        return <Navigate to="/seller" replace />;
      }
      if (user.roles.includes("ROLE_STAFF")) {
        return <Navigate to="/staff" replace />;
      }
      if (user.roles.includes("ROLE_CUSTOMER")) {
        return <Navigate to="/customer" replace />;
      }
      
      // Fallback to home if no known role
      return <Navigate to="/" replace />;
    }
  }

  return <>{children}</>;
};