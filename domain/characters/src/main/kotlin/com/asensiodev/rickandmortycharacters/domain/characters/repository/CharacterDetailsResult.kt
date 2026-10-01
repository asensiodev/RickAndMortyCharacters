package com.asensiodev.rickandmortycharacters.domain.characters.repository

import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails

sealed interface CharacterDetailsResult {
    data class Success(val character: CharacterDetails) : CharacterDetailsResult

    data object NotFound : CharacterDetailsResult

    data class Failure(val reason: CharacterRequestFailure) : CharacterDetailsResult
}
