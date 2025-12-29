import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useProduct, useUpdateProduct } from "../../lib/hooks/useProducts";
import { useAllCategories } from "../../lib/hooks/useCategories";
import { useBrands } from "../../lib/hooks/useBrands";
import { X, ArrowLeft } from "lucide-react";
import { Link } from "react-router-dom";
import { ImageUpload, UploadedImage } from "../../components/seller/ImageUpload";

export const SellerProductEditPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: product, isLoading } = useProduct(id || "");
  const updateMutation = useUpdateProduct();
  const { data: categories = [] } = useAllCategories();
  const { data: brands = [] } = useBrands();

  // Basic Info
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [shortDescription, setShortDescription] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [brandId, setBrandId] = useState("");
  const [basePrice, setBasePrice] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState("");

  // Product Images
  const [productImages, setProductImages] = useState<UploadedImage[]>([]);

  // Load product data
  useEffect(() => {
    if (product) {
      setName(product.name);
      setDescription(product.description);
      setShortDescription(product.shortDescription || "");
      setCategoryId(product.category.id);
      setBrandId(product.brand.id);
      setBasePrice(product.basePrice.toString());
      setTags(product.tags || []);

      // Load existing images
      if (product.images && product.images.length > 0) {
        const loadedImages: UploadedImage[] = product.images.map((img: any) => ({
          url: img.imageUrl,
          isPrimary: img.isPrimary || false,
          displayOrder: img.displayOrder || 0,
          altText: img.altText || "",
        }));
        setProductImages(loadedImages);
      }
    }
  }, [product]);

  const addTag = () => {
    if (tagInput.trim() && !tags.includes(tagInput.trim())) {
      setTags([...tags, tagInput.trim()]);
      setTagInput("");
    }
  };

  const removeTag = (tag: string) => {
    setTags(tags.filter(t => t !== tag));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!id) return;

    // Validation
    if (!name || !description || !categoryId || !brandId || !basePrice) {
      alert("Please fill in all required fields");
      return;
    }

    const updateData = {
      name,
      description,
      shortDescription: shortDescription || undefined,
      categoryId,
      brandId,
      basePrice: parseFloat(basePrice),
      tags: tags.length > 0 ? tags : undefined,
      images: productImages.length > 0 ? productImages.map((img) => ({
        imageUrl: img.url,
        altText: img.altText,
        isPrimary: img.isPrimary,
        displayOrder: img.displayOrder,
      })) : undefined,
    };

    try {
      await updateMutation.mutateAsync({ id, data: updateData });
      alert("Product updated successfully! It will be reviewed again before going live.");
      navigate("/seller/products");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to update product");
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading product...</div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="flex flex-col items-center justify-center h-64">
        <div className="text-gray-500 text-lg mb-4">Product not found</div>
        <Link to="/seller/products" className="text-blue-600 hover:text-blue-700">
          Return to products
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate("/seller/products")}
          className="text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft className="w-6 h-6" />
        </button>
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Edit Product</h1>
          <p className="text-gray-600 mt-1">
            Update your product details. Product will be reviewed again after update.
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Basic Information</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Product Name *
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter product name"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Short Description
              </label>
              <input
                type="text"
                value={shortDescription}
                onChange={(e) => setShortDescription(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Brief one-line description"
                maxLength={500}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Description *
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Detailed product description"
                rows={6}
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Category *
                </label>
                <select
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                >
                  <option value="">Select category</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Brand *
                </label>
                <select
                  value={brandId}
                  onChange={(e) => setBrandId(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                >
                  <option value="">Select brand</option>
                  {brands.map((brand) => (
                    <option key={brand.id} value={brand.id}>{brand.name}</option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Base Price * (USD)
              </label>
              <input
                type="number"
                step="0.01"
                value={basePrice}
                onChange={(e) => setBasePrice(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="0.00"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Tags
              </label>
              <div className="flex gap-2 mb-2">
                <input
                  type="text"
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Add tag and press Enter"
                />
                <button
                  type="button"
                  onClick={addTag}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Add
                </button>
              </div>
              <div className="flex flex-wrap gap-2">
                {tags.map((tag) => (
                  <span key={tag} className="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm flex items-center gap-2">
                    {tag}
                    <button
                      type="button"
                      onClick={() => removeTag(tag)}
                      className="text-gray-500 hover:text-gray-700"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Product Images */}
        <div className="bg-white rounded-lg shadow p-6">
          <ImageUpload
            images={productImages}
            onImagesChange={setProductImages}
            maxImages={10}
            label="Product Images"
          />
          <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <p className="text-sm text-blue-800">
              <strong>Note:</strong> Updating images will replace all existing product-level images.
              Variant-specific images are not affected.
            </p>
          </div>
        </div>

        {/* Current Variants (Read-only) */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Current Variants</h2>
          <p className="text-sm text-gray-600 mb-4">
            Variant editing is not supported yet. Contact support if you need to modify variants.
          </p>
          <div className="space-y-3">
            {product.variants.map((variant) => (
              <div key={variant.id} className="border border-gray-200 rounded-lg p-4 bg-gray-50">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-semibold text-gray-900">{variant.name}</h4>
                    <p className="text-sm text-gray-600">SKU: {variant.sku}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-blue-600">
                      {variant.price != null ? `$${variant.price.toFixed(2)}` : 'Price not set'}
                    </p>
                    <p className="text-sm text-gray-600">Stock: {variant.stock || 0}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Warning */}
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
          <p className="text-sm text-yellow-800">
            <strong>Note:</strong> After updating, your product will be set to PENDING status and require admin approval before going live again.
          </p>
        </div>

        {/* Actions */}
        <div className="flex items-center justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate("/seller/products")}
            className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={updateMutation.isPending}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50"
          >
            {updateMutation.isPending ? "Updating..." : "Update Product"}
          </button>
        </div>
      </form>
    </div>
  );
};
