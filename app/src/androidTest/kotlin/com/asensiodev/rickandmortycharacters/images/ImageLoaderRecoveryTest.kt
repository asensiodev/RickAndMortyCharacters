@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.images

import android.graphics.Color
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ColorImage
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.network.HttpException
import coil3.network.NetworkHeaders
import coil3.network.NetworkResponse
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.request.crossfade
import com.asensiodev.rickandmortycharacters.RickAndMortyApplication
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.home.composables.HomeContent
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageLoaderRecoveryTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val responses = ConcurrentLinkedQueue<(ImageRequest) -> ImageResult>()
    private val requestTimes = CopyOnWriteArrayList<Long>()

    private lateinit var imageLoader: ImageLoader

    @Before
    fun setUp() {
        val productionLoader = RickAndMortyApplication().newImageLoader(context)
        imageLoader = productionLoader.newBuilder()
            .memoryCache(null).diskCache(null).crossfade(false)
            .components {
                productionLoader.components.interceptors.filterIsInstance<RateLimitedImageInterceptor>()
                    .forEach { add(it) }
                add(
                    Interceptor { chain ->
                        requestTimes.add(SystemClock.elapsedRealtime())
                        responses.remove().invoke(chain.request)
                    },
                )
            }.build()
        productionLoader.shutdown()
    }

    @After
    fun tearDown() {
        imageLoader.shutdown()
    }

    @Test
    fun GIVEN_a_rate_limited_portrait_WHEN_the_server_wait_expires_THEN_the_same_request_recovers_once() = runBlocking {
        responses.add { rateLimited(request = it, retryAfter = "1") }
        responses.add { SuccessResult(image = ColorImage(Color.MAGENTA), request = it) }
        val request = ImageRequest.Builder(context).data("test://portrait").size(32, 32).build()

        val result = imageLoader.execute(request)

        assertTrue("Expected success, received $result", result is SuccessResult)
        assertEquals(2, requestTimes.size)
        assertTrue(requestTimes[1] - requestTimes[0] >= 2_000)
    }

    @Test
    fun GIVEN_retry_guidance_that_rounds_to_zero_WHEN_a_portrait_is_rate_limited_THEN_it_waits_before_retrying() = runBlocking {
        responses.add { rateLimited(request = it, retryAfter = "0") }
        responses.add { SuccessResult(image = ColorImage(Color.MAGENTA), request = it) }

        val result = imageLoader.execute(ImageRequest.Builder(context).data("test://portrait").size(32, 32).build())

        assertTrue(result is SuccessResult)
        assertEquals(2, requestTimes.size)
        assertTrue(requestTimes[1] - requestTimes[0] >= 1_000)
    }

    @Test
    fun GIVEN_a_persistent_rate_limit_WHEN_the_retry_also_fails_THEN_the_request_returns_an_error_without_looping() = runBlocking {
        repeat(2) { responses.add { rateLimited(request = it, retryAfter = "0") } }

        val result = imageLoader.execute(ImageRequest.Builder(context).data("test://portrait").size(32, 32).build())

        assertTrue(result is ErrorResult)
        assertEquals(2, requestTimes.size)
    }

    @Test
    fun GIVEN_a_permanent_error_or_excessive_wait_WHEN_a_portrait_is_requested_THEN_it_falls_back_without_retrying_early() = runBlocking {
        val failures = listOf<(ImageRequest) -> ImageResult>(
            { ErrorResult(image = null, request = it, throwable = HttpException(NetworkResponse(code = 404))) },
            { rateLimited(request = it, retryAfter = "61") },
            { rateLimited(request = it, retryAfter = "99999999999999999999") },
        )
        failures.forEachIndexed { index, failure ->
            responses.add(failure)

            val result = withTimeout(2_000) {
                imageLoader.execute(ImageRequest.Builder(context).data("test://portrait/$index").size(32, 32).build())
            }

            assertTrue(result is ErrorResult)
            assertEquals(index + 1, requestTimes.size)
        }
    }

    @Test
    fun GIVEN_a_server_retry_date_WHEN_a_portrait_is_requested_THEN_it_recovers_after_that_date() = runBlocking {
        val retryAt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(2).withNano(0)
        responses.add { rateLimited(request = it, retryAfter = retryAt.format(DateTimeFormatter.RFC_1123_DATE_TIME)) }
        responses.add { SuccessResult(image = ColorImage(Color.MAGENTA), request = it) }

        val result = imageLoader.execute(ImageRequest.Builder(context).data("test://portrait").size(32, 32).build())

        assertTrue(result is SuccessResult)
        assertEquals(2, requestTimes.size)
        assertTrue(System.currentTimeMillis() >= retryAt.toInstant().toEpochMilli())
    }

    @Test
    fun GIVEN_a_rate_limit_without_retry_guidance_WHEN_a_portrait_is_requested_THEN_it_waits_before_retrying_once() = runBlocking {
        responses.add { rateLimited(request = it, retryAfter = null) }
        responses.add { SuccessResult(image = ColorImage(Color.MAGENTA), request = it) }

        val result = imageLoader.execute(ImageRequest.Builder(context).data("test://portrait").size(32, 32).build())

        assertTrue(result is SuccessResult)
        assertEquals(2, requestTimes.size)
        assertTrue(requestTimes[1] - requestTimes[0] >= 10_000)
    }

    @Test
    fun GIVEN_a_portrait_waiting_to_retry_WHEN_its_request_is_cancelled_THEN_it_stops_without_another_attempt() = runBlocking {
        responses.add { rateLimited(request = it, retryAfter = "30") }
        val request = ImageRequest.Builder(context).data("test://portrait").size(32, 32).build()
        val pending = async { imageLoader.execute(request) }
        withTimeout(2_000) {
            while (requestTimes.isEmpty()) delay(10)
        }

        pending.cancelAndJoin()

        assertTrue(pending.isCancelled)
        assertEquals(1, requestTimes.size)
    }

    @Test
    fun GIVEN_a_rate_limited_Home_portrait_WHEN_it_recovers_THEN_the_card_stays_usable_without_another_scroll() {
        responses.add { rateLimited(request = it, retryAfter = "2") }
        responses.add { SuccessResult(image = ColorImage(Color.MAGENTA), request = it) }
        var selectedId: Int? = null
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    state = HomeUiState.Content(
                        characters = listOf(
                            CharacterCardUiModel(
                                id = 361,
                                name = "Toxic Rick",
                                species = "Humanoid",
                                status = CharacterStatus.Dead,
                                imageUrl = "test://portrait",
                            ),
                        ),
                    ),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onCharacterSelected = { selectedId = it },
                )
            }
        }
        compose.waitUntil { requestTimes.isNotEmpty() }
        compose.onNodeWithContentDescription("Loading portrait", useUnmergedTree = true).assertIsDisplayed()

        compose.onNodeWithText("Toxic Rick").assertIsDisplayed().performClick()

        assertEquals(361, selectedId)
        compose.onNodeWithText("Humanoid").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 5_000) {
            requestTimes.size == 2 &&
                compose.onAllNodesWithContentDescription("Loading portrait", useUnmergedTree = true)
                    .fetchSemanticsNodes().isEmpty()
        }
        assertTrue(
            compose.onAllNodesWithContentDescription("Portrait unavailable", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty(),
        )
        compose.onNodeWithText("Toxic Rick").assertIsDisplayed()
        assertEquals(2, requestTimes.size)
    }

    private fun rateLimited(request: ImageRequest, retryAfter: String?): ErrorResult = ErrorResult(
        image = null,
        request = request,
        throwable = HttpException(
            NetworkResponse(
                code = 429,
                headers = NetworkHeaders.Builder().apply {
                    if (retryAfter != null) set("Retry-After", retryAfter)
                }.build(),
            ),
        ),
    )
}
