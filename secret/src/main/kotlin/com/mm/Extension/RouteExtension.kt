@file:Suppress("UNCHECKED_CAST")

package com.mm.Extension

import com.mm.Const.*
import com.mm.exception.AppRuntimeException
import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.*
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

fun RoutingContext.putSesssionVal(key: String, value: Any) {
    this.session().put(key, value)
}

inline fun <reified T> RoutingContext.getSessionVal(key: String, required: Boolean): T? {
    when (T::class) {
        Int::class, Long::class, Boolean::class, Double::class, Float::class, String::class, Short::class, Character::class -> {
            if (required) {
                val value: T = this.session().get<T>(key)
                        ?: throw AppRuntimeException("session %s is empty".format(key), SESSION_ERROR)
                return value
            } else {
                return this.session().get<T>(key)
            }
        }
    }
    if (required) {
        val value = this.session().get<String>(key)
                ?: throw AppRuntimeException("session %s is empty".format(key), SESSION_ERROR)
        return value.fromJson()
    } else {
        val value:String = this.request().getParam(key)?: return null
        return value.fromJson()
    }
}

inline fun <reified T> RoutingContext.getReqParam(key: String, required: Boolean): T? {
    if (required) {
        val value: String = this.request().getParam(key)
                ?: throw AppRuntimeException("request param %s is empty".format(key), REQ_PARAM_ERROR)
        return convert<T>(value)
    } else {
        val value:String = this.request().getParam(key)?: return null
        return convert<T>(value)
    }
}

inline fun <reified T> RoutingContext.getPathParam(key: String, required: Boolean): T? {
    if (required) {
        val value: String = this.pathParam(key)
                ?: throw AppRuntimeException("path param %s is empty".format(key), REQ_PATH_PARAM_ERROR)
        return convert<T>(value)
    } else {
        val value: String = this.pathParam(key)?: return null
        return convert<T>(value)
    }
}

inline fun <reified T> RoutingContext.getFormParam(key: String, required: Boolean): T? {
    if (required) {
        val value: String = this.request().formAttributes().get(key)
                ?: throw AppRuntimeException("form param %s is empty".format(key), REQ_FORM_ERROR)
        return convert<T>(value)
    } else {
        val value: String = this.request().getFormAttribute(key)?: return null
        return convert<T>(value)
    }
}

inline fun <reified T> RoutingContext.getHeader(key: String, required: Boolean): T? {
    if (required) {
        val value: String = this.request().headers().get(key)
                ?: throw AppRuntimeException("header %s is empty".format(key), REQ_FORM_ERROR)
        return convert<T>(value)
    } else {
        val value: String = this.request().getHeader(key)?: return null
        return convert<T>(value)
    }
}


inline fun <reified T> convert(value: String): T {
    try {
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
    } catch (e : Exception) {
        throw AppRuntimeException("$value can not convert to " + T::class.java.toString(), DATA_CONVERT_ERROR)
    }
}

fun Router.init(vertx: Vertx): Router {
    this.route().handler(CookieHandler.create())
    this.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))
    this.route().handler(BodyHandler.create())
    this.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT))
    return this
}

fun RoutingContext.responseJson(statusCode: Int, obj: Any) {
    this.response().statusCode = statusCode
    this.response().putHeader("content-type", "application/json")
    if (obj is String) {
        this.response().end(obj)
    } else {
        val jsonStr = obj.toJson()
        this.response().end(jsonStr)
    }
}

fun Route.coroutineHandler(vararg handles: suspend (RoutingContext) -> Unit) : Route{
    handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                handles.forEach {
                    it(ctx)
                }
            } catch(e: Exception) {
                ctx.fail(e)
            }
        }
    }
    return this
}

fun Route.coroutineBeforeHandler(handle : suspend (RoutingContext) -> Unit, next: Boolean) : Route {
    handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                handle(ctx)
                if (next) {
                    ctx.next()
                }
            } catch(e: Exception) {
                ctx.fail(e)
            }
        }
    }
    return this
}
