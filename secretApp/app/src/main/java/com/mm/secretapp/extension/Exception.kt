package com.mm.secretapp.extension

import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlin.coroutines.experimental.CoroutineContext

internal val simpleExceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
    throwable.printStackTrace()
}
