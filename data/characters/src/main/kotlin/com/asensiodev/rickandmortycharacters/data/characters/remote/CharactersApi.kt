package com.asensiodev.rickandmortycharacters.data.characters.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal val CharactersJson = Json { ignoreUnknownKeys = true }

internal interface CharactersApi {
    @GET("character")
    suspend fun getPage(
        @Query("page") page: Int,
        @Query("name") name: String?,
        @Query("status") status: String?,
    ): Response<CharacterPageDto>

    @GET("episode/{ids}")
    suspend fun getEpisodes(@Path("ids") episodeIds: String): Response<JsonElement>

    @GET("character/{id}")
    suspend fun getDetails(@Path("id") characterId: Int): Response<CharacterDetailsDto>
}

internal fun createCharactersApi(baseUrl: HttpUrl, client: OkHttpClient): CharactersApi = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(client)
    .addConverterFactory(CharactersJson.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(CharactersApi::class.java)
