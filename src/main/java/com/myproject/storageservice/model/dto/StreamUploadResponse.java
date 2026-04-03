package com.myproject.storageservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamUploadResponse {

    private UUID id;
    private String fileName;
    private long fileSize;
    private String downloadUrl;
}