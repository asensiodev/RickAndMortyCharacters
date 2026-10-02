package com.asensiodev.rickandmortycharacters.domain.characters.repository

import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode

sealed interface EpisodesResult {
    data class Success(val episodes: List<Episode>) : EpisodesResult

    data class Failure(val reason: CharacterRequestFailure) : EpisodesResult
}
