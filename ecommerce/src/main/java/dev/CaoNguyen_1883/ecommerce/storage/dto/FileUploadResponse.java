package dev.CaoNguyen_1883.ecommerce.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;
    private String bucketName;
    private String objectName;
}
