package com.testplatform.modules.casesuite.controller;

import java.util.List;

import javax.validation.Valid;

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
import com.testplatform.modules.casesuite.dto.CaseNodesSaveRequest;
import com.testplatform.modules.casesuite.dto.CaseSuiteExportResponse;
import com.testplatform.modules.casesuite.dto.CaseSuiteListItemResponse;
import com.testplatform.modules.casesuite.dto.CaseSuiteResponse;
import com.testplatform.modules.casesuite.dto.CaseSuiteSummaryResponse;
import com.testplatform.modules.casesuite.service.CaseSuiteService;
import com.testplatform.modules.file.dto.FileObjectResponse;

@RestController
@RequestMapping("/api")
public class CaseSuiteController {

    private final CaseSuiteService caseSuiteService;

    public CaseSuiteController(CaseSuiteService caseSuiteService) {
        this.caseSuiteService = caseSuiteService;
    }

    @GetMapping("/case-suites")
    public ApiResponse<List<CaseSuiteListItemResponse>> searchCaseSuites(
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) Long requirementId
    ) {
        return ApiResponse.ok(caseSuiteService.search(projectId, requirementId));
    }

    @GetMapping("/requirements/{requirementId}/case-suites")
    public ApiResponse<List<CaseSuiteSummaryResponse>> listCaseSuites(@PathVariable Long requirementId) {
        return ApiResponse.ok(caseSuiteService.listByRequirement(requirementId));
    }

    @PostMapping("/requirements/{requirementId}/case-suites/upload")
    public ApiResponse<CaseSuiteResponse> uploadCaseSuite(
        @PathVariable Long requirementId,
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "name", required = false) String name
    ) {
        return ApiResponse.ok(caseSuiteService.upload(requirementId, file, name));
    }

    @GetMapping("/case-suites/{suiteId}")
    public ApiResponse<CaseSuiteResponse> getCaseSuite(@PathVariable Long suiteId) {
        return ApiResponse.ok(caseSuiteService.getSuiteDetail(suiteId));
    }

    @GetMapping("/case-suites/{suiteId}/files")
    public ApiResponse<List<FileObjectResponse>> listCaseSuiteFiles(@PathVariable Long suiteId) {
        return ApiResponse.ok(caseSuiteService.listSuiteFiles(suiteId));
    }

    @PutMapping("/case-suites/{suiteId}/nodes")
    public ApiResponse<CaseSuiteResponse> saveCaseNodes(
        @PathVariable Long suiteId,
        @Valid @RequestBody CaseNodesSaveRequest request
    ) {
        return ApiResponse.ok(caseSuiteService.saveNodes(suiteId, request));
    }

    @PostMapping("/case-suites/{suiteId}/export-xmind")
    public ApiResponse<CaseSuiteExportResponse> exportXmind(@PathVariable Long suiteId) {
        return ApiResponse.ok(caseSuiteService.exportXmind(suiteId));
    }
}
