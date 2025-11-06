package com.nivasafinance.features.rolemanagement.annotation;

import com.nivasafinance.features.rolemanagement.enums.ActionEnum;
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum;
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    ActionEnum action();
    ModuleEnum module();
    OperationsEnum operation();
}

