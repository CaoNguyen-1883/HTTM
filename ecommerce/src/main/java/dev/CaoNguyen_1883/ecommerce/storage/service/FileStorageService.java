package dev.CaoNguyen_1883.ecommerce.storage.service;

import dev.CaoNguyen_1883.ecommerce.config.MinioConfig;
import dev.CaoNguyen_1883.ecommerce.storage.dto.FileUploadResponse;
import dev.CaoNguyen_1883.ecommerce.storage.exception.FileStorageException;
import dev.CaoNguyen_1883.ecommerce.storage.exception.InvalidFileException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * Initialize bucket if not exists
     */
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );

            if (!exists) {
                log.info("Bucket '{}' does not exist. Creating...", minioConfig.getBucketName());
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );

                // Set public read policy for product images
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(minioConfig.getBucketName());

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .config(policy)
                                .build()
                );

                log.info("Bucket '{}' created successfully with public read policy", minioConfig.getBucketName());
            } else {
                log.debug("Bucket '{}' already exists", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("Error ensuring bucket exists", e);
            throw new FileStorageException("Failed to initialize storage bucket", e);
        }
    }

    /**
     * Upload file to MinIO
     */
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        // Validate file
        validateFile(file);

        // Ensure bucket exists
        ensureBucketExists();

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + "." + extension;
            String objectName = folder + "/" + filename;

            // Upload file
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded successfully: {}", objectName);

            // Generate file URL
            String fileUrl = getFileUrl(objectName);

            return FileUploadResponse.builder()
                    .fileName(originalFilename)
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .bucketName(minioConfig.getBucketName())
                    .objectName(objectName)
                    .build();

        } catch (Exception e) {
            log.error("Error uploading file", e);
            throw new FileStorageException("Failed to upload file: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Upload product image
     */
    public FileUploadResponse uploadProductImage(MultipartFile file) {
        return uploadFile(file, "products");
    }

    /**
     * Upload review image
     */
    public FileUploadResponse uploadReviewImage(MultipartFile file) {
        return uploadFile(file, "reviews");
    }

    /**
     * Upload avatar
     */
    public FileUploadResponse uploadAvatar(MultipartFile file) {
        return uploadFile(file, "avatars");
    }

    /**
     * Upload brand logo
     */
    public FileUploadResponse uploadBrandLogo(MultipartFile file) {
        return uploadFile(file, "brands");
    }

    /**
     * Delete file from MinIO
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("File deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting file: {}", objectName, e);
            throw new FileStorageException("Failed to delete file: " + objectName, e);
        }
    }

    /**
     * Get presigned URL for temporary access (7 days)
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            throw new FileStorageException("Failed to generate URL for: " + objectName, e);
        }
    }

    /**
     * Get public file URL
     */
    public String getFileUrl(String objectName) {
        // For public buckets, return direct URL
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        // Check file size
        if (file.getSize() > minioConfig.getMaxFileSize()) {
            throw new InvalidFileException(
                    String.format("File size exceeds maximum allowed size of %d MB",
                            minioConfig.getMaxFileSize() / (1024 * 1024))
            );
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InvalidFileException("Filename is missing");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!Arrays.asList(minioConfig.getAllowedExtensions()).contains(extension)) {
            throw new InvalidFileException(
                    "File type not allowed. Allowed types: " + String.join(", ", minioConfig.getAllowedExtensions())
            );
        }
    }

    /**
     * Extract file extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new InvalidFileException("File has no extension");
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Extract object name from URL
     */
    public String extractObjectNameFromUrl(String fileUrl) {
        // URL format: http://localhost:9000/bucket-name/folder/filename.ext
        // Extract: folder/filename.ext
        String prefix = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        throw new IllegalArgumentException("Invalid file URL format");
    }
}
