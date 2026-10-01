package com.asensiodev.rickandmortycharacters.feature.home.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

sealed interface HomeUiState {
    data object Loading : HomeUiState

    @Immutable
    data class Content(val characters: ImmutableList<CharacterCardUiModel>) : HomeUiState

    data object Empty : HomeUiState

    data object Error : HomeUiState
}

sealed interface HomeAction {
    data object Load : HomeAction

    data object Retry : HomeAction
}
