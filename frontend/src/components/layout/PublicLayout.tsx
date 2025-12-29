import { Outlet, Link, useNavigate } from "react-router-dom";
import { ShoppingCart, User, LogOut, Package, Menu, X } from "lucide-react";
import { useAuthStore } from "../../lib/stores/authStore";
import { useCartCount } from "../../lib/hooks";
import { useState } from "react";

export const PublicLayout = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuthStore();
  const { data: cartCount = 0 } = useCartCount();
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            {/* Logo */}
            <Link to="/" className="text-2xl font-bold text-blue-600">
              E-Commerce
            </Link>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex items-center gap-6">
              <Link
                to="/products"
                className="text-gray-700 hover:text-blue-600 font-medium transition-colors"
              >
                Products
              </Link>

              {isAuthenticated ? (
                <>
                  {/* Cart Icon with Badge */}
                  <Link
                    to="/customer/cart"
                    className="relative text-gray-700 hover:text-blue-600 transition-colors"
                  >
                    <ShoppingCart className="w-6 h-6" />
                    {cartCount > 0 && (
                      <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                        {cartCount > 9 ? "9+" : cartCount}
                      </span>
                    )}
                  </Link>

                  {/* User Menu */}
                  <div className="relative">
                    <button
                      onClick={() => setShowUserMenu(!showUserMenu)}
                      className="flex items-center gap-2 text-gray-700 hover:text-blue-600 transition-colors"
                    >
                      <User className="w-6 h-6" />
                      <span className="font-medium">{user?.fullName}</span>
                    </button>

                    {showUserMenu && (
                      <>
                        <div
                          className="fixed inset-0 z-10"
                          onClick={() => setShowUserMenu(false)}
                        />
                        <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-2 z-20">
                          <Link
                            to="/customer/orders"
                            className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100"
                            onClick={() => setShowUserMenu(false)}
                          >
                            <Package className="w-4 h-4" />
                            Orders
                          </Link>
                          <Link
                            to="/customer/profile"
                            className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100"
                            onClick={() => setShowUserMenu(false)}
                          >
                            <User className="w-4 h-4" />
                            Profile
                          </Link>
                          <hr className="my-2" />
                          <button
                            onClick={handleLogout}
                            className="flex items-center gap-2 px-4 py-2 text-red-600 hover:bg-gray-100 w-full text-left"
                          >
                            <LogOut className="w-4 h-4" />
                            Logout
                          </button>
                        </div>
                      </>
                    )}
                  </div>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="text-gray-700 hover:text-blue-600 font-medium transition-colors"
                  >
                    Login
                  </Link>
                  <Link
                    to="/register"
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors"
                  >
                    Register
                  </Link>
                </>
              )}

              {/* Portal Links */}
              {/*<div className="ml-4 border-l pl-4 flex gap-3">
                <Link
                  to="/admin/login"
                  className="text-xs text-gray-500 hover:text-gray-700"
                >
                  Admin
                </Link>
                <Link
                  to="/seller/login"
                  className="text-xs text-gray-500 hover:text-gray-700"
                >
                  Seller
                </Link>
                <Link
                  to="/staff/login"
                  className="text-xs text-gray-500 hover:text-gray-700"
                >
                  Staff
                </Link>
              </div>*/}
            </nav>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setShowMobileMenu(!showMobileMenu)}
              className="md:hidden text-gray-700"
            >
              {showMobileMenu ? (
                <X className="w-6 h-6" />
              ) : (
                <Menu className="w-6 h-6" />
              )}
            </button>
          </div>

          {/* Mobile Navigation */}
          {showMobileMenu && (
            <nav className="md:hidden mt-4 pt-4 border-t space-y-2">
              <Link
                to="/products"
                className="block py-2 text-gray-700 hover:text-blue-600"
                onClick={() => setShowMobileMenu(false)}
              >
                Sản phẩm
              </Link>

              {isAuthenticated ? (
                <>
                  <Link
                    to="/customer/cart"
                    className="flex items-center justify-between py-2 text-gray-700 hover:text-blue-600"
                    onClick={() => setShowMobileMenu(false)}
                  >
                    <span>Cart</span>
                    {cartCount > 0 && (
                      <span className="bg-red-500 text-white text-xs font-bold rounded-full px-2 py-1">
                        {cartCount}
                      </span>
                    )}
                  </Link>
                  <Link
                    to="/customer/orders"
                    className="block py-2 text-gray-700 hover:text-blue-600"
                    onClick={() => setShowMobileMenu(false)}
                  >
                    Orders
                  </Link>
                  <Link
                    to="/customer/profile"
                    className="block py-2 text-gray-700 hover:text-blue-600"
                    onClick={() => setShowMobileMenu(false)}
                  >
                    Account
                  </Link>
                  <button
                    onClick={() => {
                      handleLogout();
                      setShowMobileMenu(false);
                    }}
                    className="block w-full text-left py-2 text-red-600"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="block py-2 text-gray-700 hover:text-blue-600"
                    onClick={() => setShowMobileMenu(false)}
                  >
                    Login
                  </Link>
                  <Link
                    to="/register"
                    className="block py-2 text-blue-600 font-medium"
                    onClick={() => setShowMobileMenu(false)}
                  >
                    Register
                  </Link>
                </>
              )}

              <div className="pt-2 border-t mt-2 space-y-2">
                <Link
                  to="/admin/login"
                  className="block py-1 text-xs text-gray-500"
                  onClick={() => setShowMobileMenu(false)}
                >
                  Admin Portal
                </Link>
                <Link
                  to="/seller/login"
                  className="block py-1 text-xs text-gray-500"
                  onClick={() => setShowMobileMenu(false)}
                >
                  Seller Portal
                </Link>
                <Link
                  to="/staff/login"
                  className="block py-1 text-xs text-gray-500"
                  onClick={() => setShowMobileMenu(false)}
                >
                  Staff Portal
                </Link>
              </div>
            </nav>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main>
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 text-white py-8 mt-16">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div>
              <h3 className="text-lg font-bold mb-4">E-Commerce</h3>
            </div>
            <div>
              <h4 className="font-semibold mb-4">About</h4>
              <ul className="space-y-2 text-sm text-gray-400">
                <li>
                  <Link to="/about" className="hover:text-white">
                    About Us
                  </Link>
                </li>
                <li>
                  <Link to="/contact" className="hover:text-white">
                    Contact Us    
                  </Link>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Policy</h4>
              <ul className="space-y-2 text-sm text-gray-400">
                <li>
                  <Link to="/privacy" className="hover:text-white">
                    Privacy Policy
                  </Link>
                </li>
                <li>
                  <Link to="/terms" className="hover:text-white">
                    Terms of Service
                  </Link>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Contact</h4>
              <ul className="space-y-2 text-sm text-gray-400">
                <li>Email: n22dccn200@student.ptithcm.edu.vn</li>
                <li>Hotline: 1900 1234</li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-700 mt-8 pt-8 text-center text-sm text-gray-400">
            <p>&copy; 2025 E-Commerce. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};
