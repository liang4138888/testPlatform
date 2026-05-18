package com.testplatform.modules.casesuite.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.infrastructure.storage.LocalStorageService;
import com.testplatform.infrastructure.xmind.XMindExporter;
import com.testplatform.infrastructure.xmind.XMindParser;
import com.testplatform.infrastructure.xmind.XMindTopicNode;
import com.testplatform.modules.casesuite.dto.CaseNodeResponse;
import com.testplatform.modules.casesuite.dto.CaseNodeWriteRequest;
import com.testplatform.modules.casesuite.dto.CaseNodesSaveRequest;
import com.testplatform.modules.casesuite.dto.CaseSuiteExportResponse;
import com.testplatform.modules.casesuite.dto.CaseSuiteListItemResponse;
import com.testplatform.modules.casesuite.dto.CaseSuiteResponse;
import com.testplatform.modules.casesuite.dto.CaseSuiteSummaryResponse;
import com.testplatform.modules.project.entity.Project;
import com.testplatform.modules.project.mapper.ProjectMapper;
import com.testplatform.modules.project.service.ProjectService;
import com.testplatform.modules.requirement.mapper.RequirementMapper;
import com.testplatform.modules.casesuite.entity.CaseNode;
import com.testplatform.modules.casesuite.entity.CaseSuite;
import com.testplatform.modules.casesuite.mapper.CaseNodeMapper;
import com.testplatform.modules.casesuite.mapper.CaseSuiteMapper;
import com.testplatform.modules.file.dto.FileObjectResponse;
import com.testplatform.modules.file.entity.FileObject;
import com.testplatform.modules.file.service.FileObjectService;
import com.testplatform.modules.requirement.entity.Requirement;
import com.testplatform.modules.requirement.service.RequirementService;

@Service
public class CaseSuiteService {

    private static final Set<String> NODE_TYPES = new HashSet<String>(
        Arrays.asList("module", "case", "step", "expected")
    );

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SAVED = "SAVED";

    private final CaseSuiteMapper caseSuiteMapper;
    private final CaseNodeMapper caseNodeMapper;
    private final RequirementMapper requirementMapper;
    private final ProjectMapper projectMapper;
    private final RequirementService requirementService;
    private final ProjectService projectService;
    private final FileObjectService fileObjectService;
    private final LocalStorageService localStorageService;
    private final XMindParser xMindParser;
    private final XMindExporter xMindExporter;

    public CaseSuiteService(
        CaseSuiteMapper caseSuiteMapper,
        CaseNodeMapper caseNodeMapper,
        RequirementMapper requirementMapper,
        ProjectMapper projectMapper,
        RequirementService requirementService,
        ProjectService projectService,
        FileObjectService fileObjectService,
        LocalStorageService localStorageService,
        XMindParser xMindParser,
        XMindExporter xMindExporter
    ) {
        this.caseSuiteMapper = caseSuiteMapper;
        this.caseNodeMapper = caseNodeMapper;
        this.requirementMapper = requirementMapper;
        this.projectMapper = projectMapper;
        this.requirementService = requirementService;
        this.projectService = projectService;
        this.fileObjectService = fileObjectService;
        this.localStorageService = localStorageService;
        this.xMindParser = xMindParser;
        this.xMindExporter = xMindExporter;
    }

    public List<CaseSuiteListItemResponse> search(Long projectId, Long requirementId) {
        return loadSuites(projectId, requirementId).stream()
            .map(this::toListItem)
            .collect(Collectors.toList());
    }

    public List<CaseSuiteSummaryResponse> listByRequirement(Long requirementId) {
        requirementService.getRequiredRequirement(requirementId);
        return caseSuiteMapper.selectList(new LambdaQueryWrapper<CaseSuite>()
                .eq(CaseSuite::getRequirementId, requirementId)
                .orderByDesc(CaseSuite::getUpdatedAt))
            .stream()
            .map(CaseSuiteSummaryResponse::from)
            .collect(Collectors.toList());
    }

    public CaseSuiteResponse getSuiteDetail(Long suiteId) {
        CaseSuite suite = getRequiredSuite(suiteId);
        Requirement requirement = requirementService.getRequiredRequirement(suite.getRequirementId());
        List<CaseNodeResponse> nodes = buildTree(loadActiveNodes(suiteId));
        return CaseSuiteResponse.from(suite, requirement.getRequirementNo(), requirement.getName(), nodes);
    }

    public List<FileObjectResponse> listSuiteFiles(Long suiteId) {
        CaseSuite suite = getRequiredSuite(suiteId);
        List<FileObjectResponse> files = new ArrayList<FileObjectResponse>();
        if (suite.getOriginalFileId() != null) {
            files.add(FileObjectResponse.from(fileObjectService.getRequiredFile(suite.getOriginalFileId())));
        }
        if (suite.getExportedFileId() != null) {
            files.add(FileObjectResponse.from(fileObjectService.getRequiredFile(suite.getExportedFileId())));
        }
        return files;
    }

    @Transactional
    public CaseSuiteResponse upload(Long requirementId, MultipartFile file, String suiteName) {
        Requirement requirement = requirementService.getRequiredRequirement(requirementId);
        validateXmindFile(file);

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException exception) {
            throw new BusinessException("XMIND_READ_FAILED", "读取 XMind 文件失败");
        }

        List<XMindTopicNode> parsedNodes = xMindParser.parse(new ByteArrayInputStream(fileBytes));
        String originalFilename = file.getOriginalFilename() == null ? "upload.xmind" : file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "application/vnd.xmind.workbook" : file.getContentType();
        LocalStorageService.StoredFile storedFile = localStorageService.storeBytes(
            fileBytes,
            originalFilename,
            "xmind/original",
            contentType
        );
        FileObject originalFile = fileObjectService.createFromStored(storedFile, FileObjectService.KIND_ORIGINAL);

        String resolvedName = resolveSuiteName(suiteName, file.getOriginalFilename());
        CaseSuite suite = new CaseSuite();
        suite.setRequirementId(requirementId);
        suite.setName(resolvedName);
        suite.setOriginalFileId(originalFile.getId());
        suite.setStatus(STATUS_DRAFT);
        caseSuiteMapper.insert(suite);

        persistParsedNodes(suite.getId(), null, parsedNodes, 0);
        return getSuiteDetail(suite.getId());
    }

    @Transactional
    public CaseSuiteResponse saveNodes(Long suiteId, CaseNodesSaveRequest request) {
        getRequiredSuite(suiteId);
        validateWriteNodes(request.getNodes());

        caseNodeMapper.delete(new LambdaQueryWrapper<CaseNode>().eq(CaseNode::getSuiteId, suiteId));
        persistWriteNodes(suiteId, null, request.getNodes());

        CaseSuite suite = getRequiredSuite(suiteId);
        suite.setStatus(STATUS_SAVED);
        caseSuiteMapper.updateById(suite);
        return getSuiteDetail(suiteId);
    }

    @Transactional
    public CaseSuiteExportResponse exportXmind(Long suiteId) {
        CaseSuite suite = getRequiredSuite(suiteId);
        List<CaseNode> nodes = loadActiveNodes(suiteId);
        if (nodes.isEmpty()) {
            throw new BusinessException("EMPTY_CASE_TREE", "用例树为空，无法导出");
        }

        List<XMindTopicNode> exportNodes = toExportNodes(buildTree(nodes));
        byte[] xmindBytes = xMindExporter.export(suite.getName(), exportNodes);
        String exportName = buildExportFileName(suite.getName());
        LocalStorageService.StoredFile storedFile = localStorageService.storeBytes(
            xmindBytes,
            exportName,
            "xmind/export",
            "application/vnd.xmind.workbook"
        );
        FileObject exportedFile = fileObjectService.createFromStored(storedFile, FileObjectService.KIND_EXPORTED);

        suite.setExportedFileId(exportedFile.getId());
        caseSuiteMapper.updateById(suite);
        return new CaseSuiteExportResponse(exportedFile.getId(), exportedFile.getOriginalName());
    }

    public CaseSuite getRequiredSuite(Long suiteId) {
        CaseSuite suite = caseSuiteMapper.selectById(suiteId);
        if (suite == null) {
            throw new BusinessException("CASE_SUITE_NOT_FOUND", "用例集不存在");
        }
        return suite;
    }

    private List<CaseSuite> loadSuites(Long projectId, Long requirementId) {
        if (requirementId != null) {
            requirementService.getRequiredRequirement(requirementId);
            return caseSuiteMapper.selectList(new LambdaQueryWrapper<CaseSuite>()
                .eq(CaseSuite::getRequirementId, requirementId)
                .orderByDesc(CaseSuite::getUpdatedAt));
        }
        if (projectId != null) {
            projectService.getRequiredProject(projectId);
            List<Requirement> requirements = requirementMapper.selectList(new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getProjectId, projectId));
            if (requirements.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> requirementIds = requirements.stream()
                .map(Requirement::getId)
                .collect(Collectors.toList());
            return caseSuiteMapper.selectList(new LambdaQueryWrapper<CaseSuite>()
                .in(CaseSuite::getRequirementId, requirementIds)
                .orderByDesc(CaseSuite::getUpdatedAt));
        }
        return caseSuiteMapper.selectList(new LambdaQueryWrapper<CaseSuite>().orderByDesc(CaseSuite::getUpdatedAt));
    }

    private CaseSuiteListItemResponse toListItem(CaseSuite suite) {
        Requirement requirement = requirementMapper.selectById(suite.getRequirementId());
        if (requirement == null) {
            throw new BusinessException("REQUIREMENT_NOT_FOUND", "需求不存在");
        }
        Project project = projectMapper.selectById(requirement.getProjectId());
        if (project == null) {
            throw new BusinessException("PROJECT_NOT_FOUND", "项目不存在");
        }
        return CaseSuiteListItemResponse.from(suite, requirement, project);
    }

    private List<CaseNode> loadActiveNodes(Long suiteId) {
        return caseNodeMapper.selectList(new LambdaQueryWrapper<CaseNode>()
            .eq(CaseNode::getSuiteId, suiteId)
            .orderByAsc(CaseNode::getSortOrder)
            .orderByAsc(CaseNode::getId));
    }

    private List<CaseNodeResponse> buildTree(List<CaseNode> nodes) {
        Map<Long, List<CaseNode>> childrenMap = nodes.stream()
            .filter(node -> node.getParentId() != null)
            .collect(Collectors.groupingBy(CaseNode::getParentId));

        return nodes.stream()
            .filter(node -> node.getParentId() == null)
            .map(node -> toResponse(node, childrenMap))
            .collect(Collectors.toList());
    }

    private CaseNodeResponse toResponse(CaseNode node, Map<Long, List<CaseNode>> childrenMap) {
        List<CaseNode> children = childrenMap.getOrDefault(node.getId(), Collections.emptyList());
        List<CaseNodeResponse> childResponses = children.stream()
            .map(child -> toResponse(child, childrenMap))
            .collect(Collectors.toList());
        return CaseNodeResponse.from(node, childResponses);
    }

    private void persistParsedNodes(Long suiteId, Long parentId, List<XMindTopicNode> nodes, int startOrder) {
        int order = startOrder;
        for (XMindTopicNode node : nodes) {
            CaseNode entity = new CaseNode();
            entity.setSuiteId(suiteId);
            entity.setParentId(parentId);
            entity.setNodeType(node.getNodeType());
            entity.setName(node.getName());
            entity.setDescription(node.getDescription());
            entity.setSortOrder(order++);
            caseNodeMapper.insert(entity);
            if (!node.getChildren().isEmpty()) {
                persistParsedNodes(suiteId, entity.getId(), node.getChildren(), 0);
            }
        }
    }

    private void persistWriteNodes(Long suiteId, Long parentId, List<CaseNodeWriteRequest> nodes) {
        if (nodes == null) {
            return;
        }
        int order = 0;
        for (CaseNodeWriteRequest node : nodes) {
            CaseNode entity = new CaseNode();
            entity.setSuiteId(suiteId);
            entity.setParentId(parentId);
            entity.setNodeType(node.getNodeType());
            entity.setName(node.getName().trim());
            entity.setDescription(node.getDescription());
            entity.setSortOrder(node.getSortOrder() == null ? order : node.getSortOrder());
            caseNodeMapper.insert(entity);
            order++;
            persistWriteNodes(suiteId, entity.getId(), node.getChildren());
        }
    }

    private List<XMindTopicNode> toExportNodes(List<CaseNodeResponse> nodes) {
        List<XMindTopicNode> exportNodes = new ArrayList<XMindTopicNode>();
        for (CaseNodeResponse node : nodes) {
            exportNodes.add(toExportNode(node));
        }
        return exportNodes;
    }

    private XMindTopicNode toExportNode(CaseNodeResponse node) {
        List<XMindTopicNode> children = new ArrayList<XMindTopicNode>();
        if (node.getChildren() != null) {
            for (CaseNodeResponse child : node.getChildren()) {
                children.add(toExportNode(child));
            }
        }
        return new XMindTopicNode(node.getName(), node.getNodeType(), node.getDescription(), children);
    }

    private void validateXmindFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "上传文件不能为空");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".xmind")) {
            throw new BusinessException("INVALID_FILE_TYPE", "仅支持 .xmind 文件");
        }
    }

    private void validateWriteNodes(List<CaseNodeWriteRequest> nodes) {
        if (nodes == null) {
            throw new BusinessException("EMPTY_CASE_TREE", "用例树不能为空");
        }
        for (CaseNodeWriteRequest node : nodes) {
            validateWriteNode(node);
        }
    }

    private void validateWriteNode(CaseNodeWriteRequest node) {
        if (!NODE_TYPES.contains(node.getNodeType())) {
            throw new BusinessException("INVALID_NODE_TYPE", "无效的节点类型: " + node.getNodeType());
        }
        if (node.getName() == null || node.getName().trim().isEmpty()) {
            throw new BusinessException("INVALID_NODE_NAME", "节点名称不能为空");
        }
        if (node.getChildren() != null) {
            for (CaseNodeWriteRequest child : node.getChildren()) {
                validateWriteNode(child);
            }
        }
    }

    private String resolveSuiteName(String suiteName, String originalFilename) {
        if (suiteName != null && !suiteName.trim().isEmpty()) {
            return suiteName.trim();
        }
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "未命名用例集";
        }
        String name = originalFilename.trim();
        if (name.toLowerCase().endsWith(".xmind")) {
            return name.substring(0, name.length() - 6);
        }
        return name;
    }

    private String buildExportFileName(String suiteName) {
        String safeName = suiteName == null || suiteName.trim().isEmpty() ? "case-suite" : suiteName.trim();
        return safeName + "-exported.xmind";
    }
}
