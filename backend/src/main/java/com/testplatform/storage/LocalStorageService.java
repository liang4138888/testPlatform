package com.testplatform.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.testplatform.common.BusinessException;

@Service
public class LocalStorageService {

    private final Path baseDir;

    public LocalStorageService(LocalStorageProperties properties) {
        this.baseDir = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "上传文件不能为空");
        }

        String originalName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String datePath = LocalDate.now().toString();
        String storedName = UUID.randomUUID() + "-" + originalName;
        Path relativePath = Paths.get(category, datePath, storedName);
        Path target = baseDir.resolve(relativePath).normalize();

        if (!target.startsWith(baseDir)) {
            throw new BusinessException("INVALID_FILE_PATH", "文件路径非法");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
            return new StoredFile(originalName, relativePath.toString(), file.getContentType(), file.getSize());
        } catch (IOException exception) {
            throw new BusinessException("FILE_STORE_FAILED", "文件保存失败");
        }
    }

    public InputStream open(String relativePath) {
        Path target = baseDir.resolve(relativePath).normalize();
        if (!target.startsWith(baseDir) || !Files.exists(target)) {
            throw new BusinessException("FILE_NOT_FOUND", "文件不存在");
        }

        try {
            return Files.newInputStream(target);
        } catch (IOException exception) {
            throw new BusinessException("FILE_OPEN_FAILED", "文件读取失败");
        }
    }

    public static class StoredFile {

        private final String originalName;
        private final String storagePath;
        private final String contentType;
        private final long sizeBytes;

        public StoredFile(String originalName, String storagePath, String contentType, long sizeBytes) {
            this.originalName = originalName;
            this.storagePath = storagePath;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
        }

        public String getOriginalName() {
            return originalName;
        }

        public String getStoragePath() {
            return storagePath;
        }

        public String getContentType() {
            return contentType;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }
    }
}
