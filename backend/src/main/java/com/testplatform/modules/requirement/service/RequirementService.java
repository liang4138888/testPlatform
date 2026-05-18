package com.testplatform.modules.requirement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.project.service.ProjectService;
import com.testplatform.modules.requirement.dto.RequirementCreateRequest;
import com.testplatform.modules.requirement.dto.RequirementResponse;
import com.testplatform.modules.requirement.entity.Requirement;
import com.testplatform.modules.requirement.mapper.RequirementMapper;

@Service
public class RequirementService {

    private final RequirementMapper requirementMapper;
    private final ProjectService projectService;

    public RequirementService(RequirementMapper requirementMapper, ProjectService projectService) {
        this.requirementMapper = requirementMapper;
        this.projectService = projectService;
    }

    public Requirement getRequiredRequirement(Long requirementId) {
        Requirement requirement = requirementMapper.selectById(requirementId);
        if (requirement == null) {
            throw new BusinessException("REQUIREMENT_NOT_FOUND", "需求不存在");
        }
        return requirement;
    }

    public List<RequirementResponse> listRequirements(Long projectId) {
        projectService.getRequiredProject(projectId);
        return requirementMapper.selectList(new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getProjectId, projectId)
                .orderByDesc(Requirement::getUpdatedAt))
            .stream()
            .map(RequirementResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public RequirementResponse createRequirement(Long projectId, RequirementCreateRequest request) {
        projectService.getRequiredProject(projectId);
        Long exists = requirementMapper.selectCount(new LambdaQueryWrapper<Requirement>()
            .eq(Requirement::getProjectId, projectId)
            .eq(Requirement::getRequirementNo, request.getRequirementNo()));
        if (exists > 0) {
            throw new BusinessException("REQUIREMENT_NO_EXISTS", "需求编号已存在");
        }

        Requirement requirement = new Requirement();
        requirement.setProjectId(projectId);
        requirement.setRequirementNo(request.getRequirementNo());
        requirement.setName(request.getName());
        requirement.setDescription(request.getDescription());
        requirementMapper.insert(requirement);
        return RequirementResponse.from(requirementMapper.selectById(requirement.getId()));
    }
}
