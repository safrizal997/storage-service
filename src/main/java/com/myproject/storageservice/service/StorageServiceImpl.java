package com.myproject.storageservice.service;

import com.myproject.storageservice.config.MinioProperties;
import com.myproject.storageservice.exception.FileNotFoundException;
import com.myproject.storageservice.exception.StorageException;
import com.myproject.storageservice.model.dto.FileUploadResponse;
import com.myproject.storageservice.model.entity.FileMetadata;
import com.myproject.storageservice.repository.FileMetadataRepository;
import io.minio.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        try {
            String bucket = minioProperties.getBucket();

            // Ensure bucket exists
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            // Generate a unique object key
            String objectKey = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Upload the file to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Save file metadata to the database
            FileMetadata metadata = FileMetadata.builder()
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .bucketName(bucket)
                    .objectKey(objectKey)
                    .build();

            FileMetadata saved = fileMetadataRepository.save(metadata);

            String downloadUrl = "/api/files/download/" + saved.getId();

            return FileUploadResponse.builder()
                    .id(saved.getId())
                    .fileName(saved.getFileName())
                    .fileSize(saved.getFileSize())
                    .downloadUrl(downloadUrl)
                    .build();

        } catch (Exception e) {
            throw new StorageException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void downloadFile(UUID id, HttpServletResponse response) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(metadata.getBucketName())
                        .object(metadata.getObjectKey())
                        .build()
        )) {
            response.setContentType(metadata.getContentType());
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"" + metadata.getFileName() + "\""
            );
            response.setContentLengthLong(metadata.getFileSize());

            stream.transferTo(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            throw new StorageException("Failed to download file: " + e.getMessage(), e);
        }
    }
}