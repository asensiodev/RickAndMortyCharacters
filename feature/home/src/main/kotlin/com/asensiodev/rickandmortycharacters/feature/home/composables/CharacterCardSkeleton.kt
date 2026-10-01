package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.tooling.preview.Preview
import com.asensiodev.rickandmortycharacters.core.designsystem.composables.LoadingPlaceholder
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.feature.home.R

private const val SKELETON_PREVIEW_WIDTH_DP = 180

@Composable
fun CharacterCardSkeleton(modifier: Modifier = Modifier, animated: Boolean = true) {
    val description = stringResource(R.string.character_loading)
    Surface(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = description
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            LoadingPlaceholder(
                Modifier.fillMaxWidth().aspectRatio(CharacterCardTokens.PORTRAIT_ASPECT_RATIO),
                animated,
            )
            Column(
                modifier = Modifier.padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                LoadingPlaceholder(
                    Modifier.fillMaxWidth(CharacterCardTokens.SKELETON_NAME_WIDTH_FRACTION)
                        .height(CharacterCardTokens.skeletonNameHeight)
                        .clip(MaterialTheme.shapes.small),
                    animated,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    LoadingPlaceholder(
                        Modifier.weight(1f)
                            .height(CharacterCardTokens.skeletonMetadataHeight)
                            .clip(MaterialTheme.shapes.small),
                        animated,
                    )
                    LoadingPlaceholder(
                        Modifier.weight(1f)
                            .height(CharacterCardTokens.skeletonMetadataHeight)
                            .clip(MaterialTheme.shapes.small),
                        animated,
                    )
                }
            }
        }
    }
}

@Preview(name = "Skeleton", widthDp = SKELETON_PREVIEW_WIDTH_DP)
@Composable
private fun CharacterCardSkeletonPreview() {
    RickAndMortyTheme {
        CharacterCardSkeleton(animated = false)
    }
}
