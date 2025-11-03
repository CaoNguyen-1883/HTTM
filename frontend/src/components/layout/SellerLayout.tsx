import { Outlet, Link } from "react-router-dom";
import { useAuthStore } from "../../lib/stores";

export const SellerLayout = () => {
    const { user, logout } = useAuthStore();

    return (
        <div className="min-h-screen bg-gray-50">
        <header className="bg-white shadow-sm">
            <div className="container mx-auto px-4 py-4">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold">Seller Dashboard</h1>
                <div className="flex items-center gap-4">
                <span>Welcome, {user?.fullName}</span>
                <button
                    onClick={() => logout()}
                    className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                >
                    Logout
                </button>
                </div>
            </div>
            </div>
        </header>

        <div className="container mx-auto px-4 py-8">
            <div className="flex gap-6">
            <aside className="w-64 bg-white rounded-lg shadow p-4">
                <nav className="space-y-2">
                <Link
                    to="/seller"
                    className="block px-4 py-2 rounded hover:bg-gray-100"
                >
                    Dashboard
                </Link>
                <Link
                    to="/seller/products"
                    className="block px-4 py-2 rounded hover:bg-gray-100"
                >
                    Products
                </Link>
                <Link
                    to="/seller/orders"
                    className="block px-4 py-2 rounded hover:bg-gray-100"
                >
                    Orders
                </Link>
                <Link
                    to="/seller/profile"
                    className="block px-4 py-2 rounded hover:bg-gray-100"
                >
                    Profile
                </Link>
                </nav>
            </aside>

            <main className="flex-1">
                <Outlet />
            </main>
            </div>
        </div>
        </div>
    );
};