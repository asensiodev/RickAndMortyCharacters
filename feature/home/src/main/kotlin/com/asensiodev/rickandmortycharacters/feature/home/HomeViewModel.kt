package com.asensiodev.rickandmortycharacters.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterStatusUi
import com.asensiodev.rickandmortycharacters.feature.home.model.HomePagingState
import com.asensiodev.rickandmortycharacters.feature.home.paging.CharactersPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private const val CHARACTER_PAGE_SIZE = 20
private const val CHARACTER_PREFETCH_DISTANCE = 5

@HiltViewModel
internal class HomeViewModel @Inject constructor(repository: CharactersRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(HomePagingState())
    val state: StateFlow<HomePagingState> = mutableState.asStateFlow()

    val characters: Flow<PagingData<CharacterCardUiModel>> = Pager(
        config = PagingConfig(
            pageSize = CHARACTER_PAGE_SIZE,
            initialLoadSize = CHARACTER_PAGE_SIZE,
            prefetchDistance = CHARACTER_PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            mutableState.value = HomePagingState()
            CharactersPagingSource(repository) { total ->
                mutableState.value = HomePagingState(total)
            }
        },
    ).flow.map { data -> data.map { it.toCard() } }.cachedIn(viewModelScope)
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
