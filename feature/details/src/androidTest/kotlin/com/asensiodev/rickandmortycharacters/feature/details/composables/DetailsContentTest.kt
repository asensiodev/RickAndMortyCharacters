@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.details.composables

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ColorImage
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.details.model.CharacterDetailsUiModel
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailsContentTest {
    @get:Rule
    val compose = createComposeRule()
    private val portraitGate = CompletableDeferred<Unit>()
    private lateinit var imageLoader: ImageLoader

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        imageLoader = ImageLoader.Builder(context).components {
            add(
                Interceptor { chain ->
                    when (chain.request.data) {
                        "test://pending" -> {
                            portraitGate.await()
                            SuccessResult(ColorImage(android.graphics.Color.DKGRAY), chain.request)
                        }

                        "test://failure" -> ErrorResult(
                            null,
                            chain.request,
                            IllegalStateException("Controlled portrait failure"),
                        )

                        else -> SuccessResult(
                            ColorImage(android.graphics.Color.DKGRAY),
                            chain.request,
                        )
                    }
                },
            )
        }.build()
    }

    @After
    fun tearDown() {
        imageLoader.shutdown()
    }

    @Test
    fun GIVEN_character_WHEN_contentRenders_THEN_factsAndBack() {
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(
                    DetailsUiState.Content(toxicRick()),
                    imageLoader,
                    {},
                    { backRequests++ },
                )
            }
        }

        compose.onNodeWithText("Toxic Rick").assertIsDisplayed()
        compose.onNodeWithText("Dead").assertIsDisplayed()
        compose.onNodeWithText("Humanoid").assertIsDisplayed()
        compose.onNodeWithText("Rick's toxic side").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Male").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Detoxifier").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(
            "Earth (Replacement Dimension)",
        ).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Episode appearances").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()
        assertEquals(1, backRequests)
        compose.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun GIVEN_error_WHEN_retryAndBack_THEN_contextualCallbacks() {
        var retries = 0
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(DetailsUiState.Error, imageLoader, { retries++ }, { backRequests++ })
            }
        }

        compose.onNodeWithText("Couldn't load character").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, retries)
        assertEquals(1, backRequests)
    }

    @Test
    fun GIVEN_missingCharacter_WHEN_rendering_THEN_backWithoutRetry() {
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(DetailsUiState.NotFound, imageLoader, {}, { backRequests++ })
            }
        }

        compose.onNodeWithText("Character not found").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backRequests)
    }

    @Test
    fun GIVEN_loading_WHEN_rendering_THEN_skeletonAndPersistentBack() {
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(DetailsUiState.Loading, imageLoader, {}, { backRequests++ })
            }
        }

        compose.onNodeWithContentDescription("Loading character details").assertIsDisplayed()
        compose.onNodeWithText("Toxic Rick").assertDoesNotExist()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backRequests)
    }

    @Test
    fun GIVEN_pendingPortrait_WHEN_loaded_THEN_factsRemain() {
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(
                    DetailsUiState.Content(toxicRick().copy(imageUrl = "test://pending")),
                    imageLoader,
                    {},
                    {},
                )
            }
        }
        compose.waitUntil {
            compose.onAllNodesWithContentDescription(
                "Loading portrait",
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Toxic Rick").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
        compose.onNodeWithText("Detoxifier").performScrollTo().assertIsDisplayed()

        portraitGate.complete(Unit)

        compose.waitUntil {
            compose.onAllNodesWithContentDescription(
                "Loading portrait",
            ).fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText("Detoxifier").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun GIVEN_failedPortrait_WHEN_rendering_THEN_fallbackWithoutDataError() {
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(
                    DetailsUiState.Content(toxicRick().copy(imageUrl = "test://failure")),
                    imageLoader,
                    {},
                    {},
                )
            }
        }

        compose.waitUntil {
            compose.onAllNodesWithContentDescription(
                "Portrait unavailable",
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription(
            "Portrait unavailable",
        ).assertIsDisplayed().assertHasNoClickAction()
        compose.onNodeWithText("Toxic Rick").assertIsDisplayed()
        compose.onNodeWithText("Episode appearances").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Couldn't load character").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun GIVEN_optionalUnknownFacts_WHEN_rendering_THEN_noTypeAndOneEpisodeCount() {
        val location = "Earth (Replacement Dimension), a long location name that wraps naturally"
        val character = toxicRick().copy(
            status = CharacterStatus.Unknown,
            species = "unknown",
            gender = "unknown",
            origin = "unknown",
            location = location,
            type = "",
            episodeCount = 0,
            imageUrl = null,
        )
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(DetailsUiState.Content(character), imageLoader, {}, {})
            }
        }

        compose.onNodeWithContentDescription("Portrait unavailable").assertIsDisplayed()
        compose.onNodeWithText("Type").assertDoesNotExist()
        compose.onNodeWithText("unknown").assertDoesNotExist()
        assertTrue(compose.onAllNodesWithText("Unknown").fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithText(location).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Episode appearances").performScrollTo().assertIsDisplayed()
        assertEquals(1, compose.onAllNodesWithText("0").fetchSemanticsNodes().size)
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    private fun toxicRick(): CharacterDetailsUiModel = CharacterDetailsUiModel(
        361, "Toxic Rick", CharacterStatus.Dead, "Humanoid", "Male", "Rick's toxic side",
        "Detoxifier", "Earth (Replacement Dimension)", 1, "test://portrait",
    )
}
