package com.mm.Extension

import io.vertx.core.json.Json
import io.vertx.ext.sql.ResultSet
import io.vertx.kotlin.core.json.get
import java.text.FieldPosition

inline fun <reified T> ResultSet.getObject() : T? {
    return if (this.results.isNotEmpty()) {
        this.results[0].getString(0).fromJson()
    } else {
        null
    }
}

inline fun <reified T> ResultSet.getObjects() : List<T> {
    val objs = ArrayList<T>()
    if (this.results.isNotEmpty()) {
        for (result in results) {
            objs.add(result.getString(0).fromJson())
        }
    }
    return objs
}

fun <T> ResultSet.get(position: Int) : T? {
    return if (this.results.isNotEmpty()) {
        this.results[0].get(position)
    } else {
        null
    }
}