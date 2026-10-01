package com.asensiodev.rickandmortycharacters.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil3.ColorImage
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.request.SuccessResult
import com.asensiodev.rickandmortycharacters.core.designsystem.RickAndMortyTheme

private const val CARD_PREVIEW_WIDTH_DP = 180
private const val LARGE_TEXT_PREVIEW_FONT_SCALE = 2f

@Preview(name = "Card", widthDp = CARD_PREVIEW_WIDTH_DP)
@Preview(
    name = "Large text",
    widthDp = CARD_PREVIEW_WIDTH_DP,
    fontScale = LARGE_TEXT_PREVIEW_FONT_SCALE,
)
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
                "Alien",
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

@Preview(name = "Skeleton", widthDp = CARD_PREVIEW_WIDTH_DP)
@Composable
private fun CharacterCardSkeletonPreview() {
    RickAndMortyTheme {
        CharacterCardSkeleton(animated = false)
    }
}
