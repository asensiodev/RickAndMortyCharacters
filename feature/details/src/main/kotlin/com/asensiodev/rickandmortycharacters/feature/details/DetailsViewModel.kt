package com.asensiodev.rickandmortycharacters.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.feature.details.model.CharacterDetailsUiModel
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsAction
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
internal class DetailsViewModel @Inject constructor(private val repository: CharactersRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val state: StateFlow<DetailsUiState> = mutableState.asStateFlow()

    private var characterId: Int? = null

    fun process(action: DetailsAction) {
        when (action) {
            is DetailsAction.Load -> if (characterId == null) {
                characterId = action.characterId
                requestDetails(action.characterId)
            }

            DetailsAction.Retry -> {
                val id = characterId ?: return
                if (mutableState.value == DetailsUiState.Error) requestDetails(id)
            }
        }
    }
    private fun requestDetails(id: Int) {
        mutableState.value = DetailsUiState.Loading
        viewModelScope.launch {
            mutableState.value = when (val result = repository.getDetails(id)) {
                is CharacterDetailsResult.Success -> DetailsUiState.Content(result.character.toUi())
                CharacterDetailsResult.NotFound -> DetailsUiState.NotFound
                is CharacterDetailsResult.Failure -> DetailsUiState.Error
            }
        }
    }
}

private fun CharacterDetails.toUi(): CharacterDetailsUiModel = CharacterDetailsUiModel(
    id, name, status, species, gender, type, origin, location, episodeCount, imageUrl,
)
