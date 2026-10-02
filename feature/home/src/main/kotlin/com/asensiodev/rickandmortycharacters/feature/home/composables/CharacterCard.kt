package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.ColorImage
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.intercept.Interceptor
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.asensiodev.rickandmortycharacters.core.designsystem.composables.LoadingPlaceholder
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.StatusColors
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.home.R
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel

private const val CARD_PREVIEW_WIDTH_DP = 180

@Composable
fun CharacterCard(
    character: CharacterCardUiModel,
    imageLoader: ImageLoader,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openDetailsLabel = stringResource(R.string.open_character_details, character.name)
    Surface(
        onClick = { onClick(character.id) },
        modifier = modifier.semantics { onClick(label = openDetailsLabel, action = null) },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            CharacterPortrait(imageUrl = character.imageUrl, imageLoader = imageLoader)
            Column(
                modifier = Modifier.padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    minLines = CharacterCardTokens.NAME_MIN_LINES,
                    maxLines = if (LocalDensity.current.fontScale >
                        CharacterCardTokens.EXPANDED_NAME_FONT_SCALE
                    ) {
                        Int.MAX_VALUE
                    } else {
                        CharacterCardTokens.NAME_MIN_LINES
                    },
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = character.species,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = CharacterCardTokens.METADATA_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                    StatusBadge(status = character.status)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: CharacterStatus) {
    val (background, foreground) = when (status) {
        CharacterStatus.Alive ->
            StatusColors.aliveContainer to
                StatusColors.onAliveContainer

        CharacterStatus.Dead ->
            StatusColors.deadContainer to
                StatusColors.onDeadContainer

        CharacterStatus.Unknown ->
            MaterialTheme.colorScheme.surfaceContainerHighest to
                MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        CharacterStatus.Alive -> R.string.status_alive
        CharacterStatus.Dead -> R.string.status_dead
        CharacterStatus.Unknown -> R.string.status_unknown
    }
    Row(
        modifier = Modifier.background(color = background, shape = MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.small, vertical = Spacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(
                CharacterCardTokens.statusDotSize,
            ).background(color = foreground, shape = CircleShape),
        )
        Text(
            text = stringResource(label),
            color = foreground,
            style = MaterialTheme.typography.labelMedium,
            maxLines = CharacterCardTokens.METADATA_MAX_LINES,
        )
    }
}

@Composable
private fun CharacterPortrait(imageUrl: String?, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val sizeResolver = rememberConstraintsSizeResolver()
    val request = remember(imageUrl, context, sizeResolver) {
        ImageRequest.Builder(context).data(imageUrl).size(sizeResolver).build()
    }
    val painter = rememberAsyncImagePainter(
        model = request,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
    )
    val state by painter.state.collectAsState()
    val loadingDescription = stringResource(R.string.portrait_loading)
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(CharacterCardTokens.PORTRAIT_ASPECT_RATIO)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .semantics { hideFromAccessibility() },
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().then(sizeResolver),
        )
        if (imageUrl.isNullOrBlank() || state is AsyncImagePainter.State.Error) {
            Icon(
                painter = painterResource(R.drawable.ic_portrait_placeholder),
                contentDescription = stringResource(R.string.portrait_unavailable),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(
                    Alignment.Center,
                ).size(CharacterCardTokens.portraitFallbackSize),
            )
        } else if (state is AsyncImagePainter.State.Loading) {
            LoadingPlaceholder(
                modifier = Modifier.matchParentSize().progressSemantics()
                    .semantics { contentDescription = loadingDescription },
            )
        }
    }
}

@Preview(name = "Card", widthDp = CARD_PREVIEW_WIDTH_DP)
@Composable
private fun CharacterCardPreview() {
    val context = LocalContext.current
    val loader = remember(context) {
        ImageLoader.Builder(context).components {
            add(
                Interceptor { chain ->
                    SuccessResult(image = ColorImage(android.graphics.Color.DKGRAY), request = chain.request)
                },
            )
        }.build()
    }
    DisposableEffect(loader) {
        onDispose { loader.shutdown() }
    }
    RickAndMortyTheme {
        CharacterCard(
            character = CharacterCardUiModel(
                id = 196,
                name = "Krombopulos Michael",
                species = "Mythological Creature",
                status = CharacterStatus.Dead,
                imageUrl = "preview://portrait",
            ),
            imageLoader = loader,
            onClick = {},
        )
    }
}

@Preview(name = "Missing portrait", widthDp = CARD_PREVIEW_WIDTH_DP)
@Composable
private fun MissingPortraitPreview() {
    val context = LocalContext.current
    val loader = remember(context) { ImageLoader.Builder(context).build() }
    DisposableEffect(loader) {
        onDispose { loader.shutdown() }
    }
    RickAndMortyTheme {
        CharacterCard(
            character = CharacterCardUiModel(
                id = 1,
                name = "Missing portrait example",
                species = "Human",
                status = CharacterStatus.Unknown,
                imageUrl = null,
            ),
            imageLoader = loader,
            onClick = {
            },
        )
    }
}
