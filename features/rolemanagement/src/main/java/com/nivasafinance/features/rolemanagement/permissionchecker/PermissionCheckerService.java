package com.nivasafinance.features.rolemanagement.permissionchecker;

import com.nivasafinance.features.rolemanagement.enums.ActionEnum;
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum;
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum;

public interface PermissionCheckerService {
    boolean checkPermissionForUser(
            String username,
            ActionEnum action,
            ModuleEnum module,
            OperationsEnum operation
    );
}

