@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.details.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

internal object DetailsTokens {
    const val PORTRAIT_ASPECT_RATIO = 1f
    const val PARALLAX_FRACTION = 0.12f
    const val PARALLAX_LIMIT_FRACTION = 0.04f
    const val PORTRAIT_SCALE = 1.08f
    const val FACT_VALUE_WEIGHT = 1f
    const val SKELETON_FACT_COUNT = 5

    val portraitMaxWidth = 256.dp
    val contentTopPadding = 64.dp
    val backTargetSize = 48.dp
    val iconSize = 20.dp
    val smallIconSize = 16.dp
    val statusDotSize = 6.dp
    val portraitFallbackSize = 48.dp
    val feedbackContainerSize = 80.dp
    val feedbackIconSize = 40.dp
    val retryMinWidth = 140.dp
    val identitySkeletonWidth = 192.dp
    val identitySkeletonHeight = 32.dp
    val factSkeletonHeight = 24.dp
    val factsShape = RoundedCornerShape(16.dp)
}
