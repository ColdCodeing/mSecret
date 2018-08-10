package com.mm.entity

data class TokenInfo(
        val mtoken: String = "",
        val expiryTime: Long = 0,
        val uuid: Long = 0,
        var isInvalid: Boolean = true
)