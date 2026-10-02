package com.asensiodev.rickandmortycharacters.feature.home.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import com.android.resources.Density
import com.android.resources.NightMode
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAppendState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomePagingState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Rule
import org.junit.Test

class HomeScreenshotTest {
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
                        if (chain.request.data == "test://failure") {
                            ErrorResult(null, chain.request, IllegalStateException("Controlled portrait failure"))
                        } else {
                            SuccessResult(image = portrait, request = chain.request)
                        }
                    },
                )
            }.build()
    }

    @After
    fun closeImageLoader() {
        imageLoader.shutdown()
    }

    @Test
    fun `GIVEN a long name and species WHEN the card renders THEN wrapping and badge space are preserved`() {
        val character = character(
            id = 1,
            name = "Rick Sanchez from the Replacement Dimension",
            species = "Humanoid from another dimension",
        )

        snapshotComponent {
            CharacterCard(character = character, imageLoader = imageLoader, onClick = {}, modifier = Modifier.width(184.dp))
        }
    }

    @Test
    fun `GIVEN an unknown status and failed portrait WHEN the card renders THEN metadata and fallback remain visible`() {
        val character = CharacterCardUiModel(
            id = 2,
            name = "Unknown Rick",
            species = "Humanoid",
            status = CharacterStatus.Unknown,
            imageUrl = "test://failure",
        )

        snapshotComponent {
            CharacterCard(character = character, imageLoader = imageLoader, onClick = {}, modifier = Modifier.width(184.dp))
        }
    }

    @Test
    fun `GIVEN a loading card WHEN a fixed animation frame renders THEN skeleton geometry is preserved`() {
        snapshotComponent {
            CharacterCardSkeleton(modifier = Modifier.width(184.dp))
        }
    }

    @Test
    fun `GIVEN a constrained query with no matches WHEN Home renders THEN controls and suggestions remain visible`() {
        snapshotAtFixedFrame {
            RickAndMortyTheme {
                HomeContent(
                    state = HomeUiState.Empty,
                    searchState = HomePagingState(
                        searchText = "Nobody",
                        appliedName = "Nobody",
                        selectedStatus = CharacterStatus.Alive,
                    ),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onCharacterSelected = {},
                )
            }
        }
    }

    @Test
    fun `GIVEN loaded results and an append error WHEN Home renders THEN retry and counter keep their clearance`() {
        val characters = listOf(
            character(id = 1, name = "Rick Sanchez"),
            character(id = 2, name = "Rick Prime"),
            character(id = 3, name = "Tiny Rick"),
            character(id = 4, name = "Pickle Rick"),
        )

        snapshotAtFixedFrame {
            RickAndMortyTheme {
                HomeContent(
                    state = HomeUiState.Content(characters = characters, totalCount = 826, append = HomeAppendState.Error),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onCharacterSelected = {},
                )
            }
        }
    }

    private fun snapshotComponent(content: @Composable () -> Unit) {
        paparazzi.unsafeUpdateConfig(deviceConfig = phoneDevice(height = 480))
        snapshotAtFixedFrame {
            RickAndMortyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.padding(16.dp)) { content() }
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

    private fun character(id: Int, name: String, species: String = "Human") = CharacterCardUiModel(
        id = id,
        name = name,
        species = species,
        status = CharacterStatus.Alive,
        imageUrl = "test://portrait",
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
