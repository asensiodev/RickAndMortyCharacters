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
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeSearchAction
import com.asensiodev.rickandmortycharacters.feature.home.paging.CharactersPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MILLIS = 300L

private const val CHARACTER_PAGE_SIZE = 20
private const val CHARACTER_PREFETCH_DISTANCE = 5

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class HomeViewModel @Inject constructor(repository: CharactersRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(HomePagingState())
    val state: StateFlow<HomePagingState> = mutableState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchAction(action: HomeSearchAction) {
        when (action) {
            is HomeSearchAction.Edit -> updateSearch(action.name)

            HomeSearchAction.Submit -> submitSearch()

            HomeSearchAction.Clear -> clearSearch()

            is HomeSearchAction.Suggest -> {
                updateSearch(action.name)
                submitSearch()
            }
        }
    }

    private fun updateSearch(value: String) {
        mutableState.update { it.copy(searchText = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            applySearch()
        }
    }

    private fun submitSearch() {
        searchJob?.cancel()
        applySearch()
    }

    private fun clearSearch() {
        updateSearch("")
        submitSearch()
    }

    private fun applySearch() {
        mutableState.update { current ->
            val name = current.searchText.trim().takeIf { it.isNotEmpty() }
            if (name == current.appliedName) {
                current
            } else {
                current.copy(
                    appliedName = name,
                    generation = current.generation + 1,
                    totalCount = null,
                )
            }
        }
    }

    val characters: Flow<PagingData<CharacterCardUiModel>> = state
        .map { it.generation to it.appliedName }
        .distinctUntilChanged()
        .flatMapLatest { (generation, name) ->
            Pager(
                config = PagingConfig(
                    pageSize = CHARACTER_PAGE_SIZE,
                    initialLoadSize = CHARACTER_PAGE_SIZE,
                    prefetchDistance = CHARACTER_PREFETCH_DISTANCE,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    CharactersPagingSource(repository, name, generation) { total ->
                        mutableState.update { current ->
                            if (current.generation == generation) current.copy(totalCount = total) else current
                        }
                    }
                },
            ).flow.map { data -> data.map { it.toCard().copy(generation = generation) } }
        }
        .cachedIn(viewModelScope)
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
