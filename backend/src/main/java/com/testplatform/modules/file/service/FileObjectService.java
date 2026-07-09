package com.testplatform.modules.file.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.infrastructure.storage.LocalStorageService;
import com.testplatform.modules.auth.support.CurrentUserContext;
import com.testplatform.modules.bug.entity.Bug;
import com.testplatform.modules.bug.entity.BugAttachment;
import com.testplatform.modules.bug.mapper.BugAttachmentMapper;
import com.testplatform.modules.bug.mapper.BugMapper;
import com.testplatform.modules.casesuite.entity.CaseSuite;
import com.testplatform.modules.casesuite.mapper.CaseSuiteMapper;
import com.testplatform.modules.file.entity.FileObject;
import com.testplatform.modules.file.mapper.FileObjectMapper;
import com.testplatform.modules.user.service.UserService;

@Service
public class FileObjectService {

    public static final String KIND_ORIGINAL = "ORIGINAL";
    public static final String KIND_EXPORTED = "EXPORTED";
    public static final String KIND_BUG_IMAGE = "BUG_IMAGE";
    public static final String PERMISSION_CASE_EDIT = "CASE_EDIT";
    public static final String PERMISSION_CASE_EXPORT = "CASE_EXPORT";
    public static final String PERMISSION_BUG_VIEW = "BUG_VIEW";

    private final FileObjectMapper fileObjectMapper;
    private final CaseSuiteMapper caseSuiteMapper;
    private final BugAttachmentMapper bugAttachmentMapper;
    private final BugMapper bugMapper;
    private final UserService userService;

    public FileObjectService(FileObjectMapper fileObjectMapper, CaseSuiteMapper caseSuiteMapper,
            BugAttachmentMapper bugAttachmentMapper, BugMapper bugMapper, UserService userService) {
        this.fileObjectMapper = fileObjectMapper;
        this.caseSuiteMapper = caseSuiteMapper;
        this.bugAttachmentMapper = bugAttachmentMapper;
        this.bugMapper = bugMapper;
        this.userService = userService;
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

    public FileObject getRequiredReadableFile(Long fileId) {
        FileObject fileObject = getRequiredFile(fileId);
        ensureReadable(fileObject);
        return fileObject;
    }

    private void ensureReadable(FileObject fileObject) {
        requireFilePermission(fileObject);
        if (userService.canViewAllData()) {
            return;
        }
        Long userId = CurrentUserContext.getUserId();
        if (KIND_ORIGINAL.equals(fileObject.getFileKind()) || KIND_EXPORTED.equals(fileObject.getFileKind())) {
            CaseSuite suite = caseSuiteMapper.selectOne(new LambdaQueryWrapper<CaseSuite>()
                .and(wrapper -> wrapper
                    .eq(CaseSuite::getOriginalFileId, fileObject.getId())
                    .or()
                    .eq(CaseSuite::getExportedFileId, fileObject.getId()))
                .last("LIMIT 1"));
            if (suite != null && userId.equals(suite.getCreatedBy())) {
                return;
            }
        }
        if (KIND_BUG_IMAGE.equals(fileObject.getFileKind())) {
            BugAttachment attachment = bugAttachmentMapper.selectOne(new LambdaQueryWrapper<BugAttachment>()
                .eq(BugAttachment::getFileId, fileObject.getId())
                .last("LIMIT 1"));
            if (attachment != null) {
                Bug bug = bugMapper.selectById(attachment.getBugId());
                if (bug != null && (userId.equals(bug.getReporterId()) || userId.equals(bug.getAssigneeId()))) {
                    return;
                }
            }
        }
        throw new BusinessException("PERMISSION_DENIED", "无权限访问该文件");
    }

    private void requireFilePermission(FileObject fileObject) {
        if (KIND_BUG_IMAGE.equals(fileObject.getFileKind())) {
            userService.requirePermission(PERMISSION_BUG_VIEW);
            return;
        }
        if (KIND_EXPORTED.equals(fileObject.getFileKind())) {
            userService.requirePermission(PERMISSION_CASE_EXPORT);
            return;
        }
        if (KIND_ORIGINAL.equals(fileObject.getFileKind())) {
            userService.requirePermission(PERMISSION_CASE_EDIT);
            return;
        }
        if (!userService.canViewAllData()) {
            throw new BusinessException("PERMISSION_DENIED", "无权限访问该文件");
        }
    }
}
