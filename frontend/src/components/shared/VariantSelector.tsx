import { ProductVariant } from "../../lib/types";
import { Check } from "lucide-react";

interface VariantSelectorProps {
  variants: ProductVariant[];
  selectedVariant: ProductVariant | null;
  onVariantChange: (variant: ProductVariant) => void;
}

export const VariantSelector = ({
  variants,
  selectedVariant,
  onVariantChange,
}: VariantSelectorProps) => {
  if (!variants || variants.length === 0) {
    return null;
  }

  // Only show variant selector if there are multiple variants
  if (variants.length === 1) {
    return null;
  }

  // Group variants by their attributes (if they have color/size/etc)
  const hasAttributes = variants.some(
    (v) => v.attributes && Object.keys(v.attributes).length > 0,
  );

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-3">
          Select Variant
        </label>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {variants.map((variant) => {
            const isSelected = selectedVariant?.id === variant.id;
            const isOutOfStock = variant.stock === 0;
            const displayPrice = variant.price || 0;

            return (
              <button
                key={variant.id}
                type="button"
                onClick={() => !isOutOfStock && onVariantChange(variant)}
                disabled={isOutOfStock}
                className={`
                  relative p-4 border-2 rounded-lg text-left transition-all
                  ${
                    isSelected
                      ? "border-blue-600 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300 bg-white"
                  }
                  ${
                    isOutOfStock
                      ? "opacity-50 cursor-not-allowed"
                      : "cursor-pointer"
                  }
                `}
              >
                {/* Selected indicator */}
                {isSelected && (
                  <div className="absolute top-2 right-2">
                    <div className="w-5 h-5 bg-blue-600 rounded-full flex items-center justify-center">
                      <Check className="w-3 h-3 text-white" />
                    </div>
                  </div>
                )}

                {/* Variant info */}
                <div className="space-y-1">
                  <div className="font-semibold text-gray-900 text-sm pr-6">
                    {variant.name}
                  </div>

                  {/* Attributes (if any) */}
                  {hasAttributes &&
                    variant.attributes &&
                    Object.keys(variant.attributes).length > 0 && (
                      <div className="flex flex-wrap gap-1">
                        {Object.entries(variant.attributes).map(
                          ([key, value]) => (
                            <span
                              key={key}
                              className="text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded"
                            >
                              {String(value)}
                            </span>
                          ),
                        )}
                      </div>
                    )}

                  {/* Price */}
                  <div className="text-sm font-bold text-blue-600">
                    ${displayPrice.toFixed(2)}
                  </div>

                  {/* Stock status */}
                  <div className="text-xs">
                    {isOutOfStock ? (
                      <span className="text-red-600 font-medium">
                        Out of stock
                      </span>
                    ) : (
                      <span className="text-gray-500">
                        {variant.stock} in stock
                      </span>
                    )}
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Selected variant details */}
      {selectedVariant && (
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <div className="text-sm font-medium text-gray-700 mb-1">
                Selected: {selectedVariant.name}
              </div>
              <div className="text-xs text-gray-500">
                SKU: {selectedVariant.sku}
              </div>

              {/* Specifications (if any) */}
              {selectedVariant.specifications &&
                Object.keys(selectedVariant.specifications).length > 0 && (
                  <div className="mt-2 pt-2 border-t border-gray-200">
                    <div className="text-xs font-medium text-gray-700 mb-1">
                      Specifications:
                    </div>
                    <div className="space-y-1">
                      {Object.entries(selectedVariant.specifications).map(
                        ([key, value]) => (
                          <div
                            key={key}
                            className="text-xs text-gray-600 flex items-center gap-2"
                          >
                            <span className="font-medium">{key}:</span>
                            <span>{String(value)}</span>
                          </div>
                        ),
                      )}
                    </div>
                  </div>
                )}
            </div>
            <div className="text-right">
              <div className="text-2xl font-bold text-blue-600">
                ${selectedVariant.price?.toFixed(2) || "0.00"}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
