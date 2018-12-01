package com.mm.entity

import io.vertx.core.json.JsonObject

class UserPass {
    constructor()

    constructor(uid: Int, uuid: String, weight: Int, updatedTime: Long, data: JsonObject) {
        this.uid = uid
        this.uuid = uuid
        this.weight = weight
        this.updatedTime = updatedTime
        this.data = data
    }

    var uid: Int = 0
    var uuid: String = ""
    var weight: Int = 0
    var updatedTime: Long = 0
    var data: JsonObject = JsonObject()
}