package com.testplatform.modules.file.dto;

import java.time.LocalDateTime;

import com.testplatform.modules.file.entity.FileObject;

public class FileObjectResponse {

    private final Long id;
    private final String originalName;
    private final String fileKind;
    private final Long sizeBytes;
    private final LocalDateTime createdAt;

    public FileObjectResponse(Long id, String originalName, String fileKind, Long sizeBytes, LocalDateTime createdAt) {
        this.id = id;
        this.originalName = originalName;
        this.fileKind = fileKind;
        this.sizeBytes = sizeBytes;
        this.createdAt = createdAt;
    }

    public static FileObjectResponse from(FileObject fileObject) {
        return new FileObjectResponse(
            fileObject.getId(),
            fileObject.getOriginalName(),
            fileObject.getFileKind(),
            fileObject.getSizeBytes(),
            fileObject.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getFileKind() {
        return fileKind;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
