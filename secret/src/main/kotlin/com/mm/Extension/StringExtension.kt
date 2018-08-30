package com.mm.Extension

import java.util.regex.Pattern
const val check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"
val regex = Pattern.compile(check)

fun String.isEmail() : Boolean {
    val matcher = regex.matcher(this)
    return matcher.matches()
}