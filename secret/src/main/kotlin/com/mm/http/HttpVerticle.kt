package com.mm.http

import com.mm.Const.*
import com.mm.Extension.*
import com.mm.entity.TokenInfo
import com.mm.entity.UserInfo
import com.mm.exception.AppRuntimeException
import com.mm.utils.generateToken
import com.paratera.gpauth.utils.queryWithParams
import com.paratera.gpauth.utils.updateWithParams
import io.vertx.core.http.HttpServer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.mail.MailClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.jwt.JWTOptions


class HttpVerticle : CoroutineVerticle() {
    val LOGGER = LoggerFactory.getLogger(HttpVerticle::class.java)
    lateinit var postgreSQLClient: SQLClient
    lateinit var mailClient: MailClient

    val dbAuth: suspend (RoutingContext) -> Unit = {
        if (it.getSessionVal<UserInfo>(SESSION_KEY_USERINFO, false) == null) {
            val mmToken = it.getHeader<String>("MMToken", false)
            val tokenInfo = postgreSQLClient
                    .queryWithParams(SQL_FIND_TOKEN_INFO_BY_MTOKEN, mmToken)
                    .getObject<TokenInfo>()
            if (tokenInfo == null || tokenInfo.expiryTime < System.currentTimeMillis()) {
                throw AppRuntimeException("token already expired", TOKEN_EXPIRED)
            } else {
                val userInfo = postgreSQLClient
                        .queryWithParams(SQL_FIND_USER_INFO_BY_UUID, tokenInfo.uuid)
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

        val mailConfig = MailConfig()
        mailConfig.hostname = ""
        mailConfig.port = 443
        mailConfig.starttls = StartTLSOptions.REQUIRED
        mailConfig.username = "test"
        mailConfig.password = "111111z  "
        mailClient = MailClient.createNonShared(vertx, mailConfig)

        val router = Router.router(vertx).init(vertx)
        router.route().failureHandler(failHandler)
        router.post("/login").coroutineHandler{login(it)}

        awaitResult<HttpServer> { vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config.getInteger("http.port", 8080), it)
        }


    }

    suspend fun login(ctx: RoutingContext) {
        val email = ctx.getFormParam<String>(REQ_PARAM_KEY_EMAIL, true)
        val password = ctx.getFormParam<String>(REQ_PARAM_KEY_PASSWORD, true)
        val userInfo = postgreSQLClient.queryWithParams(SQL_FIND_USER_INFO_BY_EMAIL, email).getObject<UserInfo>()
        userInfo?: throw AppRuntimeException("user %s is not exist".format(email), AUTH_FAIL)
        if (userInfo.password != password) throw AppRuntimeException("password error", AUTH_FAIL)
        val tokenInfo = TokenInfo(generateToken(), System.currentTimeMillis() + 60 * 60 * 1000, userInfo.uuid)
        val saveResult = postgreSQLClient.updateWithParams(SQL_INSERT_TOKEN_INFO, tokenInfo.toJson()).isSuccessed()
        if (!saveResult) throw AppRuntimeException("generate token error, please try again", TOKEN_GENERATE_ERROR)
        ctx.responseJson(200, tokenInfo)
    }

    suspend fun logout(ctx: RoutingContext) {

    }

    suspend fun registe(ctx: RoutingContext) {

    }

}















