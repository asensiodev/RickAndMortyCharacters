package com.asensiodev.rickandmortycharacters.domain.characters.repository

interface CharactersRepository {
    suspend fun getPage(page: Int, name: String? = null): CharactersPageResult

    suspend fun getDetails(characterId: Int): CharacterDetailsResult
}
