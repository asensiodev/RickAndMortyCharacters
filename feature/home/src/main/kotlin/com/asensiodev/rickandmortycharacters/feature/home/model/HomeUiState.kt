package com.asensiodev.rickandmortycharacters.feature.home.model

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Content(
        val characters: List<CharacterCardUiModel>,
        val totalCount: Int? = null,
        val append: HomeAppendState = HomeAppendState.Idle,
    ) : HomeUiState

    data object Empty : HomeUiState

    data object Error : HomeUiState
}
