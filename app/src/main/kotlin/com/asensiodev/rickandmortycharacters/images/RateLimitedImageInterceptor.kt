package com.asensiodev.rickandmortycharacters.images

import coil3.intercept.Interceptor
import coil3.network.HttpException
import coil3.request.ErrorResult
import coil3.request.ImageResult
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.delay

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val DEFAULT_RETRY_DELAY_MILLIS = 10_000L
private const val MAX_RETRY_DELAY_MILLIS = 60_000L
private const val MILLIS_PER_SECOND = 1_000L
private const val RETRY_TIMING_MARGIN_MILLIS = 1_000L

internal class RateLimitedImageInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val result = chain.proceed()
        val error = (result as? ErrorResult)?.throwable as? HttpException
        if (error?.response?.code == HTTP_TOO_MANY_REQUESTS) {
            val waitMillis = error.retryDelayMillis()
            if (waitMillis <= MAX_RETRY_DELAY_MILLIS) {
                delay(waitMillis)
                return chain.proceed()
            }
        }
        return result
    }
}

private fun HttpException.retryDelayMillis(): Long {
    val header = response.headers["Retry-After"]?.trim()
    return when {
        header == null -> DEFAULT_RETRY_DELAY_MILLIS

        header.isNotEmpty() && header.all { it in '0'..'9' } -> {
            val seconds = header.toLongOrNull()
            if (seconds == null || seconds > MAX_RETRY_DELAY_MILLIS / MILLIS_PER_SECOND) {
                Long.MAX_VALUE
            } else {
                seconds * MILLIS_PER_SECOND + RETRY_TIMING_MARGIN_MILLIS
            }
        }

        else -> try {
            val retryAt = ZonedDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
            (retryAt - System.currentTimeMillis()).coerceAtLeast(0) + RETRY_TIMING_MARGIN_MILLIS
        } catch (_: DateTimeParseException) {
            DEFAULT_RETRY_DELAY_MILLIS
        }
    }
}
