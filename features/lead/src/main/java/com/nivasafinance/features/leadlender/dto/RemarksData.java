package com.nivasafinance.features.leadlender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemarksData {
    
    private Map<String, List<String>> remarks = new HashMap<>();
    
    // Helper method to add a remark for a specific status
    public void addRemark(String status, String remarkKey) {
        remarks.computeIfAbsent(status, k -> new ArrayList<>()).add(remarkKey);
    }
    
    // Helper method to get the latest remark for a specific status
    public String getLatestRemark(String status) {
        List<String> statusRemarks = remarks.get(status);
        if (statusRemarks != null && !statusRemarks.isEmpty()) {
            return statusRemarks.get(statusRemarks.size() - 1);
        }
        return null;
    }
}

