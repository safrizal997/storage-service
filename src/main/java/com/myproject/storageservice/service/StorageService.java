package com.myproject.storageservice.service;

import com.myproject.storageservice.model.dto.FileUploadResponse;
import com.myproject.storageservice.model.dto.StreamUploadResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface StorageService {

    FileUploadResponse uploadFile(MultipartFile file);

    void downloadFile(UUID id, HttpServletResponse response);

    StreamUploadResponse streamUpload(InputStream inputStream, long fileSize, String fileName, String contentType);

    void streamDownload(UUID id, HttpServletResponse response);
}