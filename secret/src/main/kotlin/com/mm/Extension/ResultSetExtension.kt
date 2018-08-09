package com.mm.Extension

import io.vertx.core.json.Json
import io.vertx.ext.sql.ResultSet

inline fun <reified T> ResultSet.getObject() : T? {
    return if (this.results.isNotEmpty()) {
        Json.decodeValue(this.results[0].getString(0), T::class.java)
    } else {
        null
    }
}