package com.mm.secretapp.extension

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.io.IOException
import android.content.Intent
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit


suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
        })

        continuation.invokeOnCompletion {
            if (continuation.isCancelled)
                try {
                    cancel()
                } catch (ex: Throwable) {
                    //Ignore cancel exception
                }
        }
    }
}