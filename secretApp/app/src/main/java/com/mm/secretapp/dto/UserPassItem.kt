package com.mm.secretapp.dto

import org.json.JSONObject

data class UserPassItem (var uid: Int, var uuid: String, var weight: Int, var updatedTime: Long, var data: JSONObject){
}