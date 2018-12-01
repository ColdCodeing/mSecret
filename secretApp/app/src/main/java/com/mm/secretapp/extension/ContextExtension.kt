package com.mm.secretapp.extension

import java.util.*

fun Any.getConfig(key: String, filename: String) : String {
    val pro = Properties()
    pro.load(this::class.java.getResourceAsStream("/assets/$filename"))
    return pro.getProperty(key)
}