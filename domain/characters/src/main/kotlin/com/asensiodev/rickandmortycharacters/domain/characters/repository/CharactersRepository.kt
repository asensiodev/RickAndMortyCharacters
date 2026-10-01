package com.asensiodev.rickandmortycharacters.domain.characters.repository

import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus

interface CharactersRepository {
    suspend fun getPage(page: Int, name: String? = null, status: CharacterStatus? = null): CharactersPageResult

    suspend fun getDetails(characterId: Int): CharacterDetailsResult
}
