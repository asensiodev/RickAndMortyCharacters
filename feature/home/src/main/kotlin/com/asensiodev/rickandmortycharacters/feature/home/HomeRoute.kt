package com.asensiodev.rickandmortycharacters.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.ImageLoader
import com.asensiodev.rickandmortycharacters.feature.home.composables.CharacterCard
import com.asensiodev.rickandmortycharacters.feature.home.composables.HomeContent
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAppendState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState

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
    val state = when {
        characters.itemCount > 0 -> HomeUiState.Content(
            characters = characters.itemSnapshotList.items,
            totalCount = metadata.totalCount,
            append = when (characters.loadState.append) {
                is LoadState.Loading -> HomeAppendState.Loading
                is LoadState.Error -> HomeAppendState.Error
                is LoadState.NotLoading -> HomeAppendState.Idle
            },
        )

        characters.loadState.refresh is LoadState.Error -> HomeUiState.Error

        characters.loadState.refresh is LoadState.Loading -> HomeUiState.Loading

        else -> HomeUiState.Empty
    }
    HomeContent(
        state = state,
        imageLoader = imageLoader,
        onRetry = characters::retry,
        onCharacterSelected = onCharacterSelected,
        modifier = modifier,
        cardContent = { index, _ ->
            characters[index]?.let { character ->
                CharacterCard(character, imageLoader, onCharacterSelected)
            }
        },
    )
}
