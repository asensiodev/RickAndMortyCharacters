package com.asensiodev.rickandmortycharacters.feature.details.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import com.asensiodev.rickandmortycharacters.core.designsystem.composables.LoadingPlaceholder
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.feature.details.R

@Composable
internal fun DetailsSkeleton() {
    val description = stringResource(R.string.detail_loading)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(
            start = Spacing.large,
            end = Spacing.large,
            top = DetailsTokens.contentTopPadding,
            bottom = Spacing.extraLarge,
        ).clearAndSetSemantics {
            contentDescription = description
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge),
    ) {
        LoadingPlaceholder(
            modifier = Modifier.widthIn(max = DetailsTokens.portraitMaxWidth).fillMaxWidth()
                .aspectRatio(DetailsTokens.PORTRAIT_ASPECT_RATIO).clip(MaterialTheme.shapes.large),
        )
        LoadingPlaceholder(
            modifier = Modifier.width(DetailsTokens.identitySkeletonWidth)
                .height(DetailsTokens.identitySkeletonHeight).clip(MaterialTheme.shapes.small),
        )
        LoadingPlaceholder(
            modifier = Modifier.width(DetailsTokens.identitySkeletonWidth)
                .height(DetailsTokens.factSkeletonHeight).clip(MaterialTheme.shapes.small),
        )
        Surface(
            Modifier.fillMaxWidth(),
            shape = DetailsTokens.factsShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                Modifier.padding(Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge),
            ) {
                repeat(DetailsTokens.SKELETON_FACT_COUNT) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.extraLarge)) {
                        LoadingPlaceholder(
                            modifier = Modifier.weight(DetailsTokens.FACT_VALUE_WEIGHT)
                                .height(DetailsTokens.factSkeletonHeight)
                                .clip(MaterialTheme.shapes.small),
                        )
                        LoadingPlaceholder(
                            modifier = Modifier.weight(DetailsTokens.FACT_VALUE_WEIGHT)
                                .height(DetailsTokens.factSkeletonHeight)
                                .clip(MaterialTheme.shapes.small),
                        )
                    }
                }
            }
        }
    }
}
