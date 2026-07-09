package com.testplatform.modules.organization.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.exception.BusinessException;
import com.testplatform.modules.organization.dto.OrganizationRequest;
import com.testplatform.modules.organization.dto.OrganizationResponse;
import com.testplatform.modules.organization.entity.SystemOrganization;
import com.testplatform.modules.organization.mapper.SystemOrganizationMapper;
import com.testplatform.modules.user.entity.SystemUser;
import com.testplatform.modules.user.service.UserService;

@Service
public class OrganizationService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String PERMISSION_ORGANIZATION = "MENU_ORGANIZATION";
    private static final String PERMISSION_ORGANIZATION_MANAGE = "ORGANIZATION_MANAGE";

    private final SystemOrganizationMapper organizationMapper;
    private final UserService userService;

    public OrganizationService(SystemOrganizationMapper organizationMapper, UserService userService) {
        this.organizationMapper = organizationMapper;
        this.userService = userService;
    }

    public List<OrganizationResponse> tree() {
        requireOrganizationRead();
        return buildTree(listAll());
    }

    public List<OrganizationResponse> activeOptions() {
        return organizationMapper.selectList(new LambdaQueryWrapper<SystemOrganization>()
                .eq(SystemOrganization::getStatus, STATUS_ACTIVE)
                .orderByAsc(SystemOrganization::getSortOrder)
                .orderByAsc(SystemOrganization::getId))
            .stream()
            .map(OrganizationResponse::from)
            .collect(Collectors.toList());
    }

    public SystemOrganization getActiveRequired(Long organizationId) {
        if (organizationId == null) {
            throw new BusinessException("ORGANIZATION_REQUIRED", "请选择所属组织");
        }
        SystemOrganization organization = organizationMapper.selectById(organizationId);
        if (organization == null) {
            throw new BusinessException("ORGANIZATION_NOT_FOUND", "组织不存在");
        }
        if (!STATUS_ACTIVE.equals(organization.getStatus())) {
            throw new BusinessException("ORGANIZATION_DISABLED", "组织已禁用");
        }
        return organization;
    }

    @Transactional
    public OrganizationResponse create(OrganizationRequest request) {
        userService.requirePermission(PERMISSION_ORGANIZATION_MANAGE);
        request.setLeaderUserId(null);
        validateRequest(null, request);
        SystemOrganization organization = new SystemOrganization();
        applyRequest(organization, request);
        organizationMapper.insert(organization);
        return toResponse(organization);
    }

    @Transactional
    public OrganizationResponse update(Long id, OrganizationRequest request) {
        userService.requirePermission(PERMISSION_ORGANIZATION_MANAGE);
        SystemOrganization organization = organizationMapper.selectById(id);
        if (organization == null) {
            throw new BusinessException("ORGANIZATION_NOT_FOUND", "组织不存在");
        }
        validateRequest(id, request);
        applyRequest(organization, request);
        organizationMapper.updateById(organization);
        return toResponse(organization);
    }

    private List<SystemOrganization> listAll() {
        return organizationMapper.selectList(new LambdaQueryWrapper<SystemOrganization>()
            .orderByAsc(SystemOrganization::getSortOrder)
            .orderByAsc(SystemOrganization::getId));
    }

    private List<OrganizationResponse> buildTree(List<SystemOrganization> organizations) {
        Map<Long, OrganizationResponse> responseMap = new LinkedHashMap<Long, OrganizationResponse>();
        Set<Long> leaderIds = organizations.stream()
            .map(SystemOrganization::getLeaderUserId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        Map<Long, String> leaderNames = new HashMap<Long, String>();
        for (Long leaderId : leaderIds) {
            SystemUser user = userService.getRequiredUser(leaderId);
            leaderNames.put(leaderId, user.getDisplayName());
        }
        for (SystemOrganization organization : organizations) {
            OrganizationResponse response = OrganizationResponse.from(organization);
            response.setLeaderName(leaderNames.get(organization.getLeaderUserId()));
            responseMap.put(response.getId(), response);
        }
        List<OrganizationResponse> roots = new ArrayList<OrganizationResponse>();
        for (OrganizationResponse response : responseMap.values()) {
            if (response.getParentId() == null || !responseMap.containsKey(response.getParentId())) {
                roots.add(response);
            } else {
                responseMap.get(response.getParentId()).getChildren().add(response);
            }
        }
        return roots;
    }

    private OrganizationResponse toResponse(SystemOrganization organization) {
        OrganizationResponse response = OrganizationResponse.from(organization);
        if (organization.getLeaderUserId() != null) {
            response.setLeaderName(userService.getRequiredUser(organization.getLeaderUserId()).getDisplayName());
        }
        return response;
    }

    private void requireOrganizationRead() {
        if (!userService.hasPermission(PERMISSION_ORGANIZATION) && !userService.hasPermission("USER_MANAGE")) {
            throw new BusinessException("PERMISSION_DENIED", "无权限操作");
        }
    }

    private void validateRequest(Long id, OrganizationRequest request) {
        if (request.getOrgCode() == null || !request.getOrgCode().matches("^[A-Za-z0-9_]+$")) {
            throw new BusinessException("INVALID_ORG_CODE", "组织编码只能包含英文、数字或下划线");
        }
        if (request.getOrgName() == null || request.getOrgName().trim().isEmpty()) {
            throw new BusinessException("INVALID_ORG_NAME", "组织名称不能为空");
        }
        if (!STATUS_ACTIVE.equals(request.getStatus()) && !STATUS_DISABLED.equals(request.getStatus())) {
            throw new BusinessException("INVALID_ORG_STATUS", "组织状态不合法");
        }
        SystemOrganization sameCode = organizationMapper.selectOne(new LambdaQueryWrapper<SystemOrganization>()
            .eq(SystemOrganization::getOrgCode, request.getOrgCode()));
        if (sameCode != null && (id == null || !sameCode.getId().equals(id))) {
            throw new BusinessException("ORG_CODE_EXISTS", "组织编码已存在");
        }
        if (request.getParentId() != null) {
            if (id != null && id.equals(request.getParentId())) {
                throw new BusinessException("INVALID_ORG_PARENT", "上级组织不能是自己");
            }
            if (organizationMapper.selectById(request.getParentId()) == null) {
                throw new BusinessException("ORG_PARENT_NOT_FOUND", "上级组织不存在");
            }
            if (id != null && isDescendant(id, request.getParentId())) {
                throw new BusinessException("INVALID_ORG_PARENT", "上级组织不能是自己的子级");
            }
        }
        if (request.getLeaderUserId() != null) {
            userService.getRequiredUser(request.getLeaderUserId());
        }
    }

    private boolean isDescendant(Long id, Long targetParentId) {
        Map<Long, Long> parentMap = new HashMap<Long, Long>();
        for (SystemOrganization organization : listAll()) {
            if (organization.getParentId() != null) {
                parentMap.put(organization.getId(), organization.getParentId());
            }
        }
        Long current = targetParentId;
        Set<Long> visited = new HashSet<Long>();
        while (current != null && visited.add(current)) {
            if (id.equals(current)) {
                return true;
            }
            current = parentMap.get(current);
        }
        return false;
    }

    private void applyRequest(SystemOrganization organization, OrganizationRequest request) {
        organization.setParentId(request.getParentId());
        organization.setOrgCode(request.getOrgCode());
        organization.setOrgName(request.getOrgName());
        organization.setLeaderUserId(request.getLeaderUserId());
        organization.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        organization.setStatus(request.getStatus());
    }
}
