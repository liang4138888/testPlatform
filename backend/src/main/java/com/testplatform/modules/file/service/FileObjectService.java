package com.testplatform.modules.file.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.testplatform.common.exception.BusinessException;
import com.testplatform.infrastructure.storage.LocalStorageService;
import com.testplatform.modules.file.entity.FileObject;
import com.testplatform.modules.file.mapper.FileObjectMapper;

@Service
public class FileObjectService {

    public static final String KIND_ORIGINAL = "ORIGINAL";
    public static final String KIND_EXPORTED = "EXPORTED";
    public static final String KIND_BUG_IMAGE = "BUG_IMAGE";

    private final FileObjectMapper fileObjectMapper;

    public FileObjectService(FileObjectMapper fileObjectMapper) {
        this.fileObjectMapper = fileObjectMapper;
    }

    @Transactional
    public FileObject createFromStored(LocalStorageService.StoredFile storedFile, String fileKind) {
        FileObject fileObject = new FileObject();
        fileObject.setOriginalName(storedFile.getOriginalName());
        fileObject.setStoragePath(storedFile.getStoragePath());
        fileObject.setContentType(storedFile.getContentType());
        fileObject.setSizeBytes(storedFile.getSizeBytes());
        fileObject.setFileKind(fileKind);
        fileObjectMapper.insert(fileObject);
        return fileObjectMapper.selectById(fileObject.getId());
    }

    public FileObject getRequiredFile(Long fileId) {
        FileObject fileObject = fileObjectMapper.selectById(fileId);
        if (fileObject == null) {
            throw new BusinessException("FILE_NOT_FOUND", "文件不存在");
        }
        return fileObject;
    }
}
