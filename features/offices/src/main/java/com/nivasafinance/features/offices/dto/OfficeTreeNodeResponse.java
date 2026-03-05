package com.nivasafinance.features.offices.dto;

import com.nivasafinance.features.offices.entity.Office;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeTreeNodeResponse {
    private Long id;
    private String name;
    private String key;
    private String code;
    private Boolean isActive;
    private List<OfficeTreeNodeResponse> children = new ArrayList<>();
    /** Full path from root to this node (for search results). Null in tree mode. */
    private List<String> pathNames;
    /** Full path keys from root to this node (for search results). Null in tree mode. */
    private List<String> pathKeys;
    /** Number of direct children for this office. */
    private Integer childCount;

    public static OfficeTreeNodeResponse forTree(Office o) {
        return new OfficeTreeNodeResponse(
                o.getId(),
                o.getName(),
                o.getKey(),
                o.getCode(),
                o.getIsActive(),
                new ArrayList<>(),
                null,
                null,
                0);
    }

    public static OfficeTreeNodeResponse forSearch(Office o, List<String> pathNames, List<String> pathKeys) {
        return new OfficeTreeNodeResponse(
                o.getId(),
                o.getName(),
                o.getKey(),
                o.getCode(),
                o.getIsActive(),
                new ArrayList<>(),
                pathNames,
                pathKeys,
                0);
    }
}
