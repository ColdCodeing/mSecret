package com.mm.http

import com.mm.Const.*
import com.mm.entity.TokenInfo
import com.mm.entity.UserInfo
import com.mm.utils.*
import com.paratera.gpauth.utils.queryWithParams
import io.vertx.core.http.HttpServer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult

class HttpVerticle : CoroutineVerticle() {
    val LOGGER = LoggerFactory.getLogger(HttpVerticle::class.java)
    lateinit var postgreSQLClient: SQLClient

    val dbAuth: suspend (RoutingContext) -> Unit = {
        if (it.getSessionVal<UserInfo>(SESSION_KEY_USERINFO, false) == null) {
            val mmToken = it.getHeader<String>("MMToken", false)
            val tokenInfo = postgreSQLClient
                    .queryWithParams(SQL_FIND_TOKENINFO_BY_MTOKEN, mmToken)
                    .getObject<TokenInfo>()
            if (tokenInfo == null || tokenInfo.expiryTime < System.currentTimeMillis()) {
                throw AppRuntimeException("token already expired", TOKEN_EXPIRED)
            } else {
                val userInfo = postgreSQLClient
                        .queryWithParams(SQL_FIND_USERINFO_BY_ID, tokenInfo.userId)
                        .getObject<UserInfo>()
                it.put(SESSION_KEY_USERINFO, userInfo)
            }
        }
    }

    val failHandler: (RoutingContext) -> Unit = {
        val throwable = it.failure()
        if (throwable != null) {
            LOGGER.error(throwable.message, throwable)
            if (throwable is AppRuntimeException) {
                it.responseJson(500, JsonObject().put("code", throwable.code).put("msg", throwable.message))
            }
        } else {
            LOGGER.error("UNKNOW EXCEPTION WHERE DOING", it.data().toString())
            it.responseJson(500, JsonObject().put("code", UNKNOW_ERROR).put("msg", "unknow exception"))
        }
    }

    override suspend fun start() {
        val postgreSQLClientConfig = json {
            obj(
                    "host" to "localhost",
                    "port" to 5432,
                    "maxPoolSize" to 30,
                    "username" to "mm",
                    "password" to "111111",
                    "database" to "mmdata",
                    "queryTimeout" to 30000
            )
        }
        val dbconfig = config.getJsonObject("db.config", postgreSQLClientConfig)
        postgreSQLClient = PostgreSQLClient.createShared(vertx, dbconfig)

        val router = Router.router(vertx).init(vertx)
        router.route().failureHandler(failHandler)
        router.get("/").coroutineHandler({getRating(it)}, dbAuth)

        awaitResult<HttpServer> { vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config.getInteger("http.port", 8080), it)
        }


    }

    suspend fun getRating(ctx: RoutingContext) {
        throw AppRuntimeException("11111", 1111)
    }

}