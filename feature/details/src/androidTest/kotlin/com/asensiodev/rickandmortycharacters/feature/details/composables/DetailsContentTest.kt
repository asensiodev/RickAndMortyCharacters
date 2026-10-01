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
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailsContentTest {
    @get:Rule
    val compose = createComposeRule()
    private val portraitGate = CompletableDeferred<Unit>()

    private val imageLoader by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ImageLoader.Builder(context).components {
            add(
                Interceptor { chain ->
                    when (chain.request.data) {
                        "test://pending" -> {
                            portraitGate.await()
                            SuccessResult(image = ColorImage(android.graphics.Color.DKGRAY), request = chain.request)
                        }

                        "test://failure" -> ErrorResult(
                            null,
                            chain.request,
                            IllegalStateException("Controlled portrait failure"),
                        )

                        else -> SuccessResult(
                            image = ColorImage(android.graphics.Color.DKGRAY),
                            request = chain.request,
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
    fun GIVEN_a_character_WHEN_detail_is_rendered_THEN_it_shows_facts_and_allows_back_navigation() {
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(
                    state = DetailsUiState.Content(character = toxicRick()),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onBack = { backRequests++ },
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
    fun GIVEN_a_detail_error_WHEN_retry_and_back_are_tapped_THEN_it_calls_the_matching_callbacks() {
        var retries = 0
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(state = DetailsUiState.Error, imageLoader = imageLoader, onRetry = {
                    retries++
                }, onBack = { backRequests++ })
            }
        }

        compose.onNodeWithText("Couldn't load character").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, retries)
        assertEquals(1, backRequests)
    }

    @Test
    fun GIVEN_a_missing_character_WHEN_detail_is_rendered_THEN_it_allows_back_without_offering_retry() {
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(state = DetailsUiState.NotFound, imageLoader = imageLoader, onRetry = {}, onBack = { backRequests++ })
            }
        }

        compose.onNodeWithText("Character not found").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backRequests)
    }

    @Test
    fun GIVEN_loading_detail_WHEN_it_is_rendered_THEN_it_shows_a_skeleton_and_keeps_back_available() {
        var backRequests = 0
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(state = DetailsUiState.Loading, imageLoader = imageLoader, onRetry = {}, onBack = { backRequests++ })
            }
        }

        compose.onNodeWithContentDescription("Loading character details").assertIsDisplayed()
        compose.onNodeWithText("Toxic Rick").assertDoesNotExist()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backRequests)
    }

    @Test
    fun GIVEN_a_loading_portrait_WHEN_it_finishes_THEN_the_character_facts_remain_visible() {
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(
                    state = DetailsUiState.Content(character = toxicRick().copy(imageUrl = "test://pending")),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onBack = {},
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
    fun GIVEN_a_failed_portrait_WHEN_detail_is_rendered_THEN_it_shows_a_fallback_without_a_data_error() {
        compose.setContent {
            RickAndMortyTheme {
                DetailsContent(
                    state = DetailsUiState.Content(character = toxicRick().copy(imageUrl = "test://failure")),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onBack = {},
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
    fun GIVEN_unknown_facts_WHEN_detail_is_rendered_THEN_it_omits_empty_type_and_shows_one_episode_count() {
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
                DetailsContent(state = DetailsUiState.Content(character = character), imageLoader = imageLoader, onRetry = {}, onBack = {})
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

    private fun toxicRick(): CharacterDetails = CharacterDetails(
        id = 361, name = "Toxic Rick", status = CharacterStatus.Dead, species = "Humanoid", gender = "Male", type = "Rick's toxic side",
        origin = "Detoxifier", location = "Earth (Replacement Dimension)", episodeCount = 1, imageUrl = "test://portrait",
    )
}
