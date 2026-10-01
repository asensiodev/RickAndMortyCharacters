package com.asensiodev.rickandmortycharacters.feature.details.model

import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus

data class CharacterDetailsUiModel(
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
