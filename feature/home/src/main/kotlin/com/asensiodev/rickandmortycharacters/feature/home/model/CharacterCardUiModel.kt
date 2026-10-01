package com.asensiodev.rickandmortycharacters.feature.home.model

import androidx.compose.runtime.Immutable

@Immutable
data class CharacterCardUiModel(
    val id: Int,
    val name: String,
    val species: String,
    val status: CharacterStatusUi,
    val imageUrl: String?,
    val generation: Long = 0,
)

enum class CharacterStatusUi {
    Alive,
    Dead,
    Unknown,
}
