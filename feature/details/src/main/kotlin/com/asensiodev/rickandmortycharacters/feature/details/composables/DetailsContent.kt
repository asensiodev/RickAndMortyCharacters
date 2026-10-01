package com.asensiodev.rickandmortycharacters.feature.details.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.StatusColors
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.details.R
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState

private const val DETAILS_PREVIEW_WIDTH_DP = 412
private const val DETAILS_PREVIEW_HEIGHT_DP = 891

@Composable
fun DetailsContent(
    state: DetailsUiState,
    imageLoader: ImageLoader,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        when (state) {
            is DetailsUiState.Content -> CharacterDetailsContent(state.character, imageLoader)

            DetailsUiState.Loading -> DetailsSkeleton()

            DetailsUiState.Error -> DetailsFeedback(
                stringResource(R.string.detail_error),
                stringResource(R.string.detail_error_description),
                onRetry,
            )

            DetailsUiState.NotFound -> DetailsFeedback(
                stringResource(R.string.detail_not_found),
                stringResource(R.string.detail_not_found_description),
            )
        }
        Surface(
            onClick = onBack,
            modifier = Modifier.padding(Spacing.large).size(DetailsTokens.backTargetSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(DetailsTokens.iconSize),
                )
            }
        }
    }
}

@Composable
private fun CharacterDetailsContent(character: CharacterDetails, imageLoader: ImageLoader) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(
            start = Spacing.large,
            end = Spacing.large,
            top = DetailsTokens.contentTopPadding,
            bottom = Spacing.extraLarge,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge),
    ) {
        DetailsPortrait(character.imageUrl, imageLoader, scrollState)
        DetailsIdentity(character)
        DetailsFacts(character)
    }
}

@Composable
private fun DetailsIdentity(character: CharacterDetails) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(R.drawable.ic_bubble_chart),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(DetailsTokens.smallIconSize),
            )
            Text(
                stringResource(R.string.entity_profile, character.id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            character.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(
                Spacing.small,
                Alignment.CenterHorizontally,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            DetailsStatus(character.status)
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(
                    Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_person),
                        contentDescription = null,
                        modifier = Modifier.size(DetailsTokens.smallIconSize),
                    )
                    Text(
                        displayFact(character.species),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsStatus(status: CharacterStatus) {
    val (background, foreground) = when (status) {
        CharacterStatus.Alive -> StatusColors.aliveContainer to StatusColors.onAliveContainer

        CharacterStatus.Dead -> StatusColors.deadContainer to StatusColors.onDeadContainer

        CharacterStatus.Unknown ->
            MaterialTheme.colorScheme.surfaceContainerHighest to
                MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        CharacterStatus.Alive -> R.string.status_alive
        CharacterStatus.Dead -> R.string.status_dead
        CharacterStatus.Unknown -> R.string.unknown
    }
    Surface(shape = CircleShape, color = background, contentColor = foreground) {
        Row(
            Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (status == CharacterStatus.Dead) {
                Icon(
                    painterResource(R.drawable.ic_skull),
                    contentDescription = null,
                    modifier = Modifier.size(DetailsTokens.smallIconSize),
                )
            }
            Box(Modifier.size(DetailsTokens.statusDotSize).background(foreground, CircleShape))
            Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview(
    name = "Details",
    widthDp = DETAILS_PREVIEW_WIDTH_DP,
    heightDp = DETAILS_PREVIEW_HEIGHT_DP,
    showSystemUi = true,
)
@Composable
private fun DetailsContentPreview(@PreviewParameter(DetailsPreviewStates::class) state: DetailsUiState) {
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
    RickAndMortyTheme { DetailsContent(state, loader, {}, {}) }
}

private class DetailsPreviewStates : PreviewParameterProvider<DetailsUiState> {
    override val values = sequenceOf(
        DetailsUiState.Content(
            CharacterDetails(
                361, "Toxic Rick", CharacterStatus.Dead, "Humanoid", "Male", "Rick's toxic side",
                "Detoxifier", "Earth (Replacement Dimension)", 1, "preview://portrait",
            ),
        ),
        DetailsUiState.Loading,
        DetailsUiState.Error,
        DetailsUiState.NotFound,
    )
}
