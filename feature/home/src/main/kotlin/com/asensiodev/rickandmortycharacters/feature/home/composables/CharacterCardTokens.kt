@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.ui.unit.dp
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.StatusColors

internal object CharacterCardTokens {
    const val PORTRAIT_ASPECT_RATIO = 1f
    const val NAME_MIN_LINES = 2
    const val METADATA_MAX_LINES = 1
    const val EXPANDED_NAME_FONT_SCALE = 1.3f
    const val SKELETON_NAME_WIDTH_FRACTION = 0.85f

    val statusDotSize = 6.dp
    val portraitFallbackSize = 48.dp
    val skeletonNameHeight = 40.dp
    val skeletonMetadataHeight = 18.dp

    val aliveContainer = StatusColors.aliveContainer
    val onAliveContainer = StatusColors.onAliveContainer
    val deadContainer = StatusColors.deadContainer
    val onDeadContainer = StatusColors.onDeadContainer
}
