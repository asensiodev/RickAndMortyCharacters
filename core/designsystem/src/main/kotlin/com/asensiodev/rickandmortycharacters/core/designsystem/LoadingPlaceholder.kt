package com.asensiodev.rickandmortycharacters.core.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val MIN_LOADING_ALPHA = 0.45f
private const val MAX_LOADING_ALPHA = 0.85f
private const val LOADING_PULSE_DURATION_MILLIS = 900

@Composable
fun LoadingPlaceholder(modifier: Modifier = Modifier, animated: Boolean = true) {
    val motion = if (animated) {
        val opacity = rememberInfiniteTransition(label = "loading-placeholder")
            .animateFloat(
                initialValue = MIN_LOADING_ALPHA,
                targetValue = MAX_LOADING_ALPHA,
                animationSpec = infiniteRepeatable(
                    tween(LOADING_PULSE_DURATION_MILLIS),
                    RepeatMode.Reverse,
                ),
                label = "loading-opacity",
            )
        Modifier.graphicsLayer { alpha = opacity.value }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(motion)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}
