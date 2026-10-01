package com.asensiodev.rickandmortycharacters.data.characters.remote

import java.io.File
import okhttp3.Cache
import okhttp3.OkHttpClient

private const val HTTP_CACHE_SIZE_BYTES = 10L * 1024 * 1024
private const val HTTP_CACHE_DIRECTORY = "character_http"

internal fun createCharactersHttpClient(cacheDirectory: File): OkHttpClient = OkHttpClient.Builder()
    .cache(Cache(File(cacheDirectory, HTTP_CACHE_DIRECTORY), HTTP_CACHE_SIZE_BYTES))
    .build()
