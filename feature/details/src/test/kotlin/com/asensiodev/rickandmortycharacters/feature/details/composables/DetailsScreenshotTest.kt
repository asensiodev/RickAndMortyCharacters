package com.asensiodev.rickandmortycharacters.feature.details.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.HtmlReportWriter
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import app.cash.paparazzi.SnapshotVerifier
import coil3.ImageLoader
import coil3.asImage
import coil3.intercept.Interceptor
import coil3.request.SuccessResult
import com.android.resources.Density
import com.android.resources.NightMode
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState
import com.asensiodev.rickandmortycharacters.feature.details.model.EpisodesUiState
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Rule
import org.junit.Test

class DetailsScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = phoneDevice(height = 891),
        maxPercentDifference = 0.0,
        snapshotHandler = FinalFrameSnapshotHandler(
            delegate = if (java.lang.Boolean.getBoolean("paparazzi.test.verify")) {
                SnapshotVerifier(maxPercentDifference = 0.0)
            } else {
                HtmlReportWriter(maxPercentDifference = 0.0)
            },
        ),
    )

    private val portrait by lazy {
        requireNotNull(javaClass.getResourceAsStream("/fixtures/rick-sanchez.jpeg")).use { stream ->
            requireNotNull(BitmapFactory.decodeStream(stream)).asImage()
        }
    }

    private val imageLoader by lazy {
        ImageLoader.Builder(paparazzi.context)
            .interceptorCoroutineContext(Dispatchers.Unconfined)
            .components {
                add(
                    Interceptor { chain ->
                        SuccessResult(image = portrait, request = chain.request)
                    },
                )
            }.build()
    }

    @After
    fun closeImageLoader() {
        imageLoader.shutdown()
    }

    @Test
    fun `GIVEN a loaded character WHEN Detail renders THEN the square portrait and facts keep their hierarchy`() {
        val character = CharacterDetails(
            id = 1,
            name = "Rick Sanchez",
            status = CharacterStatus.Alive,
            species = "Human",
            gender = "Male",
            type = null,
            origin = "Earth (C-137)",
            location = "Earth (Replacement Dimension)",
            episodeCount = 51,
            imageUrl = "test://portrait",
        )

        snapshotAtFixedFrame {
            RickAndMortyTheme {
                DetailsContent(
                    state = DetailsUiState.Content(character = character),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun `GIVEN an episode with a long title WHEN episodes render THEN wrapping and the next card remain visible`() {
        val episodes = listOf(
            Episode(
                id = 1,
                name = "Close Rick-counters of the Rick Kind in Another Dimension",
                code = "S01E10",
                airDate = "April 7, 2014",
            ),
            Episode(id = 2, name = "Ricksy Business", code = "S01E11", airDate = "April 14, 2014"),
        )

        snapshotEpisodes(state = EpisodesUiState.Content(episodes = episodes))
    }

    @Test
    fun `GIVEN an episode request error WHEN the section renders THEN its message and retry remain visible`() {
        snapshotEpisodes(state = EpisodesUiState.Error)
    }

    @Test
    fun `GIVEN a pending episode request WHEN a fixed animation frame renders THEN the placeholder keeps its geometry`() {
        snapshotEpisodes(state = EpisodesUiState.Loading)
    }

    private fun snapshotEpisodes(state: EpisodesUiState) {
        paparazzi.unsafeUpdateConfig(deviceConfig = phoneDevice(height = 480))
        snapshotAtFixedFrame {
            RickAndMortyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DetailsEpisodes(state = state, onRetry = {})
                    }
                }
            }
        }
    }

    private fun snapshotAtFixedFrame(content: @Composable () -> Unit) {
        paparazzi.gif(start = 0, end = 1_000, fps = 10, composable = content)
    }

    private fun phoneDevice(height: Int) = DeviceConfig(
        screenWidth = 412,
        screenHeight = height,
        density = Density.MEDIUM,
        xdpi = 160,
        ydpi = 160,
        fontScale = 1f,
        locale = "en",
        nightMode = NightMode.NIGHT,
        softButtons = false,
    )
}

private class FinalFrameSnapshotHandler(private val delegate: SnapshotHandler) : SnapshotHandler {
    override fun newFrameHandler(snapshot: Snapshot, frameCount: Int, fps: Int): SnapshotHandler.FrameHandler {
        val finalFrame = delegate.newFrameHandler(snapshot = snapshot, frameCount = 1, fps = -1)
        return object : SnapshotHandler.FrameHandler {
            private var frame = 0

            override fun handle(image: java.awt.image.BufferedImage) {
                frame++
                if (frame == frameCount) finalFrame.handle(image)
            }

            override fun close() = finalFrame.close()
        }
    }

    override fun close() = delegate.close()
}
