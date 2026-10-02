package com.asensiodev.rickandmortycharacters.feature.details.model

import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails

sealed interface DetailsUiState {
    data object Loading : DetailsUiState

    data class Content(val character: CharacterDetails, val episodes: EpisodesUiState = EpisodesUiState.Empty) :
        DetailsUiState

    data object Error : DetailsUiState

    data object NotFound : DetailsUiState
}

sealed interface DetailsAction {
    data class Load(val characterId: Int) : DetailsAction

    data object Retry : DetailsAction

    data object RetryEpisodes : DetailsAction
}
