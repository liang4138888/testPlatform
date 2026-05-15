package com.testplatform.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.BusinessException;
import com.testplatform.project.dto.ProjectCreateRequest;
import com.testplatform.project.dto.ProjectResponse;
import com.testplatform.project.entity.Project;
import com.testplatform.project.mapper.ProjectMapper;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public List<ProjectResponse> listProjects() {
        return projectMapper.selectList(new LambdaQueryWrapper<Project>().orderByDesc(Project::getUpdatedAt))
            .stream()
            .map(ProjectResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        Long exists = projectMapper.selectCount(new LambdaQueryWrapper<Project>().eq(Project::getName, request.getName()));
        if (exists > 0) {
            throw new BusinessException("PROJECT_NAME_EXISTS", "项目名称已存在");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectMapper.insert(project);
        return ProjectResponse.from(projectMapper.selectById(project.getId()));
    }

    public Project getRequiredProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("PROJECT_NOT_FOUND", "项目不存在");
        }
        return project;
    }
}
