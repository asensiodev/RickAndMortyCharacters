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
import com.asensiodev.rickandmortycharacters.feature.home.R
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterStatusUi

private const val CARD_PREVIEW_WIDTH_DP = 180

@Composable
fun CharacterCard(
    character: CharacterCardUiModel,
    imageLoader: ImageLoader,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { onClick(character.id) },
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            CharacterPortrait(character.imageUrl, imageLoader)
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = CharacterCardTokens.METADATA_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                    StatusBadge(character.status)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: CharacterStatusUi) {
    val (background, foreground) = when (status) {
        CharacterStatusUi.Alive ->
            CharacterCardTokens.aliveContainer to
                CharacterCardTokens.onAliveContainer

        CharacterStatusUi.Dead ->
            CharacterCardTokens.deadContainer to
                CharacterCardTokens.onDeadContainer

        CharacterStatusUi.Unknown ->
            MaterialTheme.colorScheme.surfaceContainerHighest to
                MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        CharacterStatusUi.Alive -> R.string.status_alive
        CharacterStatusUi.Dead -> R.string.status_dead
        CharacterStatusUi.Unknown -> R.string.status_unknown
    }
    Row(
        modifier = Modifier.background(background, MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.small, vertical = Spacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(CharacterCardTokens.statusDotSize).background(foreground, CircleShape))
        Text(
            text = stringResource(label),
            color = foreground,
            style = MaterialTheme.typography.labelSmall,
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
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
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
                Modifier.matchParentSize().progressSemantics()
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
                    SuccessResult(ColorImage(android.graphics.Color.DKGRAY), chain.request)
                },
            )
        }.build()
    }
    DisposableEffect(loader) {
        onDispose { loader.shutdown() }
    }
    RickAndMortyTheme {
        CharacterCard(
            CharacterCardUiModel(
                196,
                "Krombopulos Michael",
                "Mythological Creature",
                CharacterStatusUi.Dead,
                "preview://portrait",
            ),
            loader,
            {},
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
            CharacterCardUiModel(
                1,
                "Missing portrait example",
                "Human",
                CharacterStatusUi.Unknown,
                null,
            ),
            loader,
            {
            },
        )
    }
}
