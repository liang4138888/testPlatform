package com.testplatform.modules.bug.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.infrastructure.storage.LocalStorageService;
import com.testplatform.modules.auth.support.CurrentUserContext;
import com.testplatform.modules.bug.dto.BugAttachmentResponse;
import com.testplatform.modules.bug.dto.BugCommentCreateRequest;
import com.testplatform.modules.bug.dto.BugCommentResponse;
import com.testplatform.modules.bug.dto.BugCreateRequest;
import com.testplatform.modules.bug.dto.BugDetailResponse;
import com.testplatform.modules.bug.dto.BugHistoryResponse;
import com.testplatform.modules.bug.dto.BugListItemResponse;
import com.testplatform.modules.bug.dto.BugUpdateRequest;
import com.testplatform.modules.bug.entity.Bug;
import com.testplatform.modules.bug.entity.BugAttachment;
import com.testplatform.modules.bug.entity.BugComment;
import com.testplatform.modules.bug.entity.BugHistory;
import com.testplatform.modules.bug.mapper.BugAttachmentMapper;
import com.testplatform.modules.bug.mapper.BugCommentMapper;
import com.testplatform.modules.bug.mapper.BugHistoryMapper;
import com.testplatform.modules.bug.mapper.BugMapper;
import com.testplatform.modules.file.entity.FileObject;
import com.testplatform.modules.file.service.FileObjectService;
import com.testplatform.modules.notification.service.NotificationService;
import com.testplatform.modules.requirement.entity.Requirement;
import com.testplatform.modules.requirement.service.RequirementService;

@Service
public class BugService {

    private static final List<String> STATUSES = Arrays.asList("待修复", "已修复", "修复完成");

    private final BugMapper bugMapper;
    private final BugAttachmentMapper bugAttachmentMapper;
    private final BugCommentMapper bugCommentMapper;
    private final BugHistoryMapper bugHistoryMapper;
    private final BugHistoryService bugHistoryService;
    private final RequirementService requirementService;
    private final NotificationService notificationService;
    private final LocalStorageService localStorageService;
    private final FileObjectService fileObjectService;
    private final com.testplatform.modules.user.service.UserService userService;

    public BugService(BugMapper bugMapper, BugAttachmentMapper bugAttachmentMapper,
            BugCommentMapper bugCommentMapper, BugHistoryMapper bugHistoryMapper,
            BugHistoryService bugHistoryService, RequirementService requirementService,
            NotificationService notificationService, LocalStorageService localStorageService,
            FileObjectService fileObjectService, com.testplatform.modules.user.service.UserService userService) {
        this.bugMapper = bugMapper;
        this.bugAttachmentMapper = bugAttachmentMapper;
        this.bugCommentMapper = bugCommentMapper;
        this.bugHistoryMapper = bugHistoryMapper;
        this.bugHistoryService = bugHistoryService;
        this.requirementService = requirementService;
        this.notificationService = notificationService;
        this.localStorageService = localStorageService;
        this.fileObjectService = fileObjectService;
        this.userService = userService;
    }

    public List<BugListItemResponse> list(Long projectId, Long requirementId, String status, String severity, Long assigneeId, String keyword) {
        LambdaQueryWrapper<Bug> query = new LambdaQueryWrapper<Bug>()
            .eq(projectId != null, Bug::getProjectId, projectId)
            .eq(requirementId != null, Bug::getRequirementId, requirementId)
            .eq(status != null && !status.isEmpty(), Bug::getStatus, status)
            .eq(severity != null && !severity.isEmpty(), Bug::getSeverity, severity)
            .eq(assigneeId != null, Bug::getAssigneeId, assigneeId)
            .and(!userService.canViewAllData(), wrapper -> wrapper
                .eq(Bug::getReporterId, CurrentUserContext.getUserId())
                .or()
                .eq(Bug::getAssigneeId, CurrentUserContext.getUserId()))
            .and(keyword != null && !keyword.isEmpty(), wrapper -> wrapper
                .like(Bug::getTitle, keyword)
                .or()
                .like(Bug::getBugNo, keyword))
            .orderByDesc(Bug::getUpdatedAt);
        return bugMapper.selectList(query).stream()
            .map(bug -> BugListItemResponse.from(bug, attachmentCount(bug.getId())))
            .collect(Collectors.toList());
    }

    @Transactional
    public BugDetailResponse create(Long requirementId, BugCreateRequest request) {
        Long operatorId = CurrentUserContext.getUserId();
        Requirement requirement = requirementService.getRequiredRequirement(requirementId);
        if (request.getAssigneeId() == null) {
            throw new BusinessException("BUG_ASSIGNEE_REQUIRED", "Bug 指派人必填");
        }
        Bug bug = new Bug();
        bug.setProjectId(requirement.getProjectId());
        bug.setRequirementId(requirementId);
        bug.setCaseSuiteId(request.getCaseSuiteId());
        bug.setBugNo(nextBugNo(requirement.getProjectId()));
        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setStatus("待修复");
        bug.setSeverity(defaultValue(request.getSeverity(), "中"));
        bug.setPriority(defaultValue(request.getPriority(), "中"));
        bug.setReporterId(operatorId);
        bug.setAssigneeId(request.getAssigneeId());
        bugMapper.insert(bug);
        bugHistoryService.record(bug.getId(), "创建 Bug", null, null, bug.getBugNo(), operatorId);
        notificationService.create(bug.getAssigneeId(), operatorId, bug.getId(), "你被指派了一个 Bug", bug.getBugNo() + " " + bug.getTitle());
        return detail(bug.getId());
    }

    public BugDetailResponse detail(Long bugId) {
        Bug bug = getRequiredBug(bugId);
        BugDetailResponse response = BugDetailResponse.from(bug, attachmentCount(bugId));
        response.setAttachments(attachments(bugId));
        response.setComments(comments(bugId));
        response.setHistories(histories(bugId));
        return response;
    }

    @Transactional
    public BugDetailResponse update(Long bugId, BugUpdateRequest request) {
        Long operatorId = CurrentUserContext.getUserId();
        Bug bug = getRequiredBug(bugId);
        change(bug, "标题", bug.getTitle(), request.getTitle(), operatorId);
        change(bug, "描述", bug.getDescription(), request.getDescription(), operatorId);
        change(bug, "严重级别", bug.getSeverity(), request.getSeverity(), operatorId);
        change(bug, "优先级", bug.getPriority(), request.getPriority(), operatorId);
        if (request.getAssigneeId() != null && !Objects.equals(bug.getAssigneeId(), request.getAssigneeId())) {
            bugHistoryService.record(bugId, "修改指派人", "assigneeId", String.valueOf(bug.getAssigneeId()), String.valueOf(request.getAssigneeId()), operatorId);
            bug.setAssigneeId(request.getAssigneeId());
            notificationService.create(bug.getAssigneeId(), operatorId, bugId, "你被指派了一个 Bug", bug.getBugNo() + " " + bug.getTitle());
        }
        if (request.getCaseSuiteId() != null && !Objects.equals(bug.getCaseSuiteId(), request.getCaseSuiteId())) {
            bugHistoryService.record(bugId, "修改关联用例集", "caseSuiteId", String.valueOf(bug.getCaseSuiteId()), String.valueOf(request.getCaseSuiteId()), operatorId);
            bug.setCaseSuiteId(request.getCaseSuiteId());
        }
        if (request.getStatus() != null && !Objects.equals(bug.getStatus(), request.getStatus())) {
            validateStatusPermission(request.getStatus());
            bugHistoryService.record(bugId, "修改状态", "status", bug.getStatus(), request.getStatus(), operatorId);
            bug.setStatus(request.getStatus());
            notificationService.create(bug.getReporterId(), operatorId, bugId, "Bug 状态变更", bug.getBugNo() + " 已变更为 " + bug.getStatus());
            notificationService.create(bug.getAssigneeId(), operatorId, bugId, "Bug 状态变更", bug.getBugNo() + " 已变更为 " + bug.getStatus());
        }
        bugMapper.updateById(bug);
        return detail(bugId);
    }

    @Transactional
    public BugAttachmentResponse uploadImage(Long bugId, MultipartFile file) {
        Bug bug = getRequiredBug(bugId);
        validateImage(file);
        FileObject fileObject = fileObjectService.createFromStored(localStorageService.store(file, "bug/image"), FileObjectService.KIND_BUG_IMAGE);
        BugAttachment attachment = new BugAttachment();
        attachment.setBugId(bugId);
        attachment.setFileId(fileObject.getId());
        attachment.setAttachmentType("IMAGE");
        attachment.setSortOrder(attachmentCount(bugId));
        attachment.setCreatedBy(CurrentUserContext.getUserId());
        bugAttachmentMapper.insert(attachment);
        bugHistoryService.record(bugId, "上传图片", "fileId", null, String.valueOf(fileObject.getId()), CurrentUserContext.getUserId());
        notificationService.create(bug.getReporterId(), CurrentUserContext.getUserId(), bugId, "Bug 上传了图片", fileObject.getOriginalName());
        return BugAttachmentResponse.from(attachment);
    }

    @Transactional
    public BugCommentResponse addComment(Long bugId, BugCommentCreateRequest request) {
        Bug bug = getRequiredBug(bugId);
        BugComment comment = new BugComment();
        comment.setBugId(bugId);
        comment.setContent(request.getContent());
        comment.setCreatedBy(CurrentUserContext.getUserId());
        bugCommentMapper.insert(comment);
        bugHistoryService.record(bugId, "新增评论", "content", null, request.getContent(), CurrentUserContext.getUserId());
        notificationService.create(bug.getReporterId(), CurrentUserContext.getUserId(), bugId, "Bug 有新评论", request.getContent());
        notificationService.create(bug.getAssigneeId(), CurrentUserContext.getUserId(), bugId, "Bug 有新评论", request.getContent());
        return BugCommentResponse.from(comment);
    }

    public List<BugAttachmentResponse> attachments(Long bugId) {
        return bugAttachmentMapper.selectList(new LambdaQueryWrapper<BugAttachment>()
                .eq(BugAttachment::getBugId, bugId)
                .orderByAsc(BugAttachment::getSortOrder))
            .stream().map(BugAttachmentResponse::from).collect(Collectors.toList());
    }

    public List<BugCommentResponse> comments(Long bugId) {
        return bugCommentMapper.selectList(new LambdaQueryWrapper<BugComment>()
                .eq(BugComment::getBugId, bugId)
                .orderByAsc(BugComment::getCreatedAt))
            .stream().map(BugCommentResponse::from).collect(Collectors.toList());
    }

    public List<BugHistoryResponse> histories(Long bugId) {
        return bugHistoryMapper.selectList(new LambdaQueryWrapper<BugHistory>()
                .eq(BugHistory::getBugId, bugId)
                .orderByAsc(BugHistory::getCreatedAt))
            .stream().map(BugHistoryResponse::from).collect(Collectors.toList());
    }

    private Bug getRequiredBug(Long bugId) {
        Bug bug = bugMapper.selectById(bugId);
        if (bug == null) {
            throw new BusinessException("BUG_NOT_FOUND", "Bug 不存在");
        }
        if (!userService.canViewAllData()
                && !CurrentUserContext.getUserId().equals(bug.getReporterId())
                && !CurrentUserContext.getUserId().equals(bug.getAssigneeId())) {
            throw new BusinessException("PERMISSION_DENIED", "无权限访问该 Bug");
        }
        return bug;
    }

    private String nextBugNo(Long projectId) {
        Long count = bugMapper.selectCount(new LambdaQueryWrapper<Bug>().eq(Bug::getProjectId, projectId));
        return String.format("BUG-%04d", count + 1);
    }

    private Integer attachmentCount(Long bugId) {
        return bugAttachmentMapper.selectCount(new LambdaQueryWrapper<BugAttachment>().eq(BugAttachment::getBugId, bugId)).intValue();
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    private void change(Bug bug, String label, String oldValue, String newValue, Long operatorId) {
        if (newValue == null || Objects.equals(oldValue, newValue)) {
            return;
        }
        bugHistoryService.record(bug.getId(), "修改" + label, label, oldValue, newValue, operatorId);
        if ("标题".equals(label)) bug.setTitle(newValue);
        if ("描述".equals(label)) bug.setDescription(newValue);
        if ("严重级别".equals(label)) bug.setSeverity(newValue);
        if ("优先级".equals(label)) bug.setPriority(newValue);
    }

    private void validateStatusPermission(String status) {
        if (!STATUSES.contains(status)) {
            throw new BusinessException("INVALID_BUG_STATUS", "Bug 状态不合法");
        }
        if ("已修复".equals(status) && !CurrentUserContext.hasPermission("BUG_STATUS_FIXED")) {
            throw new BusinessException("PERMISSION_DENIED", "只有开发或产品可以提交已修复");
        }
        if (("待修复".equals(status) || "修复完成".equals(status)) && !CurrentUserContext.hasPermission("BUG_STATUS_TEST")) {
            throw new BusinessException("PERMISSION_DENIED", "只有测试可以提交待修复或修复完成");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("IMAGE_TOO_LARGE", "单张图片不能超过 10MB");
        }
        String contentType = file.getContentType();
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean imageContent = contentType != null && contentType.startsWith("image/");
        boolean imageName = name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".webp");
        if (!imageContent || !imageName) {
            throw new BusinessException("INVALID_IMAGE", "只支持 png、jpg、jpeg、gif、webp 图片");
        }
    }
}
