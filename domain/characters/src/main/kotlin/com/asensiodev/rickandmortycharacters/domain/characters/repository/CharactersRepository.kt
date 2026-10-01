package com.asensiodev.rickandmortycharacters.domain.characters.repository

interface CharactersRepository {
    suspend fun getPage(page: Int): CharactersPageResult

    suspend fun getDetails(characterId: Int): CharacterDetailsResult
}
