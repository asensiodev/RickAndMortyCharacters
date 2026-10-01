package com.asensiodev.rickandmortycharacters.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.ImageLoader
import com.asensiodev.rickandmortycharacters.feature.home.composables.CharacterCard
import com.asensiodev.rickandmortycharacters.feature.home.composables.HomeContent
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAppendState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import com.asensiodev.rickandmortycharacters.feature.home.paging.CharactersPagingException

@Composable
fun HomeRoute(imageLoader: ImageLoader, onCharacterSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    HomeRoute(hiltViewModel<HomeViewModel>(), imageLoader, onCharacterSelected, modifier)
}

@Composable
internal fun HomeRoute(
    viewModel: HomeViewModel,
    imageLoader: ImageLoader,
    onCharacterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metadata by viewModel.state.collectAsStateWithLifecycle()
    val characters = viewModel.characters.collectAsLazyPagingItems()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val selectCharacter: (Int) -> Unit = {
        focusManager.clearFocus()
        keyboard?.hide()
        onCharacterSelected(it)
    }
    val refreshError = (characters.loadState.refresh as? LoadState.Error)?.error as? CharactersPagingException
    val currentItems = characters.itemSnapshotList.items
    val currentContent = currentItems.firstOrNull()?.generation == metadata.generation
    val state = when {
        currentContent && metadata.totalCount != null -> HomeUiState.Content(
            characters = currentItems,
            totalCount = metadata.totalCount,
            append = when (characters.loadState.append) {
                is LoadState.Loading -> HomeAppendState.Loading
                is LoadState.Error -> HomeAppendState.Error
                is LoadState.NotLoading -> HomeAppendState.Idle
            },
        )

        refreshError?.generation == metadata.generation -> HomeUiState.Error

        metadata.totalCount == 0 && characters.loadState.refresh is LoadState.NotLoading -> HomeUiState.Empty

        else -> HomeUiState.Loading
    }
    HomeContent(
        state = state,
        imageLoader = imageLoader,
        onRetry = characters::retry,
        onCharacterSelected = selectCharacter,
        searchState = metadata,
        onSearchAction = viewModel::onSearchAction,
        modifier = modifier,
        cardContent = { index, _ ->
            characters[index]?.let { character ->
                CharacterCard(character, imageLoader, selectCharacter)
            }
        },
    )
}
