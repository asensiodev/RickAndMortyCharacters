package com.asensiodev.rickandmortycharacters.domain.characters.repository

interface EpisodesRepository {
    suspend fun getEpisodes(episodeIds: List<Int>): EpisodesResult
}
