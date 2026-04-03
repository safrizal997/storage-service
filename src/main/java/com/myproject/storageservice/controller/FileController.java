package com.myproject.storageservice.controller;

import com.myproject.storageservice.model.dto.FileUploadResponse;
import com.myproject.storageservice.service.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = storageService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable UUID id, HttpServletResponse response) {
        storageService.downloadFile(id, response);
    }
}