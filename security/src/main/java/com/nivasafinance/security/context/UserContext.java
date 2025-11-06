package com.nivasafinance.security.context;

import com.nivasafinance.security.model.UserInfo;

public class UserContext {
    private static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();
    private static final String SYSTEM_USERNAME = "system";

    private UserContext() {
        // Private constructor to prevent instantiation
    }

    public static void setUserInfo(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return threadLocal.get();
    }

    public static String getCurrentUsername() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUsername() : SYSTEM_USERNAME;
    }

    public static void clear() {
        threadLocal.remove();
    }
}

