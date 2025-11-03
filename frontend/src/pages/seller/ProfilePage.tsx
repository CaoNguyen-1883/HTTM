import { Mail, Phone, MapPin, Building } from "lucide-react";

export const SellerProfilePage = () => {
  const seller = {
    fullName: "John Doe",
    email: "john.doe@example.com",
    phone: "+1 234 567 8900",
    avatarUrl: "https://via.placeholder.com/150",
    businessName: "John's Electronics",
    businessAddress: "123 Commerce Street, Business City, BC 12345",
    joinedDate: "January 2024",
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Seller Profile</h1>
        <p className="text-gray-600 mt-1">
          Manage your seller account information
        </p>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-start gap-6">
          <img
            src={seller.avatarUrl}
            alt={seller.fullName}
            className="w-24 h-24 rounded-full border-2 border-gray-200"
          />
          <div className="flex-1">
            <h2 className="text-2xl font-bold text-gray-900">{seller.fullName}</h2>
            <p className="text-gray-500 mb-4">Seller since {seller.joinedDate}</p>
            <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium">
              Edit Profile
            </button>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-bold text-gray-900 mb-4">Contact Information</h3>
        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <Mail className="w-5 h-5 text-gray-400" />
            <div>
              <p className="text-sm text-gray-500">Email</p>
              <p className="font-medium text-gray-900">{seller.email}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <Phone className="w-5 h-5 text-gray-400" />
            <div>
              <p className="text-sm text-gray-500">Phone</p>
              <p className="font-medium text-gray-900">{seller.phone}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-bold text-gray-900 mb-4">Business Information</h3>
        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <Building className="w-5 h-5 text-gray-400" />
            <div>
              <p className="text-sm text-gray-500">Business Name</p>
              <p className="font-medium text-gray-900">{seller.businessName}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <MapPin className="w-5 h-5 text-gray-400" />
            <div>
              <p className="text-sm text-gray-500">Business Address</p>
              <p className="font-medium text-gray-900">{seller.businessAddress}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-bold text-gray-900 mb-4">Account Settings</h3>
        <div className="space-y-3">
          <button className="w-full text-left px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <p className="font-medium text-gray-900">Change Password</p>
            <p className="text-sm text-gray-500">Update your password</p>
          </button>
          <button className="w-full text-left px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <p className="font-medium text-gray-900">Notification Settings</p>
            <p className="text-sm text-gray-500">Manage email and push notifications</p>
          </button>
          <button className="w-full text-left px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <p className="font-medium text-gray-900">Payment Information</p>
            <p className="text-sm text-gray-500">Update bank account details</p>
          </button>
        </div>
      </div>
    </div>
  );
};