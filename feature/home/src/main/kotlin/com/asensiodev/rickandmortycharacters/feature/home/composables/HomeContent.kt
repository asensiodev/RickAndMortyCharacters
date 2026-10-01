package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import coil3.ColorImage
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.request.SuccessResult
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.feature.home.R
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterStatusUi
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import kotlinx.collections.immutable.persistentListOf

private const val PHONE_PREVIEW_WIDTH_DP = 412
private const val PHONE_PREVIEW_HEIGHT_DP = 891
private const val NARROW_PREVIEW_WIDTH_DP = 320
private const val HOME_PREVIEW_FONT_SCALE = 2f

@Composable
fun HomeContent(
    state: HomeUiState,
    imageLoader: ImageLoader,
    onRetry: () -> Unit,
    onCharacterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        val columns = if (
            maxWidth < HomeLayoutTokens.twoColumnMinWidth ||
            LocalDensity.current.fontScale > HomeLayoutTokens.EXPANDED_TEXT_FONT_SCALE
        ) {
            HomeLayoutTokens.NARROW_COLUMNS
        } else {
            HomeLayoutTokens.ORDINARY_COLUMNS
        }
        val feedbackModifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .heightIn(min = maxHeight).padding(Spacing.extraLarge)
        when (state) {
            is HomeUiState.Content -> HomeGrid(columns) {
                items(state.characters, key = {
                    it.id
                }, contentType = { "character" }) { character ->
                    CharacterCard(character, imageLoader, onCharacterSelected)
                }
            }

            HomeUiState.Loading -> HomeGrid(columns) {
                items(HomeLayoutTokens.SKELETON_COUNT, contentType = { "skeleton" }) {
                    CharacterCardSkeleton()
                }
            }

            HomeUiState.Error -> HomeFeedback(
                title = stringResource(R.string.catalogue_error),
                description = stringResource(R.string.catalogue_error_description),
                icon = R.drawable.ic_error,
                modifier = feedbackModifier,
            ) {
                HomeRetryButton(onRetry)
            }

            HomeUiState.Empty -> HomeFeedback(
                title = stringResource(R.string.catalogue_empty),
                description = stringResource(R.string.catalogue_empty_description),
                icon = R.drawable.ic_travel_explore,
                modifier = feedbackModifier,
            )
        }
    }
}

@Composable
private fun HomeGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.large),
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
        content = content,
    )
}

@Composable
private fun HomeFeedback(
    title: String,
    description: String,
    icon: Int,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge, Alignment.CenterVertically),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Box(
                Modifier.size(HomeLayoutTokens.feedbackContainerSize),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(HomeLayoutTokens.feedbackIconSize),
                )
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action()
    }
}

@Composable
private fun HomeRetryButton(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onRetry,
        modifier = modifier.sizeIn(
            minWidth = HomeLayoutTokens.retryMinWidth,
            minHeight = HomeLayoutTokens.retryMinHeight,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Text(stringResource(R.string.retry))
    }
}

@Preview(
    name = "Home",
    widthDp = PHONE_PREVIEW_WIDTH_DP,
    heightDp = PHONE_PREVIEW_HEIGHT_DP,
    showSystemUi = true,
)
@Preview(
    name = "Narrow, large text",
    widthDp = NARROW_PREVIEW_WIDTH_DP,
    heightDp = PHONE_PREVIEW_HEIGHT_DP,
    fontScale = HOME_PREVIEW_FONT_SCALE,
    showSystemUi = true,
)
@Composable
private fun HomeContentPreview(@PreviewParameter(HomePreviewStates::class) state: HomeUiState) {
    val context = LocalContext.current
    val loader = remember(context) {
        ImageLoader.Builder(context).components {
            add(
                Interceptor { chain ->
                    SuccessResult(ColorImage(android.graphics.Color.DKGRAY), chain.request)
                },
            )
        }.build()
    }
    DisposableEffect(loader) { onDispose { loader.shutdown() } }
    RickAndMortyTheme { HomeContent(state, loader, {}, {}) }
}

private class HomePreviewStates : PreviewParameterProvider<HomeUiState> {
    override val values = sequenceOf(
        HomeUiState.Loading,
        HomeUiState.Content(
            persistentListOf(
                CharacterCardUiModel(
                    1,
                    "Rick Sanchez",
                    "Human",
                    CharacterStatusUi.Alive,
                    "preview://rick",
                ),
                CharacterCardUiModel(
                    196,
                    "Krombopulos Michael",
                    "Alien",
                    CharacterStatusUi.Dead,
                    null,
                ),
            ),
        ),
        HomeUiState.Empty,
        HomeUiState.Error,
    )
}
