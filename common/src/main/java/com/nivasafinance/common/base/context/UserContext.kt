package com.nivasafinance.common.base.context

import com.nivasafinance.common.base.model.UserInfo

object UserContext {
    private val threadLocal = ThreadLocal<UserInfo>()

    fun setUserInfo(userInfo: UserInfo) {
        threadLocal.set(userInfo)
    }

    fun getUserInfo(): UserInfo? {
        return threadLocal.get()
    }

    fun clear() {
        threadLocal.remove()
    }
}
