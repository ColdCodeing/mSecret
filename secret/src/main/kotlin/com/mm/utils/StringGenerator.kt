package com.mm.utils

import java.util.*

fun generateToken() : String {
    return UUID.randomUUID().toString().replace("-", "")
}

fun generateActiveCode() : String {
    return UUID.randomUUID().toString().replace("-", "").substring(0,8)
}