package com.testplatform.modules.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.auth.support.CurrentUserContext;
import com.testplatform.modules.project.dto.ProjectCreateRequest;
import com.testplatform.modules.project.dto.ProjectResponse;
import com.testplatform.modules.project.entity.Project;
import com.testplatform.modules.project.mapper.ProjectMapper;
import com.testplatform.modules.user.service.UserService;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final UserService userService;

    public ProjectService(ProjectMapper projectMapper, UserService userService) {
        this.projectMapper = projectMapper;
        this.userService = userService;
    }

    public List<ProjectResponse> listProjects() {
        LambdaQueryWrapper<Project> query = new LambdaQueryWrapper<Project>().orderByDesc(Project::getUpdatedAt);
        if (!userService.canViewAllData()) {
            query.eq(Project::getCreatedBy, CurrentUserContext.getUserId());
        }
        return projectMapper.selectList(query)
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
        project.setCreatedBy(CurrentUserContext.getUserId());
        projectMapper.insert(project);
        return ProjectResponse.from(projectMapper.selectById(project.getId()));
    }

    public Project getRequiredProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("PROJECT_NOT_FOUND", "项目不存在");
        }
        if (!userService.canViewAllData() && !CurrentUserContext.getUserId().equals(project.getCreatedBy())) {
            throw new BusinessException("PERMISSION_DENIED", "无权限访问该项目");
        }
        return project;
    }
}
