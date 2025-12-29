import { useState, useRef, useEffect } from "react";
import { Upload, X, Loader2, Clipboard } from "lucide-react";
import { filesApi } from "../../lib/api/files.api";

export interface UploadedImage {
  url: string;
  file?: File;
  isPrimary: boolean;
  displayOrder: number;
  altText?: string;
}

interface ImageUploadProps {
  images: UploadedImage[];
  onImagesChange: (images: UploadedImage[]) => void;
  maxImages?: number;
  label?: string;
}

export const ImageUpload: React.FC<ImageUploadProps> = ({
  images,
  onImagesChange,
  maxImages = 10,
  label = "Product Images",
}) => {
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState<string>("");
  const [isPasteAreaFocused, setIsPasteAreaFocused] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const pasteAreaRef = useRef<HTMLDivElement>(null);

  // Refactored upload function to be reusable
  const uploadFiles = async (files: File[]) => {
    if (files.length === 0) return;

    // Check max images limit
    if (images.length + files.length > maxImages) {
      alert(`You can only upload up to ${maxImages} images`);
      return;
    }

    setIsUploading(true);
    setUploadProgress(`Uploading ${files.length} image(s)...`);

    try {
      // Upload files to MinIO
      const result = await filesApi.uploadMultipleProductImages(files);

      if (result.failed.length > 0) {
        console.error("Failed uploads:", result.failed);
        alert(`Some images failed to upload:\n${result.failed.join("\n")}`);
      }

      // Add successfully uploaded images
      const newImages: UploadedImage[] = result.successful.map((upload, index) => ({
        url: upload.fileUrl,
        isPrimary: images.length === 0 && index === 0, // First image is primary if no images exist
        displayOrder: images.length + index,
        altText: upload.fileName,
      }));

      onImagesChange([...images, ...newImages]);
      setUploadProgress(`Successfully uploaded ${result.totalSuccess} image(s)`);

      // Clear success message after 2 seconds
      setTimeout(() => setUploadProgress(""), 2000);
    } catch (error: any) {
      console.error("Upload error:", error);
      alert(error.response?.data?.message || "Failed to upload images");
      setUploadProgress("");
    } finally {
      setIsUploading(false);
      // Reset input
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    await uploadFiles(files);
  };

  // Handle paste event
  const handlePaste = async (e: ClipboardEvent) => {
    e.preventDefault();

    const clipboardItems = e.clipboardData?.items;
    if (!clipboardItems) return;

    const imageFiles: File[] = [];

    // Extract image files from clipboard
    for (let i = 0; i < clipboardItems.length; i++) {
      const item = clipboardItems[i];

      if (item.type.startsWith("image/")) {
        const file = item.getAsFile();
        if (file) {
          // Rename file to include timestamp
          const timestamp = new Date().getTime();
          const extension = file.type.split("/")[1];
          const renamedFile = new File(
            [file],
            `pasted-image-${timestamp}.${extension}`,
            { type: file.type }
          );
          imageFiles.push(renamedFile);
        }
      }
    }

    if (imageFiles.length > 0) {
      setUploadProgress(`Pasted ${imageFiles.length} image(s) from clipboard`);
      await uploadFiles(imageFiles);
    } else {
      alert("No image found in clipboard. Please copy an image first.");
    }
  };

  // Add paste event listener
  useEffect(() => {
    const pasteArea = pasteAreaRef.current;
    if (!pasteArea) return;

    const handlePasteEvent = (e: ClipboardEvent) => {
      handlePaste(e);
    };

    pasteArea.addEventListener("paste", handlePasteEvent as any);

    return () => {
      pasteArea.removeEventListener("paste", handlePasteEvent as any);
    };
  }, [images, maxImages]); // Re-attach when dependencies change

  const removeImage = (index: number) => {
    const newImages = images.filter((_, i) => i !== index);

    // If removed image was primary, make first image primary
    if (images[index].isPrimary && newImages.length > 0) {
      newImages[0].isPrimary = true;
    }

    // Reorder display order
    newImages.forEach((img, i) => {
      img.displayOrder = i;
    });

    onImagesChange(newImages);
  };

  const setPrimaryImage = (index: number) => {
    const newImages = images.map((img, i) => ({
      ...img,
      isPrimary: i === index,
    }));
    onImagesChange(newImages);
  };

  const moveImage = (fromIndex: number, toIndex: number) => {
    const newImages = [...images];
    const [movedImage] = newImages.splice(fromIndex, 1);
    newImages.splice(toIndex, 0, movedImage);

    // Update display order
    newImages.forEach((img, i) => {
      img.displayOrder = i;
    });

    onImagesChange(newImages);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <label className="block text-sm font-medium text-gray-700">
          {label}
          {images.length > 0 && (
            <span className="ml-2 text-gray-500 text-xs">
              ({images.length}/{maxImages})
            </span>
          )}
        </label>
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          disabled={isUploading || images.length >= maxImages}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
        >
          {isUploading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Uploading...
            </>
          ) : (
            <>
              <Upload className="w-4 h-4" />
              Upload Images
            </>
          )}
        </button>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
        multiple
        onChange={handleFileSelect}
        className="hidden"
      />

      {/* Paste Area */}
      <div
        ref={pasteAreaRef}
        tabIndex={0}
        onFocus={() => setIsPasteAreaFocused(true)}
        onBlur={() => setIsPasteAreaFocused(false)}
        className={`border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition-all ${
          isPasteAreaFocused
            ? "border-blue-500 bg-blue-50"
            : "border-gray-300 bg-gray-50 hover:border-blue-400 hover:bg-blue-50"
        }`}
      >
        <Clipboard className={`w-10 h-10 mx-auto mb-2 ${isPasteAreaFocused ? "text-blue-600" : "text-gray-400"}`} />
        <p className={`text-sm font-medium ${isPasteAreaFocused ? "text-blue-700" : "text-gray-600"}`}>
          {isPasteAreaFocused ? "Ready to paste! Press Ctrl+V (Cmd+V on Mac)" : "Click here and paste images (Ctrl+V / Cmd+V)"}
        </p>
        <p className="text-xs text-gray-500 mt-1">
          Copy an image from anywhere and paste it here
        </p>
      </div>

      {uploadProgress && (
        <div className="text-sm text-blue-600 bg-blue-50 border border-blue-200 rounded-lg px-4 py-2">
          {uploadProgress}
        </div>
      )}

      {images.length === 0 ? (
        <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
          <Upload className="w-12 h-12 text-gray-400 mx-auto mb-3" />
          <p className="text-gray-600 text-sm">
            No images uploaded yet. Upload or paste images above.
          </p>
          <p className="text-gray-500 text-xs mt-2">
            Supported formats: JPG, PNG, GIF, WEBP (Max 5MB per image)
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {images.map((image, index) => (
            <div
              key={index}
              className={`relative group border-2 rounded-lg overflow-hidden ${
                image.isPrimary ? "border-blue-500" : "border-gray-200"
              }`}
            >
              <img
                src={image.url}
                alt={image.altText || `Product image ${index + 1}`}
                className="w-full h-40 object-cover"
              />

              {/* Primary badge */}
              {image.isPrimary && (
                <div className="absolute top-2 left-2 bg-blue-600 text-white text-xs px-2 py-1 rounded">
                  Primary
                </div>
              )}

              {/* Display order */}
              <div className="absolute top-2 right-2 bg-black bg-opacity-60 text-white text-xs px-2 py-1 rounded">
                #{index + 1}
              </div>

              {/* Actions overlay */}
              <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100">
                {!image.isPrimary && (
                  <button
                    type="button"
                    onClick={() => setPrimaryImage(index)}
                    className="px-3 py-1 bg-blue-600 text-white text-xs rounded hover:bg-blue-700"
                  >
                    Set Primary
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => removeImage(index)}
                  className="px-3 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700 flex items-center gap-1"
                >
                  <X className="w-3 h-3" />
                  Remove
                  </button>
              </div>

              {/* Reorder buttons */}
              <div className="absolute bottom-2 left-2 right-2 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                {index > 0 && (
                  <button
                    type="button"
                    onClick={() => moveImage(index, index - 1)}
                    className="flex-1 px-2 py-1 bg-gray-800 text-white text-xs rounded hover:bg-gray-700"
                  >
                    ← Move Left
                  </button>
                )}
                {index < images.length - 1 && (
                  <button
                    type="button"
                    onClick={() => moveImage(index, index + 1)}
                    className="flex-1 px-2 py-1 bg-gray-800 text-white text-xs rounded hover:bg-gray-700"
                  >
                    Move Right →
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {images.length > 0 && (
        <div className="text-xs text-gray-500 space-y-1">
          <p>• The first image (marked "Primary") will be the main product image</p>
          <p>• Use "Move" buttons to reorder images</p>
          <p>• Click "Set Primary" to change the main image</p>
          <p>• <strong>Tip:</strong> Copy image (Ctrl+C) from anywhere and paste (Ctrl+V) in the paste area above</p>
        </div>
      )}
    </div>
  );
};
