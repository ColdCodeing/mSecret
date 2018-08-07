@file:Suppress("UNCHECKED_CAST")

package com.mm.utils

import com.mm.http.*
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.*
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.groovy.ext.sql.SQLRowStream_GroovyExtension.handler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

inline fun <reified T> RoutingContext.getSessionVal(key: String, required: Boolean): T? {
    if (required) {
        val value: T = this.session().get<T>(key)
                ?: throw AppRuntimeException("session %s is empty".format(key), SESSION_ERROR)
        return value
    } else {
        return this.session().get<T>(key)
    }
}

inline fun <reified T> RoutingContext.getReqParam(key: String, required: Boolean): T? {
    if (required) {
        val value: String = this.request().getParam(key)
                ?: throw AppRuntimeException("request param %s is empty".format(key), REQ_PARAM_ERROR)
        return convert<T>(value)
    } else {
        val value = this.request().getParam(key)
        if (value != null) {
            return convert<T>(value)
        }
        return value
    }
}

inline fun <reified T> RoutingContext.getPathParam(key: String, required: Boolean): T {
    if (required) {
        val value: String = this.pathParam(key)
                ?: throw AppRuntimeException("path param %s is empty".format(key), REQ_PATH_PARAM_ERROR)
        return convert<T>(value)
    } else {
        val value: String = this.pathParam(key)
        if (value != null) {
            return convert<T>(value)
        }
        return value
    }
}

inline fun <reified T> RoutingContext.getFormParam(key: String, required: Boolean): T {
    if (required) {
        val value: String = this.request().getFormAttribute(key)
                ?: throw AppRuntimeException("form param %s is empty".format(key), REQ_FORM_ERROR)
        return convert<T>(value)
    } else {
        val value: String = this.request().getFormAttribute(key)
        if (value != null) {
            return convert<T>(value)
        }
        return value
    }
}


inline fun <reified T> convert(value: String): T {
    when (T::class) {
        Int::class -> {
            return value.toInt() as T
        }
        Long::class -> {
            return value.toLong() as T
        }
        Boolean::class -> {
            return value.toBoolean() as T
        }
        Double::class -> {
            return value.toDouble() as T
        }
        Float::class -> {
            return value.toFloat() as T
        }
        String::class -> {
            return value as T
        }
        Short::class -> {
            return value.toShort() as T
        }
        Character::class -> {
            return value[0] as T
        }
        else -> {
            return value as T
        }
    }
}

fun Router.defaultInit(vertx: Vertx): Router {
    this.route().handler(CookieHandler.create())
    this.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))
    this.route().handler(BodyHandler.create())
    this.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT))
    return this
}

fun RoutingContext.responseJson(obj: Any) {
    this.response().statusCode = 200
    if (this.acceptableContentType == "application/json") {
        this.response().putHeader("content-type", "application/json")
        if (obj is String) {
            this.response().end(obj)
        } else {
            this.response().end(Json.encode(obj))
        }

    }
}

fun Route.coroutineHandler(handle: suspend (RoutingContext) -> Unit,
                           authHandle: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                authHandle(ctx)
                handle(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}

fun Route.coroutineHandler(handle: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                handle(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}
