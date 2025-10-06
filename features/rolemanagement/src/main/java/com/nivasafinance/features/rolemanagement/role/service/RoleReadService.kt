package com.nivasafinance.features.rolemanagement.role.service

import com.nivasafinance.features.rolemanagement.role.dto.RoleResponse

interface RoleService {

    fun getRolesByUser(username : String): List<RoleResponse>
}


