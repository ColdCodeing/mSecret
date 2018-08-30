package com.mm.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.mm.Extension.toJson

class UserInfo {

    constructor()

    constructor(uuid: String, email: String, password: String, active: Boolean,
                sex: Int, registTime: Long, oldPassword: List<String>) {
        this.uuid = uuid
        this.email = email
        this.password = password
        this.active = active
        this.sex = sex
        this.registTime = registTime
        this.oldPassword = oldPassword
    }

    var uuid: String = ""
    var email: String = ""
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String = ""
    var active: Boolean = false
    var sex: Int = 0
    var registTime: Long = 0
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var oldPassword: List<String> = ArrayList()
}