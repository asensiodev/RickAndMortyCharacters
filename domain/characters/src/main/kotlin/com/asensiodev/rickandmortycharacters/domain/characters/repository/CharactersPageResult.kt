package com.asensiodev.rickandmortycharacters.domain.characters.repository

import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage

sealed interface CharactersPageResult {
    data object EndOfCatalogue : CharactersPageResult

    data class Success(val page: CharacterPage) : CharactersPageResult

    data class Failure(val reason: CharacterRequestFailure) : CharactersPageResult
}
