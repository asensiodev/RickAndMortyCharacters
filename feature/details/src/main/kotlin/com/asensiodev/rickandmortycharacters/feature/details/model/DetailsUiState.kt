package com.asensiodev.rickandmortycharacters.feature.details.model

sealed interface DetailsUiState {
    data object Loading : DetailsUiState

    data class Content(val character: CharacterDetailsUiModel) : DetailsUiState

    data object Error : DetailsUiState

    data object NotFound : DetailsUiState
}

sealed interface DetailsAction {
    data class Load(val characterId: Int) : DetailsAction

    data object Retry : DetailsAction
}
