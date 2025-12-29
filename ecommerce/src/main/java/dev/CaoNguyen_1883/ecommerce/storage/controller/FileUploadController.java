package dev.CaoNguyen_1883.ecommerce.storage.controller;

import dev.CaoNguyen_1883.ecommerce.storage.dto.FileUploadResponse;
import dev.CaoNguyen_1883.ecommerce.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload", description = "File upload and management APIs")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(summary = "Upload product image", description = "Upload a single product image")
    public ResponseEntity<FileUploadResponse> uploadProductImage(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading product image: {}", file.getOriginalFilename());
        FileUploadResponse response = fileStorageService.uploadProductImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/products/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(summary = "Upload multiple product images", description = "Upload multiple product images at once")
    public ResponseEntity<Map<String, Object>> uploadMultipleProductImages(
            @RequestParam("files") MultipartFile[] files) {
        log.info("Uploading {} product images", files.length);

        List<FileUploadResponse> successfulUploads = new ArrayList<>();
        List<String> failedUploads = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileUploadResponse response = fileStorageService.uploadProductImage(file);
                successfulUploads.add(response);
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                failedUploads.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successful", successfulUploads);
        result.put("failed", failedUploads);
        result.put("totalSuccess", successfulUploads.size());
        result.put("totalFailed", failedUploads.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(value = "/upload/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Upload review image", description = "Upload a review image")
    public ResponseEntity<FileUploadResponse> uploadReviewImage(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading review image: {}", file.getOriginalFilename());
        FileUploadResponse response = fileStorageService.uploadReviewImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload user avatar", description = "Upload or update user avatar")
    public ResponseEntity<FileUploadResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading avatar: {}", file.getOriginalFilename());
        FileUploadResponse response = fileStorageService.uploadAvatar(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/brand-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Upload brand logo", description = "Upload a brand logo")
    public ResponseEntity<FileUploadResponse> uploadBrandLogo(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading brand logo: {}", file.getOriginalFilename());
        FileUploadResponse response = fileStorageService.uploadBrandLogo(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(summary = "Delete file", description = "Delete a file from storage")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("objectName") String objectName) {
        log.info("Deleting file: {}", objectName);
        fileStorageService.deleteFile(objectName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        response.put("objectName", objectName);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-by-url")
    @PreAuthorize("hasAnyRole('ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(summary = "Delete file by URL", description = "Delete a file using its URL")
    public ResponseEntity<Map<String, String>> deleteFileByUrl(@RequestParam("fileUrl") String fileUrl) {
        log.info("Deleting file by URL: {}", fileUrl);
        String objectName = fileStorageService.extractObjectNameFromUrl(fileUrl);
        fileStorageService.deleteFile(objectName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        response.put("fileUrl", fileUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get presigned URL", description = "Get temporary access URL for a file")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestParam("objectName") String objectName) {
        log.info("Generating presigned URL for: {}", objectName);
        String url = fileStorageService.getPresignedUrl(objectName);

        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        response.put("objectName", objectName);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check file exists", description = "Check if a file exists in storage")
    public ResponseEntity<Map<String, Boolean>> checkFileExists(@RequestParam("objectName") String objectName) {
        boolean exists = fileStorageService.fileExists(objectName);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }
}
