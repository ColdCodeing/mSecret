package com.mm.Const

const val SQL_FIND_TOKENINFO_BY_MTOKEN = "select token_info\n" +
        "  from datatest.t_token where mtoken = ?"
const val SQL_FIND_USERINFO_BY_ID = "select uinfo\n" +
        "  from datatest.t_user where id = ?"