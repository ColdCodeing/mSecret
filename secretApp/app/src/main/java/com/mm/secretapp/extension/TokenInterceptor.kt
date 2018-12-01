package com.mm.secretapp.extension

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class TokenInterceptor(val sharedPreferences: SharedPreferences) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val tokenRequest: Request? = null
        val tokenInfoStr = sharedPreferences.getString("tokenInfo", null)
        if (tokenInfoStr.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        } else {
            val tokenInfo = JSONObject(tokenInfoStr)
            return chain.proceed(
                    originalRequest
                    .newBuilder()
                    .addHeader("mtoken", tokenInfo.getString("mtoken"))
                    .build())
        }
    }

}