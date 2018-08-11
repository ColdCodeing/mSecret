package com.mm.Const

const val SQL_INSERT_TOKEN_INFO = "insert into datatest.t_token (token_info) values (?) returning unique_id"
const val SQL_INSERT_USER_INFO = "insert into datatest.t_user (user_info) values (?) returning unique_id"
const val SQL_FIND_USER_INFO_BY_EMAIL = "select user_info from datatest.t_user where (user_info ->> 'email') = ?"
const val SQL_FIND_TOKEN_INFO_BY_MTOKEN = "select token_info from datatest.t_token where (token_info ->> 'mtoken') = ?"
const val SQL_FIND_USER_INFO_BY_UUID = "select user_info from datatest.t_user where (user_info ->> 'uuid')::int4 = ?"
const val SQL_UPDATE_TOKEN_BY_MTOKEN = "update datatest.t_token set token_info = ? where (token_info ->> 'mtoken') = ?"
const val SQL_UPDATE_USER_BY_EMAIL = "update datatest.t_user set user_info = ? " +
        "where (user_info ->> 'email') =? and (user_info ->> 'uuid')::int4 = ?";
const val SQL_GET_LAST_UUID = "select max((user_info ->> 'uuid')::int4) from datatest.t_user"
const val SQL_INSET_ACTIVATE = "insert into datatest.t_account_activate (activate_code, email, uuid)values (? ,?, ?)" +
        "  on conflict(email) do update set activate_code = ?"
const val SQL_FIND_ACTIVATE_CODE_BY_UUID_AND_EMAIL = "select activate_code from datatest.t_account_activate " +
        "where uuid = ? and email = ?"
