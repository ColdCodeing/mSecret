package com.mm.http

import com.alibaba.fastjson.JSON
import com.mm.Const.*
import com.mm.Extension.*
import com.mm.entity.TokenInfo
import com.mm.entity.UserInfo
import com.mm.exception.AppRuntimeException
import com.mm.utils.generateToken
import com.mm.Extension.queryWithParams
import com.mm.Extension.updateWithParams
import com.mm.entity.SuccessResult
import com.mm.entity.UserPass
import com.mm.utils.generateActiveCode
import com.mm.utils.generateUserId
import com.sun.jmx.snmp.EnumRowStatus.active
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailResult
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.ext.mail.MailConfig
import io.vertx.kotlin.ext.mail.MailMessage
import sun.security.jgss.GSSUtil.login
import java.util.stream.Collectors

class HttpVerticle : CoroutineVerticle() {
    val LOGGER = LoggerFactory.getLogger(HttpVerticle::class.java)!!
    lateinit var postgreSQLClient: SQLClient
    lateinit var mailClient: MailClient

    suspend fun userInfoHandler(ctx: RoutingContext){
        //session
        if (ctx.getSessionVal<UserInfo>(SESSION_KEY_USERINFO, false) == null) {
            val tokenInfo: TokenInfo = ctx.getSessionVal(SESSION_KEY_TOKEN, true)!!
            val userInfo = postgreSQLClient
                    .queryWithParams(SQL_FIND_USER_INFO_BY_UUID, tokenInfo.uuid)
                    .getJsonObject<UserInfo>() ?: throw Exception()
            ctx.putSesssionVal(SESSION_KEY_USERINFO, userInfo.toJson())
        }
    }

    suspend fun authHandler(ctx: RoutingContext) {
        if (ctx.getSessionVal<TokenInfo>(SESSION_KEY_TOKEN, false) == null) {
            val mmToken = ctx.getHeader<String>(HEADER_KEY_MTOKEN, true)!!
            //TODO need redis
            val tokenInfo = postgreSQLClient
                    .queryWithParams(SQL_FIND_TOKEN_INFO_BY_MTOKEN, mmToken)
                    .getJsonObject<TokenInfo>() ?: throw AppRuntimeException("token non-existent", TOKEN_NON_EXIST)
            if (tokenInfo.expiryTime < System.currentTimeMillis() || tokenInfo.invalid) {
                throw AppRuntimeException("token invalid", TOKEN_EXPIRED)
            } else {
                ctx.putSesssionVal(SESSION_KEY_TOKEN, tokenInfo.toJson())
            }
        }
    }

    fun activeCheckHandler(ctx: RoutingContext) {
        val userInfo: UserInfo = ctx.getSessionVal<UserInfo>(SESSION_KEY_USERINFO, true)!!
        if (!userInfo.active) {
            throw AppRuntimeException("your email not activate", UN_ACTIVATE)
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
        val dbconfig = config.getJsonObject(CONFIG_SECRET_DATABASE, postgreSQLClientConfig)
        postgreSQLClient = PostgreSQLClient.createShared(vertx, dbconfig)

        val mailServiceConfig = config.getJsonObject(CONFIG_MIAL_SERVICE)
        val mailConfig = MailConfig()
        mailConfig.hostname = mailServiceConfig.getString("hostname")
        mailConfig.port = mailServiceConfig.getInteger("port")
        mailConfig.isSsl = mailServiceConfig.getBoolean("isSsl")
        mailConfig.starttls = StartTLSOptions.valueOf(mailServiceConfig.getString("starttls"))
        mailConfig.username = mailServiceConfig.getString("username")
        mailConfig.password = mailServiceConfig.getString("password")
        mailClient = MailClient.createNonShared(vertx, mailConfig)

        val router = Router.router(vertx).init(vertx)
        val subRouter = Router.router(vertx)
        subRouter.route().failureHandler({failHandler(it)})
        subRouter.post("/login")
                .coroutineHandler({login(it)})
        subRouter.get("/user")
                .coroutineHandler({authHandler(it)}, {userInfoHandler(it)}, {activeCheckHandler(it)}, {getUserInfo(it)})
        subRouter.delete("/logout")
                .coroutineHandler({authHandler(it)}, {userInfoHandler(it)},  {logout(it)})
        subRouter.post("/regist")
                .coroutineHandler({registe(it)})
        subRouter.get("/active")
                .coroutineHandler({active(it)})
        subRouter.get("/mail")
                .coroutineHandler({authHandler(it)}, {userInfoHandler(it)}, {sendActivateMail(it)})
        //前处理
        subRouter.route("/upasses/*")
                .coroutineBeforeHandler({authHandler(it)}, true)
                .coroutineBeforeHandler({userInfoHandler(it)}, true)
                .coroutineBeforeHandler({activeCheckHandler(it)}, true)
        subRouter.post("/upasses")
                .coroutineHandler({saveUserPass(it)})
        subRouter.delete("/upasses/:$REQ_PARAM_KEY_USERPASS_ID")
                .coroutineHandler({deleteUserPass(it)})
        subRouter.put("/upasses/:$REQ_PARAM_KEY_USERPASS_ID")
                .coroutineHandler({updateUserPass(it)})
        subRouter.get("/upasses/:$REQ_PARAM_KEY_USERPASS_ID")
                .coroutineHandler({getUserPass(it)})
        subRouter.get("/upasses")
                .coroutineHandler({getUserPasses(it)})

        router.mountSubRouter("/api/v1", subRouter)
        awaitResult<HttpServer> { vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config.getInteger(CONFIG_HTTP_PORT, 8080), it)
        }
    }

    suspend fun login(ctx: RoutingContext) {
        val email: String = ctx.getFormParam(REQ_PARAM_KEY_EMAIL, true)!!
        val password: String = ctx.getFormParam(REQ_PARAM_KEY_PASSWORD, true)!!
        val userInfo = postgreSQLClient.queryWithParams(SQL_FIND_USER_INFO_BY_EMAIL, email)
                .getJsonObject<UserInfo>()?:throw AppRuntimeException("user %s is not exist".format(email), LOGIN_FAIL)
        if (userInfo.password != password) throw AppRuntimeException("password error", LOGIN_FAIL)
        val tokenInfo = TokenInfo(generateToken(), System.currentTimeMillis() + 60 * 60 * 1000, userInfo.uuid, false)
        val saveResult = postgreSQLClient.updateWithParams(SQL_INSERT_TOKEN_INFO, JSON.toJSONString(tokenInfo)).isSuccessed()
        if (!saveResult) throw AppRuntimeException("generate token error, please try again", TOKEN_GENERATE_ERROR)
        ctx.responseJson(200, tokenInfo)
    }

    suspend fun logout(ctx: RoutingContext) {
        val tokenInfo: TokenInfo = ctx.getSessionVal<TokenInfo>(SESSION_KEY_TOKEN, true)!!
        tokenInfo.invalid = true
        postgreSQLClient.updateWithParams(SQL_UPDATE_TOKEN_BY_MTOKEN, JSON.toJSONString(tokenInfo), tokenInfo.mtoken)
        ctx.responseJson(200, SuccessResult("logout success", true))
    }

    suspend fun registe(ctx: RoutingContext) {
        val email: String = ctx.getFormParam(REQ_PARAM_KEY_EMAIL, true)!!
        val pass: String = ctx.getFormParam(REQ_PARAM_KEY_PASSWORD, true)!!
        val sex: Int = ctx.getFormParam(REQ_PARAM_KEY_SEX, true)!!
        if (!email.isEmail()) throw AppRuntimeException("email validation error", REQ_PARAM_ERROR)
        if (postgreSQLClient.queryWithParams(SQL_FIND_USER_INFO_BY_EMAIL, email).getJsonObject<UserInfo>() != null) {
            throw AppRuntimeException("email already be used", REGIST_ERROR)
        }
        val userInfo = UserInfo(generateUserId(), email, pass, false, sex, System.currentTimeMillis(), ArrayList())
        val saveResult = postgreSQLClient.updateWithParams(SQL_INSERT_USER_INFO, JSON.toJSONString(userInfo)).isSuccessed()
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
                    .getJsonObject<UserInfo>()?: throw Exception()
            userInfo.active = true
            val saveBoolean = postgreSQLClient
                    .updateWithParams(SQL_UPDATE_USER_BY_EMAIL, JSON.toJSONString(userInfo), email, uuid).isSuccessed()
            if (!saveBoolean) throw AppRuntimeException("update user error. please try again", ACTIVATE_ERROR)
            ctx.responseJson(200, SuccessResult("activate success", true))
        } else {
            throw AppRuntimeException("activate code error. please check you input", ACTIVATE_ERROR)
        }
    }

    suspend fun sendActivateMail(ctx: RoutingContext) {
        val userInfo = ctx.getSessionVal<UserInfo>(SESSION_KEY_USERINFO, true)!!
        if (userInfo.active) throw AppRuntimeException("you do not neet active you account", SEND_EMAIL_ERROR)
        val code = generateActiveCode()
        val saveBoolean = postgreSQLClient
                .updateWithParams(SQL_INSET_ACTIVATE, code, userInfo.email, userInfo.uuid, code).isSuccessed()
        if (!saveBoolean) throw AppRuntimeException("update activate error. please try again", SEND_EMAIL_ERROR)
        val message = MailMessage()
        message.from = "1035298618@qq.com"
        message.to = arrayListOf(userInfo.email)
        message.text = "mSecret service activate"
        message.html = config.getString(CONFIG_ACTIVATE_MESSAGE_TEMPLATE).format(code,
                config.getString(CONFIG_ACTIVATE_URL_TEMPLATE).format(userInfo.uuid, userInfo.email, code))
        val mailResult = awaitResult<MailResult> {
            mailClient.sendMail(message, it)
        }
        ctx.responseJson(200, SuccessResult("send success", true, mailResult.toJson()))
    }

    fun getUserInfo(ctx: RoutingContext) {
        val userInfo: UserInfo = ctx.getSessionVal<UserInfo>(SESSION_KEY_USERINFO, true)!!
        ctx.responseJson(200, userInfo)
    }

    suspend fun saveUserPass(ctx: RoutingContext) {
        val uuid = ctx.getSessionVal<TokenInfo>(SESSION_KEY_TOKEN, true)!!.uuid
        val queryData = ctx.bodyAsJson?: throw throw AppRuntimeException("body not contain json data", REQ_BODY_ERROR)
        var updateResult = false
        val topWeight = postgreSQLClient.queryWithParams(SQL_FIND_MAX_WEIGHT_WITH_UUID, uuid).get<Int>(0)?:0
        queryData.getJsonObject(REQ_PARAM_KEY_USERPASS_DATA)?.let {
            updateResult = postgreSQLClient.updateWithParams(SQL_INSERT_USER_PASS, uuid, topWeight + 1, it.toString()).isSuccessed()
        }
        ctx.responseJson(200, SuccessResult("save success", updateResult))
    }

    suspend fun deleteUserPass(ctx: RoutingContext) {
        val uid = ctx.getPathParam<Int>(REQ_PARAM_KEY_USERPASS_ID, true)
        val updateResult = postgreSQLClient.updateWithParams(SQL_DELETE_USER_PASS_BY_UID, uid).isSuccessed()
        ctx.responseJson(200, SuccessResult("delete success", updateResult))
    }

    suspend fun updateUserPass(ctx: RoutingContext) {
        val uid = ctx.getPathParam<Int>(REQ_PARAM_KEY_USERPASS_ID, true)
        val uuid = ctx.getSessionVal<TokenInfo>(SESSION_KEY_TOKEN, true)!!.uuid
        val queryData = ctx.bodyAsJson?: throw throw AppRuntimeException("body not contain json data", REQ_BODY_ERROR)
        var updateResult = false
        println(queryData)
        queryData.get<String>(REQ_PARAM_KEY_USERPASS_WEIGHT)?.let {
            when (it) {
                REQ_PARAM_WEIGHT_TOP -> {
                    val topWeight = postgreSQLClient.queryWithParams(SQL_FIND_MAX_WEIGHT_WITH_UUID, uuid).get<Int>(0)?:0
                    updateResult = postgreSQLClient.updateWithParams(SQL_UPDATE_USER_PASS_WEIGHT_BY_UID, topWeight + 1, uid).isSuccessed()
                }
                REQ_PARAM_WEIGHT_SWAP -> {
                    //交换
                    val toUid = queryData.get<Int>(REQ_PARAM_KEY_USERPASS_SWAP_TO)?:
                    throw AppRuntimeException("%s is empty".format(REQ_PARAM_KEY_USERPASS_SWAP_TO), REQ_BODY_ERROR)
                    val fromWeight = postgreSQLClient.queryWithParams(SQL_FIND_WEIGHT_BY_UID, uid).get<Int>(0)
                    val toWeight = postgreSQLClient.queryWithParams(SQL_FIND_WEIGHT_BY_UID, toUid).get<Int>(0)
                    updateResult = postgreSQLClient.updateWithParams(SQL_UPDATE_USER_PASS_WEIGHT_BY_UID, toWeight, uid).isSuccessed()
                    updateResult = postgreSQLClient.updateWithParams(SQL_UPDATE_USER_PASS_WEIGHT_BY_UID, fromWeight, uid).isSuccessed()
                }
            }
        }
        queryData.getJsonObject(REQ_PARAM_KEY_USERPASS_DATA)?.let {
            updateResult = postgreSQLClient.updateWithParams(SQL_UPDATE_USER_PASS_BY_UID, it.toString(), uid).isSuccessed()
        }
        ctx.responseJson(200, SuccessResult("update success", updateResult))
    }

    suspend fun getUserPasses(ctx: RoutingContext) {
        val uuid = ctx.getSessionVal<TokenInfo>(SESSION_KEY_TOKEN, true)!!.uuid
        val result = postgreSQLClient.queryWithParams(SQL_FIND_USER_PASS_BY_UUID, uuid)
                .results
                .stream()
                .map {
                    UserPass(it.getInteger(0),
                            it.getString(1),
                            it.getInteger(2),
                            it.getLong(3),
                            JsonObject(it.getString(4)))
                }.collect(Collectors.toList())
        ctx.responseJson(200, result)
    }

    suspend fun getUserPass(ctx: RoutingContext) {
        val uuid = ctx.getSessionVal<TokenInfo>(SESSION_KEY_TOKEN, true)!!.uuid
        val uid = ctx.getPathParam<Int>(REQ_PARAM_KEY_USERPASS_ID, true)
        val result = postgreSQLClient.queryWithParams(SQL_FIND_USER_PASS_BY_UUID_AND_UID, uuid, uid).results.get(0)
        val userpass = UserPass(result.getInteger(0),
                result.getString(1),
                result.getInteger(2),
                result.getLong(3),
                JsonObject(result.getString(4)))
        ctx.responseJson(200, userpass)
    }
}















