package com.mm.Extension

import io.vertx.core.json.Json

fun Any.toJson() : String {
    return Json.encode(this)
}

inline fun <reified T> String.fromJson() : T {
    return Json.decodeValue(this, T::class.java)
}