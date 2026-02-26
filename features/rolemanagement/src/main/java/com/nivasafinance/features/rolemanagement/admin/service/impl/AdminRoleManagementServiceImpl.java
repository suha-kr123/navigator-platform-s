package com.nivasafinance.features.rolemanagement.admin.service.impl;

import com.nivasafinance.features.rolemanagement.admin.service.AdminRoleManagementService;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import com.nivasafinance.features.rolemanagement.permission.repository.PermissionReadRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permission.repository.PermissionRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupWithPermissionsResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.UpdateRolePermissionGroupsRequest;
import com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup;
import com.nivasafinance.features.rolemanagement.permissiongroup.repository.PermissionGroupRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping;
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.PermissionGroupMappingRepository;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepository;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionMappingRepository;
import com.nivasafinance.features.rolemanagement.permission.dto.UpdateRolePermissionsRequest;
import com.nivasafinance.features.rolemanagement.role.dto.CreateRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.RoleResponse;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionsUpdateResponse;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionGroupsUpdateResponse;
import com.nivasafinance.features.rolemanagement.role.entity.Role;
import com.nivasafinance.features.rolemanagement.role.repository.RoleRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoleManagementServiceImpl implements AdminRoleManagementService {
    private final PermissionRepositoryWrapper permissionRepositoryWrapper;
    private final PermissionGroupRepositoryWrapper permissionGroupRepositoryWrapper;
    private final PermissionReadRepositoryWrapper permissionReadRepositoryWrapper;
    private final RoleRepositoryWrapper roleRepositoryWrapper;
    private final RolePermissionMappingRepository rolePermissionMappingRepository;
    private final RolePermissionGroupMappingRepository rolePermissionGroupMappingRepository;
    private final PermissionGroupMappingRepository permissionGroupMappingRepository;

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepositoryWrapper.findAll().stream().map(PermissionResponse::from).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<PermissionGroupResponse> getAllPermissionGroups() {
        List<PermissionGroup> groups = permissionGroupRepositoryWrapper.findAll();
        return groups.stream().map(g -> PermissionGroupResponse.builder().id(g.getId()).name(g.getName()).build()).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<PermissionResponse> getPermissions(PaginationRequest pagination, String q) {
        int limit = Math.max(1, pagination.getLimit());
        int pageNumber = Math.max(0, pagination.getOffset() / limit);
        Sort.Direction dir = Sort.Direction.fromOptionalString(pagination.getSortDirection()).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(dir, pagination.getSortBy());
        Pageable pageable = PageRequest.of(pageNumber, limit, sort);
        Page<Permission> page;
        if (q != null && !q.isBlank()) {
            page = permissionRepositoryWrapper.findByNameContainingIgnoreCase(q.trim(), pageable);
        } else {
            page = permissionRepositoryWrapper.findAll(pageable);
        }
        List<PermissionResponse> content = page.getContent().stream().map(PermissionResponse::from).filter(Objects::nonNull).collect(Collectors.toList());
        PaginationInfo info = PaginationInfo.builder()
                .offset(pagination.getOffset())
                .limit(limit)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        return PaginatedResponse.<PermissionResponse>builder()
                .content(content)
                .pagination(info)
                .build();
    }

    @Override
    public List<PermissionGroupWithPermissionsResponse> getAllPermissionGroupsWithPermissions() {
        List<PermissionGroup> groups = permissionGroupRepositoryWrapper.findAll();
        List<Long> groupIds = groups.stream().map(PermissionGroup::getId).collect(Collectors.toList());
        List<com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping> mappings = permissionGroupMappingRepository.findByPermissionGroupIdIn(groupIds);
        Map<Long, List<Long>> groupToPermissionIds = mappings.stream().collect(Collectors.groupingBy(
                com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping::getPermissionGroupId,
                Collectors.mapping(com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping::getPermissionId, Collectors.toList())
        ));
        Set<Long> allPermissionIds = mappings.stream().map(com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping::getPermissionId).collect(Collectors.toSet());
        Map<Long, PermissionResponse> permMap = permissionRepositoryWrapper.findByIdIn(new ArrayList<>(allPermissionIds)).stream()
                .map(PermissionResponse::from).filter(Objects::nonNull).collect(Collectors.toMap(PermissionResponse::getId, p -> p));
        List<PermissionGroupWithPermissionsResponse> responses = new ArrayList<>();
        for (PermissionGroup group : groups) {
            List<Long> ids = groupToPermissionIds.getOrDefault(group.getId(), Collections.emptyList());
            List<PermissionResponse> perms = ids.stream().map(permMap::get).filter(Objects::nonNull).collect(Collectors.toList());
            responses.add(PermissionGroupWithPermissionsResponse.builder().id(group.getId()).name(group.getName()).permissions(perms).build());
        }
        return responses;
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepositoryWrapper.findAll();
        return roles.stream().map(r -> RoleResponse.builder().id(r.getId()).name(r.getName()).build()).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<PermissionGroupResponse> getPermissionGroups(PaginationRequest pagination, String q) {
        int limit = Math.max(1, pagination.getLimit());
        int pageNumber = Math.max(0, pagination.getOffset() / limit);
        Sort.Direction dir = Sort.Direction.fromOptionalString(pagination.getSortDirection()).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(dir, pagination.getSortBy());
        Pageable pageable = PageRequest.of(pageNumber, limit, sort);
        Page<PermissionGroup> page;
        if (q != null && !q.isBlank()) {
            page = permissionGroupRepositoryWrapper.findByNameContainingIgnoreCase(q.trim(), pageable);
        } else {
            page = permissionGroupRepositoryWrapper.findAll(pageable);
        }
        List<PermissionGroupResponse> content = page.getContent().stream()
                .map(g -> PermissionGroupResponse.builder().id(g.getId()).name(g.getName()).build())
                .collect(Collectors.toList());
        PaginationInfo info = PaginationInfo.builder()
                .offset(pagination.getOffset())
                .limit(limit)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        return PaginatedResponse.<PermissionGroupResponse>builder()
                .content(content)
                .pagination(info)
                .build();
    }

    @Override
    public PaginatedResponse<RoleResponse> getRoles(PaginationRequest pagination, String q) {
        int limit = Math.max(1, pagination.getLimit());
        int pageNumber = Math.max(0, pagination.getOffset() / limit);
        Sort.Direction dir = Sort.Direction.fromOptionalString(pagination.getSortDirection()).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(dir, pagination.getSortBy());
        Pageable pageable = PageRequest.of(pageNumber, limit, sort);
        Page<Role> page;
        if (q != null && !q.isBlank()) {
            page = roleRepositoryWrapper.findByNameContainingIgnoreCase(q.trim(), pageable);
        } else {
            page = roleRepositoryWrapper.findAll(pageable);
        }
        List<RoleResponse> content = page.getContent().stream()
                .map(r -> RoleResponse.builder().id(r.getId()).name(r.getName()).build())
                .collect(Collectors.toList());
        PaginationInfo info = PaginationInfo.builder()
                .offset(pagination.getOffset())
                .limit(limit)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        return PaginatedResponse.<RoleResponse>builder()
                .content(content)
                .pagination(info)
                .build();
    }

    @Override
    public List<PermissionResponse> getRolePermissions(String role) {
        return permissionReadRepositoryWrapper.findPermissionsByRoleNames(Collections.singletonList(role)).stream()
                .map(PermissionResponse::from).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<PermissionGroupResponse> getRolePermissionGroups(String role) {
        List<RolePermissionGroupMapping> mappings = rolePermissionGroupMappingRepository.findByRole(role);
        List<Long> groupIds = mappings.stream().map(RolePermissionGroupMapping::getPermissionGroupId).distinct().collect(Collectors.toList());
        return groupIds.stream().map(permissionGroupRepositoryWrapper::findById).filter(Objects::nonNull)
                .map(g -> PermissionGroupResponse.builder().id(g.getId()).name(g.getName()).build()).collect(Collectors.toList());
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        String name = request.getName();
        Optional<Role> existing = roleRepositoryWrapper.findByNameIgnoreCase(name);
        Role saved = existing.orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            return roleRepositoryWrapper.save(r);
        });
        return RoleResponse.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @Override
    public RolePermissionsUpdateResponse addPermissionsToRole(String role, UpdateRolePermissionsRequest request) {
        int created = 0;
        java.util.List<Long> addedIds = new java.util.ArrayList<>();
        for (Long permId : request.getPermissionIds()) {
            RolePermissionMapping rpm = new RolePermissionMapping();
            rpm.setRole(role);
            rpm.setPermissionId(permId);
            try {
                rolePermissionMappingRepository.save(rpm);
                created++;
                addedIds.add(permId);
            } catch (DataIntegrityViolationException ignored) {}
        }
        return RolePermissionsUpdateResponse.builder()
                .role(role)
                .requestedCount(request.getPermissionIds() != null ? request.getPermissionIds().size() : 0)
                .addedCount(created)
                .addedPermissionIds(addedIds)
                .build();
    }

    @Override
    @Transactional
    public void removePermissionFromRole(String role, Long permissionId) {
        rolePermissionMappingRepository.deleteByRoleAndPermissionId(role, permissionId);
    }

    @Override
    public RolePermissionGroupsUpdateResponse addPermissionGroupsToRole(String role, UpdateRolePermissionGroupsRequest request) {
        int created = 0;
        java.util.List<Long> addedIds = new java.util.ArrayList<>();
        for (Long groupId : request.getPermissionGroupIds()) {
            RolePermissionGroupMapping rpgm = new RolePermissionGroupMapping();
            rpgm.setRole(role);
            rpgm.setPermissionGroupId(groupId);
            try {
                rolePermissionGroupMappingRepository.save(rpgm);
                created++;
                addedIds.add(groupId);
            } catch (DataIntegrityViolationException ignored) {}
        }
        return RolePermissionGroupsUpdateResponse.builder()
                .role(role)
                .requestedCount(request.getPermissionGroupIds() != null ? request.getPermissionGroupIds().size() : 0)
                .addedCount(created)
                .addedPermissionGroupIds(addedIds)
                .build();
    }

    @Override
    @Transactional
    public void removePermissionGroupFromRole(String role, Long groupId) {
        rolePermissionGroupMappingRepository.deleteByRoleAndPermissionGroupId(role, groupId);
    }
}
