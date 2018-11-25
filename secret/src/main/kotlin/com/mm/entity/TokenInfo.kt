package com.mm.entity

import com.fasterxml.jackson.annotation.JsonProperty

class TokenInfo {
    constructor()

    constructor(mtoken: String, expiryTime: Long, uuid: String, invalid: Boolean) {
        this.mtoken = mtoken
        this.expiryTime = expiryTime
        this.uuid = uuid
        this.invalid = invalid
    }

    var mtoken: String = ""
    var expiryTime: Long = 0
    var uuid: String = ""
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var invalid: Boolean = true
}