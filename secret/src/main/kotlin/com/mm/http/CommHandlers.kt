package com.mm.http

import com.github.mauricio.async.db.exceptions.DatabaseException
import com.mm.Const.DATABASE_ERROR
import com.mm.Const.UNKNOW_ERROR
import com.mm.Extension.responseJson
import com.mm.exception.AppRuntimeException
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonObject
import org.slf4j.LoggerFactory

val LOGGER = LoggerFactory.getLogger(HttpVerticle::class.java)

fun failHandler(ctx: RoutingContext){
    val throwable = ctx.failure()
    if (throwable != null) {
        throwable.printStackTrace()
        LOGGER.error(throwable.message, throwable)
        if (throwable is AppRuntimeException) {
            ctx.responseJson(500, JsonObject().put("code", throwable.code).put("msg", throwable.message))
        } else if (throwable is DatabaseException) {
            ctx.responseJson(500, JsonObject(Pair("code", DATABASE_ERROR),
                    Pair("stack trace", throwable.message)))
        } else {
            ctx.responseJson(500, JsonObject(Pair("code", UNKNOW_ERROR),Pair("msg", throwable.message)))
        }
    } else {
        LOGGER.error("UNKNOW EXCEPTION WHERE DOING", ctx.data().toString())
        ctx.responseJson(500, JsonObject().put("code", UNKNOW_ERROR).put("msg", "no abnormality was captured"))
    }
}