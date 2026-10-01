package com.asensiodev.rickandmortycharacters.data.characters.di

import android.content.Context
import com.asensiodev.rickandmortycharacters.data.characters.remote.CharactersApi
import com.asensiodev.rickandmortycharacters.data.characters.remote.createCharactersApi
import com.asensiodev.rickandmortycharacters.data.characters.remote.createCharactersHttpClient
import com.asensiodev.rickandmortycharacters.data.characters.repository.RemoteCharactersRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

private const val API_BASE_URL = "https://rickandmortyapi.com/api/"

@Module
@InstallIn(SingletonComponent::class)
abstract class CharactersBindings {
    @Binds
    @Singleton
    internal abstract fun repository(implementation: RemoteCharactersRepository): CharactersRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object CharactersNetworkModule {
    @Provides
    @Singleton
    fun client(@ApplicationContext context: Context): OkHttpClient = createCharactersHttpClient(context.cacheDir)

    @Provides
    @Singleton
    fun api(client: OkHttpClient): CharactersApi = createCharactersApi(API_BASE_URL.toHttpUrl(), client)
}
