package com.mm.http

import com.github.mauricio.async.db.exceptions.DatabaseException
import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException
import com.mm.Const.*
import com.mm.Extension.*
import com.mm.entity.TokenInfo
import com.mm.entity.UserInfo
import com.mm.exception.AppRuntimeException
import com.mm.utils.generateToken
import com.mm.Extension.queryWithParams
import com.mm.Extension.updateWithParams
import com.mm.entity.SuccessResult
import com.mm.utils.generateActiveCode
import com.mm.utils.generateUserId
import io.vertx.core.http.HttpServer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailResult
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.ext.mail.MailConfig
import io.vertx.kotlin.ext.mail.MailMessage

class HttpVerticle : CoroutineVerticle() {
    val LOGGER = LoggerFactory.getLogger(HttpVerticle::class.java)
    lateinit var postgreSQLClient: SQLClient
    lateinit var mailClient: MailClient

    suspend fun userInfoHandler(ctx: RoutingContext){
        val tokenInfo: TokenInfo = ctx.getSessionVal(SESSION_KEY_TOKEN, true)!!
        val userInfo = postgreSQLClient
                .queryWithParams(SQL_FIND_USER_INFO_BY_UUID, tokenInfo.uuid)
                .getObject<UserInfo>()?: throw Exception()
        ctx.put(SESSION_KEY_USERINFO, userInfo)
    }

    suspend fun authHandler(ctx: RoutingContext) {
        val mmToken = ctx.getHeader<String>(HEADER_KEY_MMTOKEN, true)!!
        val tokenInfo = postgreSQLClient
                .queryWithParams(SQL_FIND_TOKEN_INFO_BY_MTOKEN, mmToken)
                .getObject<TokenInfo>()?: throw AppRuntimeException("token non-existent", TOKEN_NON_EXIST)
        if (tokenInfo.expiryTime < System.currentTimeMillis() || tokenInfo.isInvalid) {
            throw AppRuntimeException("token invalid", TOKEN_EXPIRED)
        } else {
            ctx.put(SESSION_KEY_TOKEN, tokenInfo)
        }
    }

    fun failHandler(ctx: RoutingContext){
        val throwable = ctx.failure()
        if (throwable != null) {
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

    suspend fun login(ctx: RoutingContext) {
        val email: String = ctx.getFormParam(REQ_PARAM_KEY_EMAIL, true)!!
        val password: String = ctx.getFormParam(REQ_PARAM_KEY_PASSWORD, true)!!
        val userInfo = postgreSQLClient.queryWithParams(SQL_FIND_USER_INFO_BY_EMAIL, email)
                .getObject<UserInfo>()?:throw AppRuntimeException("user %s is not exist".format(email), LOGIN_FAIL)
        if (userInfo.password != password) throw AppRuntimeException("password error", LOGIN_FAIL)
        val tokenInfo = TokenInfo(generateToken(), System.currentTimeMillis() + 60 * 60 * 1000, userInfo.uuid, false)
        val saveResult = postgreSQLClient.updateWithParams(SQL_INSERT_TOKEN_INFO, tokenInfo.toJson()).isSuccessed()
        if (!saveResult) throw AppRuntimeException("generate token error, please try again", TOKEN_GENERATE_ERROR)
        ctx.responseJson(200, tokenInfo)
    }

    suspend fun logout(ctx: RoutingContext) {
        val tokenInfo: TokenInfo = ctx.get(SESSION_KEY_TOKEN)
        tokenInfo.isInvalid = true
        postgreSQLClient.updateWithParams(SQL_UPDATE_TOKEN_BY_MTOKEN, tokenInfo.toJson(), tokenInfo.mtoken)
        ctx.responseJson(200, SuccessResult("logout success", true))
    }

    suspend fun registe(ctx: RoutingContext) {
        val email: String = ctx.getFormParam(REQ_PARAM_KEY_EMAIL, true)!!
        val pass: String = ctx.getFormParam(REQ_PARAM_KEY_PASSWORD, true)!!
        val sex: Int = ctx.getFormParam(REQ_PARAM_KEY_SEX, true)!!
        if (!email.isEmail()) throw AppRuntimeException("email validation error", REQ_PARAM_ERROR)
        if (postgreSQLClient.queryWithParams(SQL_FIND_USER_INFO_BY_EMAIL, email).getObject<UserInfo>() != null) {
            throw AppRuntimeException("email already be used", REGIST_ERROR)
        }
        val userInfo = UserInfo(generateUserId(), email, pass, false, sex, System.currentTimeMillis(), ArrayList())
        val saveResult = postgreSQLClient.updateWithParams(SQL_INSERT_USER_INFO, userInfo.toJson()).isSuccessed()
        if (!saveResult) throw AppRuntimeException("save user error, please try again", REGIST_ERROR)
        ctx.responseJson(200, SuccessResult("regist success", true, userInfo))
    }

    suspend fun active(ctx: RoutingContext) {
        val uuid: String = ctx.getReqParam(REQ_PARAM_KEY_UUID, true)!!
        val email: String = ctx.getReqParam(REQ_PARAM_KEY_EMAIL, true)!!
        val activateCode: String = ctx.getReqParam(REQ_PARAM_KEY_AVTIVATE_CODE, true)!!

        val code: String = postgreSQLClient
                .queryWithParams(SQL_FIND_ACTIVATE_CODE_BY_UUID_AND_EMAIL, uuid, email)
                .get(0)?: throw AppRuntimeException("can not found you email", ACTIVATE_ERROR)
        if (activateCode == code) {
            val userInfo = postgreSQLClient
                    .queryWithParams(SQL_FIND_USER_INFO_BY_UUID, uuid)
                    .getObject<UserInfo>()?: throw Exception()
            userInfo.active = true
            val saveBoolean = postgreSQLClient
                    .updateWithParams(SQL_UPDATE_USER_BY_EMAIL, userInfo.toJson(), email, uuid).isSuccessed()
            if (!saveBoolean) throw AppRuntimeException("update user error. please try again", ACTIVATE_ERROR)
            ctx.responseJson(200, SuccessResult("activate success", true))
        } else {
            throw AppRuntimeException("activate code error. please check you input", ACTIVATE_ERROR)
        }
    }

    suspend fun sendActivateMail(ctx: RoutingContext) {
        val userInfo: UserInfo = ctx.get(SESSION_KEY_USERINFO)
        if (userInfo.active) throw AppRuntimeException("you do not neet active you account", SEND_EMAIL_ERROR)
        val code = generateActiveCode()
        val saveBoolean = postgreSQLClient
                .updateWithParams(SQL_INSET_ACTIVATE, code, userInfo.email, userInfo.uuid, code).isSuccessed()
        if (!saveBoolean) throw AppRuntimeException("update activate error. please try again", SEND_EMAIL_ERROR)
        var message = MailMessage()
        message.from = "1035298618@qq.com"
        message.to = arrayListOf("1035298618@qq.com")
        message.text = "mSecret service activate"
        message.html = ACTIVATE_MATL_HTML_TEMPLATE.format(code,
                ACTIVATE_URL.format(userInfo.uuid, userInfo.email, code))
        val mailResult = awaitResult<MailResult> {
            mailClient.sendMail(message, it)
        }
        ctx.responseJson(200, SuccessResult("send success", true, mailResult.toJson()))
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
        mailConfig.hostname = "smtp.qq.com"
        mailConfig.port = 465
        mailConfig.isSsl  =true
        mailConfig.starttls = StartTLSOptions.REQUIRED
        mailConfig.username = "1035298618@qq.com"
        mailConfig.password = MAIL_AUTH_CODE
        mailClient = MailClient.createNonShared(vertx, mailConfig)

        val router = Router.router(vertx).init(vertx)
        router.route().failureHandler({failHandler(it)})
        router.post("/login")
                .coroutineHandler({login(it)})
        router.delete("/logout")
                .coroutineHandler({authHandler(it)}, {userInfoHandler(it)},  {logout(it)})
        router.post("/regist")
                .coroutineHandler({registe(it)})
        router.get("/active")
                .coroutineHandler({active(it)})
        router.get("/mail")
                .coroutineHandler({authHandler(it)}, {userInfoHandler(it)}, {sendActivateMail(it)})

        awaitResult<HttpServer> { vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config.getInteger("http.port", 8080), it)
        }
    }
}















