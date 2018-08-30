package com.mm.utils

import java.util.*
import java.text.SimpleDateFormat



fun generateToken() : String {
    return UUID.randomUUID().toString().replace("-", "")
}

fun generateActiveCode() : String {
    return UUID.randomUUID().toString().replace("-", "").substring(0,8)
}

fun generateUserId() : String {
    return SimpleDateFormat("yyyyMMdd").format(Date()) +
            UUID.randomUUID().toString().replace("-", "").substring(0,16)
}