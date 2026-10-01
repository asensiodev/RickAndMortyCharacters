package com.asensiodev.rickandmortycharacters.feature.details.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import com.asensiodev.rickandmortycharacters.core.designsystem.composables.LoadingPlaceholder
import com.asensiodev.rickandmortycharacters.feature.details.R

@Composable
internal fun DetailsPortrait(imageUrl: String?, imageLoader: ImageLoader, scrollState: ScrollState) {
    val context = LocalContext.current
    val sizeResolver = rememberConstraintsSizeResolver()
    val request = remember(imageUrl, context, sizeResolver) {
        ImageRequest.Builder(context).data(imageUrl).size(sizeResolver).build()
    }
    val painter = rememberAsyncImagePainter(
        request,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
    )
    val state by painter.state.collectAsState()
    var motionEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val scale = coroutineContext[MotionDurationScale]
        snapshotFlow { (scale?.scaleFactor ?: 0f) > 0f }.collect { motionEnabled = it }
    }
    val loadingDescription = stringResource(R.string.portrait_loading)
    Box(
        Modifier.widthIn(max = DetailsTokens.portraitMaxWidth).fillMaxWidth()
            .aspectRatio(DetailsTokens.PORTRAIT_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Image(
            painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().then(sizeResolver).graphicsLayer {
                scaleX = DetailsTokens.PORTRAIT_SCALE
                scaleY = DetailsTokens.PORTRAIT_SCALE
                translationY = if (motionEnabled) {
                    (scrollState.value * DetailsTokens.PARALLAX_FRACTION).coerceAtMost(
                        size.height * DetailsTokens.PARALLAX_LIMIT_FRACTION,
                    )
                } else {
                    0f
                }
            },
        )
        if (imageUrl.isNullOrBlank() || state is AsyncImagePainter.State.Error) {
            Icon(
                painterResource(R.drawable.ic_portrait_placeholder),
                contentDescription = stringResource(R.string.portrait_unavailable),
                modifier = Modifier.align(
                    Alignment.Center,
                ).size(DetailsTokens.portraitFallbackSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (state is AsyncImagePainter.State.Loading) {
            LoadingPlaceholder(
                Modifier.matchParentSize().progressSemantics()
                    .semantics { contentDescription = loadingDescription },
            )
        }
    }
}
