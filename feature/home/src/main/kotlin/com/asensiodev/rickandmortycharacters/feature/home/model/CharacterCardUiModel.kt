package com.asensiodev.rickandmortycharacters.feature.home.model

import androidx.compose.runtime.Immutable
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus

@Immutable
data class CharacterCardUiModel(
    val id: Int,
    val name: String,
    val species: String,
    val status: CharacterStatus,
    val imageUrl: String?,
    val generation: Long = 0,
)
