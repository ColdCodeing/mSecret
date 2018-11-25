package com.mm.Const

const val SESSION_KEY_USERINFO = "user_info"
const val SESSION_KEY_TOKEN = "mtoken"
const val REQ_PARAM_KEY_EMAIL = "email"
const val REQ_PARAM_KEY_PASSWORD = "password"
const val HEADER_KEY_MTOKEN = "mtoken"
const val REQ_PARAM_KEY_SEX = "sex"
const val REQ_PARAM_KEY_UUID = "uuid"
const val REQ_PARAM_KEY_AVTIVATE_CODE = "activate_code"
const val REQ_PARAM_KEY_USERPASS_ID = "uid"

const val MAIL_AUTH_CODE = "znaeswkydstpbdhc"
const val ACTIVATE_MATL_HTML_TEMPLATE = "你的激活码是: %s <br> 激活请点链接: %s"
const val ACTIVATE_URL = "http://localhost:8080/active?uuid=%s&email=%s&activate_code=%s"