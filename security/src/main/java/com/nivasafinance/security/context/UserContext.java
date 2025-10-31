package com.nivasafinance.security.context;

import com.nivasafinance.security.model.UserInfo;

public class UserContext {
    private static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    private UserContext() {
        // Private constructor to prevent instantiation
    }

    public static void setUserInfo(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}

