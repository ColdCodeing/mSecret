package com.mm.entity

data class UserInfo(val uuid: String = "", val email: String = "", val password: String = "", val sex: Int = 0,
                    val registTime: Long = 0, val oldPassword: List<String> = ArrayList())