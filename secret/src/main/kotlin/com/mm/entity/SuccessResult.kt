package com.mm.entity

data class SuccessResult(
        val msg: String = "",
        val successed: Boolean = true,
        val ext: Any? = null
)