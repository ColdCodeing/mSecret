package com.paratera.gpauth.utils

import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import kotlin.coroutines.experimental.suspendCoroutine


suspend fun SQLClient.getConnection(): SQLConnection = suspendCoroutine { cont ->
    this.getConnection { conn ->
        if (conn.succeeded()) {
            cont.resume(conn.result())
        } else {
            cont.resumeWithException(conn.cause())
        }
    }
}

suspend fun SQLClient.query(sql: String): ResultSet = suspendCoroutine { cont ->
    this.query(sql) { conn ->
        if (conn.succeeded()) {
            cont.resume(conn.result())
        } else {
            cont.resumeWithException(conn.cause())
        }
    }
}

suspend fun SQLClient.queryWithParams(sql: String, args: JsonArray): ResultSet = suspendCoroutine { cont ->
    this.queryWithParams(sql, args) { conn ->
        if (conn.succeeded()) {
            cont.resume(conn.result())
        } else {
            cont.resumeWithException(conn.cause())
        }
    }
}

suspend fun SQLClient.update(sql: String): UpdateResult = suspendCoroutine { cont ->
    this.update(sql) { conn ->
        if (conn.succeeded()) {
            cont.resume(conn.result())
        } else {
            cont.resumeWithException(conn.cause())
        }
    }
}

suspend fun SQLClient.updateWithParams(sql: String, args: JsonArray): UpdateResult = suspendCoroutine { cont ->
    this.updateWithParams(sql, args) { conn ->
        if (conn.succeeded()) {
            cont.resume(conn.result())
        } else {
            cont.resumeWithException(conn.cause())
        }
    }
}