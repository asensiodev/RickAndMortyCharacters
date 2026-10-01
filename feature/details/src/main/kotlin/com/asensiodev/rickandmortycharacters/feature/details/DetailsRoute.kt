package com.asensiodev.rickandmortycharacters.feature.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import com.asensiodev.rickandmortycharacters.feature.details.composables.DetailsContent
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsAction

@Composable
fun DetailsRoute(characterId: Int, imageLoader: ImageLoader, onBack: () -> Unit, modifier: Modifier = Modifier) {
    DetailsRoute(hiltViewModel<DetailsViewModel>(), characterId, imageLoader, onBack, modifier)
}

@Composable
internal fun DetailsRoute(
    viewModel: DetailsViewModel,
    characterId: Int,
    imageLoader: ImageLoader,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel, characterId) { viewModel.process(DetailsAction.Load(characterId)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    DetailsContent(state, imageLoader, { viewModel.process(DetailsAction.Retry) }, onBack, modifier)
}
