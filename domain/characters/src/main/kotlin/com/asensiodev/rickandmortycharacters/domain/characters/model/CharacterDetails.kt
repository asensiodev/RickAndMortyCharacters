package com.asensiodev.rickandmortycharacters.domain.characters.model

data class CharacterDetails(
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val gender: String,
    val type: String?,
    val origin: String,
    val location: String,
    val episodeCount: Int,
    val imageUrl: String?,
)
