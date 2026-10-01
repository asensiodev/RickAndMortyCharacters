package com.asensiodev.rickandmortycharacters.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterStatusUi
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAction
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
internal class HomeViewModel @Inject constructor(private val repository: CharactersRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private var initialized = false

    fun process(action: HomeAction) {
        when (action) {
            HomeAction.Load -> if (!initialized) {
                initialized = true
                requestPage()
            }

            HomeAction.Retry -> if (mutableState.value == HomeUiState.Error) requestPage()
        }
    }

    private fun requestPage() {
        mutableState.value = HomeUiState.Loading
        viewModelScope.launch {
            mutableState.value = when (val result = repository.getPage(1)) {
                is CharactersPageResult.Success -> if (result.page.characters.isEmpty()) {
                    HomeUiState.Empty
                } else {
                    HomeUiState.Content(
                        result.page.characters.map {
                            it.toCard()
                        }.toImmutableList(),
                    )
                }

                is CharactersPageResult.Failure -> HomeUiState.Error
            }
        }
    }
}

private fun CharacterSummary.toCard(): CharacterCardUiModel = CharacterCardUiModel(
    id = id,
    name = name,
    species = species,
    status = when (status) {
        CharacterStatus.Alive -> CharacterStatusUi.Alive
        CharacterStatus.Dead -> CharacterStatusUi.Dead
        CharacterStatus.Unknown -> CharacterStatusUi.Unknown
    },
    imageUrl = imageUrl,
)
