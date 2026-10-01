package com.asensiodev.rickandmortycharacters.domain.characters.model

data class CharacterSummary(
    val id: Int,
    val name: String,
    val species: String,
    val status: CharacterStatus,
    val imageUrl: String?,
)
