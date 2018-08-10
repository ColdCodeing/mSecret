package com.mm.entity

data class UserInfo(
        val uuid: Long = 0,
        val email: String = "",
        val password: String = "",
        val active: Boolean = false,
        val sex: Int = 0,
        val registTime: Long = 0,
        val oldPassword: List<String> = ArrayList()
)