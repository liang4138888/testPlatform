package com.testplatform.modules.file.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.infrastructure.storage.LocalStorageService;
import com.testplatform.modules.file.entity.FileObject;
import com.testplatform.modules.file.service.FileObjectService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileObjectService fileObjectService;
    private final LocalStorageService localStorageService;

    public FileController(FileObjectService fileObjectService, LocalStorageService localStorageService) {
        this.fileObjectService = fileObjectService;
        this.localStorageService = localStorageService;
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long fileId) throws IOException {
        return fileResponse(fileId, "attachment");
    }

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<InputStreamResource> preview(@PathVariable Long fileId) throws IOException {
        return fileResponse(fileId, "inline");
    }

    private ResponseEntity<InputStreamResource> fileResponse(Long fileId, String disposition) throws IOException {
        FileObject fileObject = fileObjectService.getRequiredFile(fileId);
        InputStream inputStream = localStorageService.open(fileObject.getStoragePath());
        String encodedName = URLEncoder.encode(fileObject.getOriginalName(), StandardCharsets.UTF_8.name())
            .replace("+", "%20");

        String contentType = fileObject.getContentType() == null
            ? MediaType.APPLICATION_OCTET_STREAM_VALUE
            : fileObject.getContentType();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename*=UTF-8''" + encodedName)
            .contentType(MediaType.parseMediaType(contentType))
            .contentLength(fileObject.getSizeBytes())
            .body(new InputStreamResource(inputStream));
    }
}
