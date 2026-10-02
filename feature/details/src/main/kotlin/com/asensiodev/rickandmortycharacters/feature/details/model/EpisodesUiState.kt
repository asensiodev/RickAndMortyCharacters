package com.asensiodev.rickandmortycharacters.feature.details.model

import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode

sealed interface EpisodesUiState {
    data object Loading : EpisodesUiState

    data class Content(val episodes: List<Episode>) : EpisodesUiState

    data object Empty : EpisodesUiState

    data object Error : EpisodesUiState
}
