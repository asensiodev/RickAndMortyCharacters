@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal object CharacterCardTokens {
    const val PORTRAIT_ASPECT_RATIO = 1f
    const val NAME_MIN_LINES = 2
    const val EXPANDED_NAME_FONT_SCALE = 1.3f
    const val SKELETON_NAME_WIDTH_FRACTION = 0.85f

    val statusDotSize = 6.dp
    val portraitFallbackSize = 48.dp
    val skeletonNameHeight = 40.dp
    val skeletonMetadataHeight = 18.dp

    val aliveContainer = Color(0xFF143823)
    val onAliveContainer = Color(0xFF86EFAC)
    val deadContainer = Color(0xFF381A1D)
    val onDeadContainer = Color(0xFFFCA5A5)
}
