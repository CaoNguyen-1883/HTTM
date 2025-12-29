import { apiClient } from "./client";

export interface FileUploadResponse {
  fileUrl: string;
  fileName: string;
  objectName: string;
  fileSize: number;
  contentType: string;
}

export interface MultipleFileUploadResponse {
  successful: FileUploadResponse[];
  failed: string[];
  totalSuccess: number;
  totalFailed: number;
}

export const filesApi = {
  // Upload single product image
  uploadProductImage: async (file: File): Promise<FileUploadResponse> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<FileUploadResponse>(
      "/files/upload/product",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  },

  // Upload multiple product images
  uploadMultipleProductImages: async (
    files: File[]
  ): Promise<MultipleFileUploadResponse> => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append("files", file);
    });

    const response = await apiClient.post<MultipleFileUploadResponse>(
      "/files/upload/products/multiple",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  },

  // Upload review image
  uploadReviewImage: async (file: File): Promise<FileUploadResponse> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<FileUploadResponse>(
      "/files/upload/review",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  },

  // Upload avatar
  uploadAvatar: async (file: File): Promise<FileUploadResponse> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<FileUploadResponse>(
      "/files/upload/avatar",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  },

  // Upload brand logo
  uploadBrandLogo: async (file: File): Promise<FileUploadResponse> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<FileUploadResponse>(
      "/files/upload/brand-logo",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  },

  // Delete file by object name
  deleteFile: async (objectName: string): Promise<void> => {
    await apiClient.delete("/files/delete", {
      params: { objectName },
    });
  },

  // Delete file by URL
  deleteFileByUrl: async (fileUrl: string): Promise<void> => {
    await apiClient.delete("/files/delete-by-url", {
      params: { fileUrl },
    });
  },
};
