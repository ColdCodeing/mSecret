package com.mm.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class UserInfo(
        val uuid: Long = 0,
        val email: String = "",
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) val password: String = "",
        var active: Boolean = false,
        val sex: Int = 0,
        val registTime: Long = 0,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) val oldPassword: List<String> = ArrayList()
)