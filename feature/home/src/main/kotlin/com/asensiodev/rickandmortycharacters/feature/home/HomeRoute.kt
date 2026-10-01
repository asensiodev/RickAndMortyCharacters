package com.asensiodev.rickandmortycharacters.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import com.asensiodev.rickandmortycharacters.feature.home.composables.HomeContent
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAction

@Composable
fun HomeRoute(
    imageLoader: ImageLoader,
    onCharacterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeRoute(hiltViewModel<HomeViewModel>(), imageLoader, onCharacterSelected, modifier)
}

@Composable
internal fun HomeRoute(
    viewModel: HomeViewModel,
    imageLoader: ImageLoader,
    onCharacterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) { viewModel.process(HomeAction.Load) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        imageLoader = imageLoader,
        onRetry = { viewModel.process(HomeAction.Retry) },
        onCharacterSelected = onCharacterSelected,
        modifier = modifier,
    )
}
