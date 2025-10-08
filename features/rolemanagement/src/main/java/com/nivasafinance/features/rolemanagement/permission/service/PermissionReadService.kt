package com.nivasafinance.features.rolemanagement.permission.service

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse

interface PermissionReadService {
    fun getPermissionsByRoles(roleName: List<String>): List<PermissionResponse>
    // interally call role permission mapping
    // internally call role permission group service
    // internall call PermissionReptository to get permissions by permisson ids (list)
}
