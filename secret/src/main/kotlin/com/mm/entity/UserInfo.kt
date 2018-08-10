package com.mm.entity

import com.fasterxml.jackson.annotation.JsonIgnore

data class UserInfo(
        val uuid: Long = 0,
        val email: String = "",
        @JsonIgnore val password: String = "",
        val active: Boolean = false,
        val sex: Int = 0,
        val registTime: Long = 0,
        @JsonIgnore val oldPassword: List<String> = ArrayList()
)