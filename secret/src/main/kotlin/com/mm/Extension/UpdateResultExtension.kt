package com.mm.Extension

import io.vertx.ext.sql.UpdateResult

fun UpdateResult.isSuccessed() : Boolean {
    return this.updated > 0
}

fun UpdateResult.getOutput() : Int {
    return this.keys.getInteger(0)
}