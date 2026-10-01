package com.asensiodev.rickandmortycharacters.data.characters.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class CharacterDetailsDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: CharacterLocationDto,
    val location: CharacterLocationDto,
    val episode: List<String>,
    val type: String? = null,
    val image: String? = null,
)

@Serializable
internal data class CharacterLocationDto(val name: String)

@Serializable
internal data class CharacterErrorDto(val error: String)
