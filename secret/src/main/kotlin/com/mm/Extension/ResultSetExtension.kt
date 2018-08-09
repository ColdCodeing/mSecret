package com.mm.Extension

import io.vertx.core.json.Json
import io.vertx.ext.sql.ResultSet

inline fun <reified T> ResultSet.getObject() : T? {
    if (this.results.isNotEmpty()) {
        return Json.decodeValue(this.results.get(0).getString(0), T::class.java)
    } else {
        return null
    }
}