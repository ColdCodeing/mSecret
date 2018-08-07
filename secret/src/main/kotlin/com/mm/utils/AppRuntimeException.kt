package com.mm.utils

class AppRuntimeException : Exception {
    var code: Int = 0

    constructor(message: String, code: Int) : super(message) {
        this.code = code
    }

    constructor(message: String, throwable: Throwable, code: Int) : super(message, throwable) {
        this.code = code
    }
}