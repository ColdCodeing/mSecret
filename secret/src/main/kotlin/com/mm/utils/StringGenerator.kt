package com.mm.utils

import java.util.*

fun generateToken() : String {
    return UUID.randomUUID().toString().replace("-", "")
}