package com.asensiodev.rickandmortycharacters.data.characters.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class CharacterPageDto(val info: PageInfoDto, val results: List<CharacterDto>)

@Serializable
internal data class PageInfoDto(val count: Int, val next: String?)

@Serializable
internal data class CharacterDto(
    val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val image: String? = null,
)
