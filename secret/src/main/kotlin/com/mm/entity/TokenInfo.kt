package com.mm.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenInfo(
        val mtoken: String = "",
        val expiryTime: Long = 0,
        val uuid: Long = 0,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)var isInvalid: Boolean = true
)