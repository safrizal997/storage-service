package com.myproject.storageservice.controller;

import com.myproject.storageservice.model.dto.StreamUploadResponse;
import com.myproject.storageservice.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/files/stream")
@RequiredArgsConstructor
public class StreamFileController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<StreamUploadResponse> streamUpload(
            HttpServletRequest request,
            @RequestHeader("X-File-Name") String fileName,
            @RequestHeader(value = "Content-Length", required = false, defaultValue = "-1") long contentLength,
            @RequestHeader(value = "Content-Type", defaultValue = "application/octet-stream") String contentType
    ) throws Exception {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Header X-File-Name is required");
        }
        if (contentLength <= 0) {
            throw new IllegalArgumentException("Header Content-Length is required and must be > 0");
        }

        StreamUploadResponse response = storageService.streamUpload(
                request.getInputStream(),
                contentLength,
                fileName,
                contentType
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    public void streamDownload(@PathVariable UUID id, HttpServletResponse response) {
        storageService.streamDownload(id, response);
    }
}