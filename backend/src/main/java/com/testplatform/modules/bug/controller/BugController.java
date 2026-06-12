package com.testplatform.modules.bug.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.bug.dto.BugAttachmentResponse;
import com.testplatform.modules.bug.dto.BugCommentCreateRequest;
import com.testplatform.modules.bug.dto.BugCommentResponse;
import com.testplatform.modules.bug.dto.BugCreateRequest;
import com.testplatform.modules.bug.dto.BugDetailResponse;
import com.testplatform.modules.bug.dto.BugHistoryResponse;
import com.testplatform.modules.bug.dto.BugListItemResponse;
import com.testplatform.modules.bug.dto.BugUpdateRequest;
import com.testplatform.modules.bug.service.BugService;

@RestController
@RequestMapping("/api")
public class BugController {

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping("/bugs")
    public ApiResponse<List<BugListItemResponse>> list(@RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(bugService.list(projectId, requirementId, status, severity, assigneeId, keyword));
    }

    @PostMapping("/requirements/{requirementId}/bugs")
    public ApiResponse<BugDetailResponse> create(@PathVariable Long requirementId, @RequestBody BugCreateRequest request) {
        return ApiResponse.ok(bugService.create(requirementId, request));
    }

    @GetMapping("/bugs/{bugId}")
    public ApiResponse<BugDetailResponse> detail(@PathVariable Long bugId) {
        return ApiResponse.ok(bugService.detail(bugId));
    }

    @PutMapping("/bugs/{bugId}")
    public ApiResponse<BugDetailResponse> update(@PathVariable Long bugId, @RequestBody BugUpdateRequest request) {
        return ApiResponse.ok(bugService.update(bugId, request));
    }

    @PostMapping("/bugs/{bugId}/images")
    public ApiResponse<BugAttachmentResponse> uploadImage(@PathVariable Long bugId, @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(bugService.uploadImage(bugId, file));
    }

    @GetMapping("/bugs/{bugId}/attachments")
    public ApiResponse<List<BugAttachmentResponse>> attachments(@PathVariable Long bugId) {
        return ApiResponse.ok(bugService.attachments(bugId));
    }

    @PostMapping("/bugs/{bugId}/comments")
    public ApiResponse<BugCommentResponse> addComment(@PathVariable Long bugId, @RequestBody BugCommentCreateRequest request) {
        return ApiResponse.ok(bugService.addComment(bugId, request));
    }

    @GetMapping("/bugs/{bugId}/comments")
    public ApiResponse<List<BugCommentResponse>> comments(@PathVariable Long bugId) {
        return ApiResponse.ok(bugService.comments(bugId));
    }

    @GetMapping("/bugs/{bugId}/history")
    public ApiResponse<List<BugHistoryResponse>> histories(@PathVariable Long bugId) {
        return ApiResponse.ok(bugService.histories(bugId));
    }
}
