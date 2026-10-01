package com.asensiodev.rickandmortycharacters.feature.home.composables

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import coil3.ColorImage
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.request.SuccessResult
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.home.R
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAppendState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomePagingState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeSearchAction
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState

private const val PHONE_PREVIEW_WIDTH_DP = 412
private const val PHONE_PREVIEW_HEIGHT_DP = 891
private const val NARROW_PREVIEW_WIDTH_DP = 320
private const val HOME_PREVIEW_FONT_SCALE = 2f

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    imageLoader: ImageLoader,
    onRetry: () -> Unit,
    onCharacterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    searchState: HomePagingState = HomePagingState(),
    onSearchAction: (HomeSearchAction) -> Unit = {},
    cardContent: (@Composable (Int, CharacterCardUiModel) -> Unit)? = null,
) {
    val gridState = rememberLazyGridState()
    var scrolledGeneration by rememberSaveable { mutableLongStateOf(searchState.generation) }
    LaunchedEffect(searchState.generation) {
        if (scrolledGeneration != searchState.generation) {
            gridState.scrollToItem(0)
            scrolledGeneration = searchState.generation
        }
    }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val searchAction: (HomeSearchAction) -> Unit = { action ->
        onSearchAction(action)
        if (action is HomeSearchAction.Submit || action is HomeSearchAction.Suggest) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    val selectCharacter: (Int) -> Unit = { id ->
        focusManager.clearFocus()
        keyboard?.hide()
        onCharacterSelected(id)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        HomeSearchField(
            searchState.searchText,
            searchAction,
            Modifier.padding(start = Spacing.large, top = Spacing.large, end = Spacing.large),
        )
        HomeStatusFilters(
            searchState.selectedStatus,
            { searchAction(HomeSearchAction.SelectStatus(it)) },
            Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small),
        )
        HomeResultsPanel(
            if (scrolledGeneration == searchState.generation) state else HomeUiState.Loading,
            searchState,
            gridState,
            onRetry,
            searchAction,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { index, character ->
            if (cardContent == null) {
                CharacterCard(character, imageLoader, selectCharacter)
            } else {
                cardContent(index, character)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeResultsPanel(
    state: HomeUiState,
    searchState: HomePagingState,
    gridState: LazyGridState,
    onRetry: () -> Unit,
    onSearchAction: (HomeSearchAction) -> Unit,
    modifier: Modifier = Modifier,
    cardContent: @Composable (Int, CharacterCardUiModel) -> Unit,
) {
    BoxWithConstraints(modifier) {
        var counterHeightPx by rememberSaveable { mutableIntStateOf(0) }
        val content = state as? HomeUiState.Content
        val showCounter = content != null && content.characters.isNotEmpty() &&
            content.totalCount != null && !WindowInsets.isImeVisible
        val bottomPadding = if (showCounter) {
            with(LocalDensity.current) { counterHeightPx.toDp() } + Spacing.large * 2
        } else {
            Spacing.large
        }
        val columns = if (
            maxWidth < HomeLayoutTokens.twoColumnMinWidth ||
            LocalDensity.current.fontScale > HomeLayoutTokens.EXPANDED_TEXT_FONT_SCALE
        ) {
            HomeLayoutTokens.NARROW_COLUMNS
        } else {
            HomeLayoutTokens.ORDINARY_COLUMNS
        }
        val feedbackModifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .heightIn(min = maxHeight)
            .padding(Spacing.extraLarge)
        HomeResults(
            state, columns, bottomPadding, feedbackModifier, onRetry, gridState,
            searchState.appliedName != null || searchState.selectedStatus != null, onSearchAction,
            cardContent,
        )
        if (showCounter) {
            HomeLoadedCounter(
                loadedCount = content.characters.size,
                totalCount = content.totalCount,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Spacing.large)
                    .widthIn(max = maxWidth - Spacing.large * 2)
                    .onSizeChanged { counterHeightPx = it.height },
            )
        }
    }
}

@Composable
private fun HomeResults(
    state: HomeUiState,
    columns: Int,
    bottomPadding: Dp,
    feedbackModifier: Modifier,
    onRetry: () -> Unit,
    gridState: LazyGridState,
    hasQueryConstraint: Boolean,
    onSearchAction: (HomeSearchAction) -> Unit,
    cardContent: @Composable (Int, CharacterCardUiModel) -> Unit,
) {
    when (state) {
        is HomeUiState.Content -> HomeCharacterGrid(
            state,
            columns,
            bottomPadding,
            onRetry,
            gridState,
            cardContent,
        )

        HomeUiState.Loading -> HomeGrid(columns, gridState) {
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
            title = stringResource(if (hasQueryConstraint) R.string.catalogue_no_matches else R.string.catalogue_empty),
            description = stringResource(
                if (hasQueryConstraint) {
                    R.string.catalogue_no_matches_description
                } else {
                    R.string.catalogue_empty_description
                },
            ),
            icon = R.drawable.ic_travel_explore,
            modifier = feedbackModifier,
        ) {
            if (hasQueryConstraint) HomeSearchSuggestions(onSearchAction)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeSearchSuggestions(onAction: (HomeSearchAction) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        listOf(R.string.suggest_rick, R.string.suggest_morty, R.string.suggest_beth, R.string.suggest_summer)
            .forEach { resource ->
                val name = stringResource(resource)
                OutlinedButton(onClick = { onAction(HomeSearchAction.Suggest(name)) }) { Text(name) }
            }
    }
}

@Composable
private fun HomeCharacterGrid(
    state: HomeUiState.Content,
    columns: Int,
    bottomPadding: Dp,
    onRetry: () -> Unit,
    gridState: LazyGridState,
    cardContent: @Composable (Int, CharacterCardUiModel) -> Unit,
) {
    HomeGrid(columns, gridState, bottomPadding = bottomPadding) {
        itemsIndexed(
            state.characters,
            key = { _, character ->
                character.id
            },
            contentType = { _, _ -> "character" },
        ) { index, character ->
            cardContent(index, character)
        }
        if (state.append != HomeAppendState.Idle) {
            item(
                key = "pagination",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "pagination",
            ) {
                HomeAppendFooter(state.append, onRetry)
            }
        }
    }
}

@Composable
private fun HomeGrid(
    columns: Int,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = Spacing.large,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.large,
            end = Spacing.large,
            bottom = bottomPadding,
        ),
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
private fun HomeAppendFooter(state: HomeAppendState, onRetry: () -> Unit) {
    val loading = state == HomeAppendState.Loading
    val feedbackModifier = if (loading) Modifier.alpha(0f).clearAndSetSemantics {} else Modifier
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Text(
            stringResource(R.string.catalogue_append_error),
            modifier = feedbackModifier,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Box(contentAlignment = Alignment.Center) {
            HomeRetryButton(onRetry, modifier = feedbackModifier, enabled = !loading)
            if (loading) {
                val description = stringResource(R.string.catalogue_append_loading)
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(HomeLayoutTokens.paginationProgressSize)
                        .semantics { contentDescription = description },
                )
            }
        }
    }
}

@Composable
private fun HomeRetryButton(onRetry: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onRetry,
        enabled = enabled,
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
                    SuccessResult(ColorImage(Color.DKGRAY), chain.request)
                },
            )
        }.build()
    }
    DisposableEffect(loader) { onDispose { loader.shutdown() } }
    RickAndMortyTheme { HomeContent(state, loader, {}, {}) }
}

private class HomePreviewStates : PreviewParameterProvider<HomeUiState> {
    private val content = HomeUiState.Content(
        listOf(
            CharacterCardUiModel(
                1,
                "Rick Sanchez",
                "Human",
                CharacterStatus.Alive,
                "preview://rick",
            ),
            CharacterCardUiModel(
                196,
                "Krombopulos Michael",
                "Alien",
                CharacterStatus.Dead,
                null,
            ),
        ),
        totalCount = 57,
    )
    override val values = sequenceOf(
        HomeUiState.Loading,
        content,
        content.copy(append = HomeAppendState.Loading),
        content.copy(append = HomeAppendState.Error),
        HomeUiState.Empty,
        HomeUiState.Error,
    )
}
