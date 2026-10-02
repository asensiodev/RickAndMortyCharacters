package com.asensiodev.rickandmortycharacters.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesResult
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsAction
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState
import com.asensiodev.rickandmortycharacters.feature.details.model.EpisodesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
internal class DetailsViewModel @Inject constructor(
    private val repository: CharactersRepository,
    private val episodesRepository: EpisodesRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val state: StateFlow<DetailsUiState> = mutableState.asStateFlow()

    private var characterId: Int? = null

    fun process(action: DetailsAction) {
        when (action) {
            is DetailsAction.Load -> if (characterId == null) {
                characterId = action.characterId
                requestDetails(id = action.characterId)
            }

            DetailsAction.RetryEpisodes -> {
                val content = mutableState.value as? DetailsUiState.Content ?: return
                if (content.episodes == EpisodesUiState.Error) {
                    mutableState.value = content.copy(episodes = EpisodesUiState.Loading)
                    viewModelScope.launch {
                        val updatedContent = if (content.hasIncompleteEpisodeReferences()) {
                            val result = repository.getDetails(characterId = content.character.id)
                            if (result !is CharacterDetailsResult.Success) {
                                mutableState.value = content.copy(episodes = EpisodesUiState.Error)
                                return@launch
                            }
                            content.copy(character = result.character)
                        } else {
                            content
                        }
                        requestEpisodes(content = updatedContent)
                    }
                }
            }

            DetailsAction.Retry -> {
                val id = characterId ?: return
                if (mutableState.value == DetailsUiState.Error) requestDetails(id = id)
            }
        }
    }
    private fun requestDetails(id: Int) {
        mutableState.value = DetailsUiState.Loading
        viewModelScope.launch {
            mutableState.value = when (val result = repository.getDetails(characterId = id)) {
                is CharacterDetailsResult.Success -> DetailsUiState.Content(character = result.character)
                CharacterDetailsResult.NotFound -> DetailsUiState.NotFound
                is CharacterDetailsResult.Failure -> DetailsUiState.Error
            }
            val content = mutableState.value as? DetailsUiState.Content ?: return@launch
            requestEpisodes(content = content)
        }
    }

    private suspend fun requestEpisodes(content: DetailsUiState.Content) {
        val episodes = when {
            content.hasIncompleteEpisodeReferences() -> EpisodesUiState.Error

            content.character.episodeIds.isEmpty() -> EpisodesUiState.Empty

            else -> {
                mutableState.value = content.copy(episodes = EpisodesUiState.Loading)
                when (val result = episodesRepository.getEpisodes(episodeIds = content.character.episodeIds)) {
                    is EpisodesResult.Success -> if (result.episodes.isEmpty()) {
                        EpisodesUiState.Empty
                    } else {
                        EpisodesUiState.Content(episodes = result.episodes)
                    }

                    is EpisodesResult.Failure -> EpisodesUiState.Error
                }
            }
        }
        mutableState.value = content.copy(episodes = episodes)
    }

    private fun DetailsUiState.Content.hasIncompleteEpisodeReferences(): Boolean =
        character.episodeIds.size != character.episodeCount
}
