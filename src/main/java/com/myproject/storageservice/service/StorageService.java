package com.myproject.storageservice.service;

import com.myproject.storageservice.model.dto.FileUploadResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StorageService {

    FileUploadResponse uploadFile(MultipartFile file);

    void downloadFile(UUID id, HttpServletResponse response);
}