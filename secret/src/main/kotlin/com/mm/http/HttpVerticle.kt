package com.mm.http

import com.mm.utils.AppRuntimeException
import com.mm.utils.defaultInit
import com.mm.utils.responseJson
import io.vertx.core.Future
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle

class HttpVerticle : CoroutineVerticle() {
    val LOGGER = LoggerFactory.getLogger(HttpVerticle::class.java)

    override fun start(startFuture: Future<Void>?) {
        val router = Router.router(vertx).defaultInit(vertx)
        router.route().failureHandler {
            val throwable = it.failure()
            if (throwable != null) {
                LOGGER.error(throwable.message, throwable)
                if (throwable is AppRuntimeException) {
                    it.responseJson(JsonObject().put("code", throwable.code).put("msg", throwable.message))
                }
            } else {
                LOGGER.error("UNKNOW EXCEPTION WHERE DOING", it.data().toString())
                it.responseJson(JsonObject().put("code", UNKNOW_ERROR).put("msg", "unknow exception"))
            }
        }

        val dbAuth = {

        }
    }
}