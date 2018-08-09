package com.mm.Extension

import io.vertx.core.json.Json

fun Any.toJson() : String {
    return Json.encode(this)
}