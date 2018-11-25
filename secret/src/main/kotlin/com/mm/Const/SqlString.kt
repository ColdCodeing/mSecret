package com.mm.Const

const val SQL_INSERT_TOKEN_INFO = "insert into t_token (token_info) values (?) returning unique_id"
const val SQL_INSERT_USER_INFO = "insert into t_user (user_info) values (?) returning unique_id"
const val SQL_FIND_USER_INFO_BY_EMAIL = "select user_info from t_user where (user_info ->> 'email') = ?"
const val SQL_FIND_TOKEN_INFO_BY_MTOKEN = "select token_info from t_token where (token_info ->> 'mtoken') = ?"
const val SQL_FIND_USER_INFO_BY_UUID = "select user_info from t_user where (user_info ->> 'uuid')::varchar = ?"
const val SQL_UPDATE_TOKEN_BY_MTOKEN = "update t_token set token_info = ? where (token_info ->> 'mtoken') = ?"
const val SQL_UPDATE_USER_BY_EMAIL = "update t_user set user_info = ? " +
        "where (user_info ->> 'email') =? and (user_info ->> 'uuid')::varchar = ?"
const val SQL_INSET_ACTIVATE = "insert into t_account_activate (activate_code, email, uuid) values (? ,?, ?)" +
        "  on conflict(email) do update set activate_code = ?"
const val SQL_FIND_ACTIVATE_CODE_BY_UUID_AND_EMAIL = "select activate_code from t_account_activate " +
        "where uuid = ? and email = ?"

const val SQL_FIND_USER_PASS_BY_UUID =
        "select unique_id, uuid, floor(extract(epoch from((updated_time - timestamp '1970-01-01 00:00:00')*1000))), data " +
                "from t_user_pass where uuid = ?;"
const val SQL_INSERT_USER_PASS = "insert into t_user_pass(uuid, data) values (?, ?) returning unique_id"
const val SQL_UPDATE_USER_PASS_BY_UID = "update t_user_pass set data = ? where unique_id = ?"
const val SQL_DELETE_USER_PASS_BY_UID = "delete from t_user_pass where unique_id = ?"