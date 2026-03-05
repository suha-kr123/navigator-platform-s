package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.dto.OfficeTreeNodeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.repository.OfficeRepositoryWrapper;
import com.nivasafinance.features.offices.service.OfficeReadService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OfficeReadServiceImpl implements OfficeReadService {

    private final OfficeRepository officeRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final MessageSource messageSource;

    @Override
    @org.springframework.cache.annotation.Cacheable(cacheNames = "offices", key = "#key")
    public OfficeResponse getOfficeByKey(String key) {
        Office entity = findOfficeByKey(key);
        return toResponse(entity);
    }

    @Override
    public List<OfficeResponse> getOfficeByKeys(List<String> keys) {
       return keys.stream().map(this::findOfficeByKey).map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<OfficeResponse> getOffices(String parentKey, String nameQuery, Boolean activeOnly, PaginationRequest paginationRequest) {
        String parentCodePrefix = null;
        if (StringUtils.hasText(parentKey)) {
            Office parentOffice = findOfficeByKey(parentKey);
            parentCodePrefix = parentOffice.getCode();
        }

        PaginatedResponse<Office> paginatedOffices = officeRepositoryWrapper.findOffices(
                parentCodePrefix,
                nameQuery,
                activeOnly,
                paginationRequest
        );

        return new PaginatedResponse<>(
                paginatedOffices.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()),
                paginatedOffices.getPagination()
        );
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(cacheNames = "officesByCodePrefix", key = "#codePrefix != null ? #codePrefix : 'null'")
    public List<OfficeResponse> getOfficesByCodePrefix(String codePrefix) {
        List<Office> offices = officeRepositoryWrapper.findAllByCodePrefix(codePrefix);
        return offices.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OfficeTreeNodeResponse> getOfficeTree(String parentKey, String search) {
        if (StringUtils.hasText(search)) {
            List<Office> matches = officeRepositoryWrapper.findAllByNameContaining(search);
            if (matches.isEmpty()) {
                return List.of();
            }
            Map<Long, Office> byId = buildOfficeMapWithAncestors(matches);
            List<OfficeTreeNodeResponse> nodes = matches.stream()
                    .map(o -> toTreeNodeWithPath(o, byId))
                    .collect(Collectors.toList());
            Map<Long, Integer> childCounts = officeRepositoryWrapper.countChildrenByParentIds(
                    matches.stream().map(Office::getId).collect(Collectors.toList())
            );
            for (OfficeTreeNodeResponse node : nodes) {
                Integer cnt = childCounts.getOrDefault(node.getId(), 0);
                node.setChildCount(cnt);
            }
            return nodes;
        }
        Long parentId = null;
        if (StringUtils.hasText(parentKey)) {
            Office parent = findOfficeByKey(parentKey);
            parentId = parent.getId();
        }
        List<Office> offices = officeRepositoryWrapper.findAllByParentId(parentId);
        List<OfficeTreeNodeResponse> nodes = offices.stream()
                .map(OfficeTreeNodeResponse::forTree)
                .collect(Collectors.toList());
        Map<Long, Integer> childCounts = officeRepositoryWrapper.countChildrenByParentIds(
                offices.stream().map(Office::getId).collect(Collectors.toList())
        );
        for (OfficeTreeNodeResponse node : nodes) {
            Integer cnt = childCounts.getOrDefault(node.getId(), 0);
            node.setChildCount(cnt);
        }
        return nodes;
    }

    private Map<Long, Office> buildOfficeMapWithAncestors(List<Office> matches) {
        Map<Long, Office> byId = new HashMap<>();
        for (Office m : matches) {
            byId.put(m.getId(), m);
        }
        Set<Long> toLoad = new HashSet<>();
        for (Office m : matches) {
            if (m.getParentId() != null && !byId.containsKey(m.getParentId())) {
                toLoad.add(m.getParentId());
            }
        }
        while (!toLoad.isEmpty()) {
            List<Office> loaded = officeRepository.findAllById(toLoad);
            for (Office o : loaded) {
                byId.put(o.getId(), o);
            }
            toLoad.clear();
            for (Office o : loaded) {
                if (o.getParentId() != null && !byId.containsKey(o.getParentId())) {
                    toLoad.add(o.getParentId());
                }
            }
        }
        return byId;
    }

    private OfficeTreeNodeResponse toTreeNodeWithPath(Office office, Map<Long, Office> byId) {
        List<String> pathNames = new ArrayList<>();
        List<String> pathKeys = new ArrayList<>();
        Office current = office;
        while (current != null) {
            pathNames.add(0, current.getName());
            pathKeys.add(0, current.getKey());
            current = current.getParentId() != null ? byId.get(current.getParentId()) : null;
        }
        return OfficeTreeNodeResponse.forSearch(office, pathNames, pathKeys);
    }

    private Office findOfficeByKey(String key) {
        return officeRepository.findByKey(key).orElseThrow(() ->
                new OfficeNotFoundException(key, messageSource)
        );
    }

    private OfficeResponse toResponse(Office entity) {
        return new OfficeResponse(
                entity.getId(),
                entity.getName(),
                entity.getKey(),
                entity.getCode(),
                entity.getAddressData(),
                entity.getParentId(),
                entity.getIsActive()
        );
    }

}
