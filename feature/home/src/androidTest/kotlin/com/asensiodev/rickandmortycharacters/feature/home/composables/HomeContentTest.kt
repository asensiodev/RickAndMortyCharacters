@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModelStore
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ColorImage
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.RickAndMortyTheme
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.feature.home.HomeRoute
import com.asensiodev.rickandmortycharacters.feature.home.HomeViewModel
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAppendState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomePagingState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeSearchAction
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeContentTest {
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
                            SuccessResult(ColorImage(android.graphics.Color.DKGRAY), chain.request)
                        }

                        "test://failure" -> ErrorResult(
                            image = null,
                            request = chain.request,
                            throwable = IllegalStateException("Controlled image failure"),
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

    private lateinit var homeViewModel: HomeViewModel

    @After
    fun tearDown() {
        imageLoader.shutdown()
    }

    @Test
    fun GIVEN_visible_filters_WHEN_the_cards_scroll_THEN_the_gap_matches_the_input_gap_and_stays_fixed() {
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    HomeUiState.Content(
                        (1..20).map {
                            CharacterCardUiModel(it, "Character $it", "Human", CharacterStatus.Alive, null)
                        },
                    ),
                    imageLoader,
                    {},
                    {},
                )
            }
        }

        val inputBounds = compose.onNodeWithContentDescription("Search characters").fetchSemanticsNode().boundsInRoot
        val filterBounds = compose.onNodeWithText("All").fetchSemanticsNode().boundsInRoot
        val firstBounds = compose.onNodeWithText("Character 1").fetchSemanticsNode().boundsInRoot
        val expectedGap = filterBounds.top - inputBounds.bottom
        assertEquals(expectedGap, firstBounds.top - filterBounds.bottom, 1f)

        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(4)

        val scrolledBounds = compose.onNodeWithText("Character 5").fetchSemanticsNode().boundsInRoot
        val retainedFilterBounds = compose.onNodeWithText("All").fetchSemanticsNode().boundsInRoot
        assertEquals(filterBounds.top, retainedFilterBounds.top, 1f)
        assertEquals(expectedGap, scrolledBounds.top - retainedFilterBounds.bottom, 1f)
    }

    @Test
    fun GIVEN_a_visible_append_error_WHEN_retry_loads_and_fails_again_THEN_feedback_stays_in_place_above_the_counter() {
        verifyStableAppendFeedback(1f)
    }

    @Test
    fun GIVEN_large_text_and_an_append_error_WHEN_retry_loads_and_fails_again_THEN_feedback_stays_visible_in_place() {
        verifyStableAppendFeedback(2f)
    }

    @Test
    fun GIVEN_a_long_species_WHEN_Home_is_rendered_THEN_it_uses_ellipsis_and_keeps_status_on_the_same_row() {
        val species = "Mythological Creature from another dimension"
        compose.setContent {
            RickAndMortyTheme {
                Box(Modifier.width(HomeLayoutTokens.twoColumnMinWidth)) {
                    HomeContent(
                        HomeUiState.Content(
                            listOf(CharacterCardUiModel(1, "Mr. Booby Buyer", species, CharacterStatus.Alive, null)),
                        ),
                        imageLoader,
                        {},
                        {},
                    )
                }
            }
        }

        val speciesNode = compose.onNodeWithText(species, useUnmergedTree = true).assertIsDisplayed()
        val statusNode = compose.onAllNodesWithText("Alive", useUnmergedTree = true)[1].assertIsDisplayed()
        val speciesBounds = speciesNode.fetchSemanticsNode().boundsInRoot
        val statusBounds = statusNode.fetchSemanticsNode().boundsInRoot
        val textLayouts = mutableListOf<TextLayoutResult>()
        speciesNode.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(textLayouts) }

        assertTrue(speciesBounds.top < statusBounds.bottom && statusBounds.top < speciesBounds.bottom)
        assertEquals(1, textLayouts.single().lineCount)
        assertTrue(textLayouts.single().isLineEllipsized(0))
    }

    @Test
    fun GIVEN_loaded_characters_WHEN_the_keyboard_opens_THEN_it_hides_the_counter_until_dismissal() {
        compose.setContent {
            var query by androidx.compose.runtime.remember { mutableStateOf("") }
            RickAndMortyTheme {
                Column {
                    BasicTextField(query, { query = it }, Modifier.testTag("keyboard-input"))
                    Box(Modifier.weight(1f)) {
                        HomeContent(
                            HomeUiState.Content(
                                listOf(
                                    CharacterCardUiModel(
                                        1,
                                        "Rick",
                                        "Human",
                                        CharacterStatus.Alive,
                                        null,
                                    ),
                                ),
                                40,
                            ),
                            imageLoader,
                            {},
                            {},
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Loaded 1 of 40 characters").assertIsDisplayed()

        compose.onNodeWithTag("keyboard-input").performClick().performTextInput("Rick")
        compose.waitUntil {
            compose.onAllNodesWithText("Loaded 1 of 40 characters").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("keyboard-input").assertIsDisplayed()
        Espresso.closeSoftKeyboard()
        compose.waitUntil {
            compose.onAllNodesWithText(
                "Loaded 1 of 40 characters",
            ).fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithText("Loaded 1 of 40 characters").assertIsDisplayed()
    }

    @Test
    fun GIVEN_a_counter_WHEN_load_states_change_THEN_it_is_visible_only_with_loaded_content() {
        val characters = listOf(
            CharacterCardUiModel(1, "Rick", "Human", CharacterStatus.Alive, null),
        )
        var state by mutableStateOf<HomeUiState>(HomeUiState.Content(characters, 40))
        compose.setContent {
            RickAndMortyTheme { HomeContent(state, imageLoader, {}, {}) }
        }

        compose.onNodeWithText("Loaded 1 of 40 characters").assertIsDisplayed()
        compose.runOnIdle { state = HomeUiState.Content(characters, 40, HomeAppendState.Loading) }
        compose.onNodeWithText("Loaded 1 of 40 characters").assertIsDisplayed()
        compose.onNodeWithContentDescription("Loading more characters").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.runOnIdle { state = HomeUiState.Content(characters, 40, HomeAppendState.Error) }
        compose.onNodeWithText("Loaded 1 of 40 characters").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertIsDisplayed()
        compose.runOnIdle { state = HomeUiState.Content(characters) }
        compose.onNodeWithText("Loaded 1 of 40 characters").assertDoesNotExist()
        for (initial in listOf(HomeUiState.Loading, HomeUiState.Empty, HomeUiState.Error)) {
            compose.runOnIdle { state = initial }
            compose.onNodeWithText("Loaded 1 of 40 characters").assertDoesNotExist()
        }
    }

    @Test
    fun GIVEN_large_text_WHEN_scrolling_to_the_end_THEN_the_last_card_and_retry_remain_above_the_counter() {
        compose.setContent {
            val density = LocalDensity.current
            RickAndMortyTheme {
                Box(Modifier.width(HomeLayoutTokens.twoColumnMinWidth - Spacing.extraLarge)) {
                    CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                        HomeContent(
                            HomeUiState.Content(
                                (1..20).map {
                                    CharacterCardUiModel(
                                        it,
                                        "Character $it",
                                        "Human",
                                        CharacterStatus.Alive,
                                        null,
                                    )
                                },
                                40,
                                HomeAppendState.Error,
                            ),
                            imageLoader,
                            {},
                            {},
                        )
                    }
                }
            }
        }

        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 20"))
        compose.onNodeWithText("Character 20").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        compose.onNodeWithText("Retry").assertIsDisplayed()
        val retryBounds = compose.onNodeWithText("Retry").fetchSemanticsNode().boundsInRoot
        val counterBounds = compose.onNodeWithText(
            "Loaded 20 of 40 characters",
        ).fetchSemanticsNode().boundsInRoot

        assertTrue(retryBounds.bottom <= counterBounds.top)
    }

    @Test
    fun GIVEN_an_append_failure_WHEN_scrolling_and_retrying_THEN_it_retains_cards_and_increases_the_count() {
        val requestedPages = mutableListOf<Int>()
        val failedRetryGate = CompletableDeferred<Unit>()
        val responseGate = CompletableDeferred<Unit>()
        var selectedId: Int? = null
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
                requestedPages += page
                if (page == 2) {
                    val attempt = requestedPages.count { it == 2 }
                    if (attempt == 2) failedRetryGate.await()
                    if (attempt <= 2) return CharactersPageResult.Failure(CharacterRequestFailure.Network)
                    responseGate.await()
                }
                return CharactersPageResult.Success(
                    CharacterPage(
                        ((page - 1) * 20 + 1..page * 20).map {
                            CharacterSummary(it, "Character $it", "Human", CharacterStatus.Alive, null)
                        },
                        40,
                        if (page == 1) 2 else null,
                    ),
                )
            }
        }

        homeViewModel = HomeViewModel(repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent {
                RickAndMortyTheme {
                    HomeRoute(homeViewModel, imageLoader, { selectedId = it })
                }
            }
            compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 19"))
            compose.waitUntil {
                compose.onAllNodesWithText("Couldn't load more characters").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Loaded 20 of 40 characters").assertIsDisplayed()

            compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
            val retryBounds = compose.onNodeWithText("Retry").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            compose.onNodeWithText("Retry").performTouchInput {
                click()
                click()
            }
            compose.waitUntil { requestedPages == listOf(1, 2, 2) }
            compose.onNodeWithContentDescription("Loading more characters").assertIsDisplayed()
            compose.onNodeWithText("Retry").assertDoesNotExist()
            compose.runOnIdle { assertEquals(listOf(1, 2, 2), requestedPages) }
            compose.runOnIdle { failedRetryGate.complete(Unit) }
            compose.waitUntil { compose.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Retry").assertIsDisplayed()
            assertEquals(retryBounds, compose.onNodeWithText("Retry").fetchSemanticsNode().boundsInRoot)
            compose.onNodeWithText("Loaded 20 of 40 characters").assertIsDisplayed()

            compose.onNodeWithText("Retry").performTouchInput {
                click()
                click()
            }
            compose.waitUntil { requestedPages == listOf(1, 2, 2, 2) }
            compose.onNodeWithContentDescription("Loading more characters").assertIsDisplayed()
            compose.onNodeWithText("Retry").assertDoesNotExist()
            compose.runOnIdle { responseGate.complete(Unit) }
            compose.waitUntil {
                compose.onAllNodesWithText("Loaded 40 of 40 characters").fetchSemanticsNodes().isNotEmpty()
            }

            compose.onNodeWithText("Couldn't load more characters").assertDoesNotExist()
            compose.onNodeWithContentDescription("Loading more characters").assertDoesNotExist()
            compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 40"))
            compose.onNodeWithText("Character 40").performClick()
            assertEquals(40, selectedId)
            assertEquals(listOf(1, 2, 2, 2), requestedPages)
        } finally {
            compose.runOnIdle { store.clear() }
        }
    }

    @Test
    fun GIVEN_twenty_loaded_characters_WHEN_only_four_fit_THEN_the_counter_still_reports_twenty() {
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    HomeUiState.Content(
                        (1..20).map {
                            CharacterCardUiModel(
                                it,
                                "Character $it",
                                "Human",
                                CharacterStatus.Alive,
                                null,
                            )
                        },
                        totalCount = 40,
                    ),
                    imageLoader,
                    {},
                    {},
                )
            }
        }

        compose.onNodeWithText(
            "Loaded 20 of 40 characters",
        ).assertIsDisplayed().assertHasNoClickAction()
        compose.onNodeWithText("Loaded 4 of 40 characters").assertDoesNotExist()
    }

    @Test
    fun GIVEN_an_append_failure_WHEN_retry_is_tapped_THEN_it_keeps_cards_and_requests_footer_recovery() {
        var retries = 0
        var selectedId: Int? = null
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    HomeUiState.Content(
                        listOf(
                            CharacterCardUiModel(1, "Rick", "Human", CharacterStatus.Alive, null),
                        ),
                        totalCount = 40,
                        append = HomeAppendState.Error,
                    ),
                    imageLoader,
                    { retries++ },
                    { selectedId = it },
                )
            }
        }

        compose.onNodeWithText("Rick").assertIsDisplayed().performClick()
        compose.onNodeWithText(
            "Couldn't load more characters",
        ).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Retry").performScrollTo().assertIsDisplayed().performClick()

        assertEquals(1, selectedId)
        assertEquals(1, retries)
    }

    @Test
    fun GIVEN_loading_or_failed_portraits_WHEN_cards_are_selected_THEN_metadata_and_character_IDs_remain_available() {
        val selected = mutableListOf<Int>()
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    HomeUiState.Content(
                        listOf(
                            CharacterCardUiModel(
                                1,
                                "Rick Sanchez",
                                "Human",
                                CharacterStatus.Alive,
                                "test://pending",
                            ),
                            CharacterCardUiModel(
                                2,
                                "Morty Smith",
                                "Human",
                                CharacterStatus.Alive,
                                "test://failure",
                            ),
                            CharacterCardUiModel(
                                3,
                                "Summer Smith",
                                "Human",
                                CharacterStatus.Unknown,
                                null,
                            ),
                        ),
                    ),
                    imageLoader,
                    {},
                    { selected += it },
                )
            }
        }

        compose.waitUntil {
            compose.onAllNodesWithContentDescription("Portrait unavailable", useUnmergedTree = true)
                .fetchSemanticsNodes().size == 2
        }
        compose.onNodeWithContentDescription(
            "Loading portrait",
            useUnmergedTree = true,
        ).assertIsDisplayed()
        compose.onNodeWithText("Rick Sanchez").assertIsDisplayed().performClick()
        compose.onNodeWithText("Morty Smith").assertIsDisplayed().performClick()
        compose.onNodeWithText("Summer Smith").assertIsDisplayed().performClick()
        assertEquals(listOf(1, 2, 3), selected)

        portraitGate.complete(Unit)
        compose.waitUntil {
            compose.onAllNodesWithContentDescription("Loading portrait", useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText("Rick Sanchez").assertIsDisplayed().performClick()
        assertEquals(listOf(1, 2, 3, 1), selected)
    }

    @Test
    fun GIVEN_a_narrow_screen_with_large_text_WHEN_an_error_is_shown_THEN_retry_remains_reachable() {
        var retries = 0
        compose.setContent {
            val density = LocalDensity.current
            RickAndMortyTheme {
                Box(Modifier.width(HomeLayoutTokens.twoColumnMinWidth - Spacing.extraLarge)) {
                    CompositionLocalProvider(
                        LocalDensity provides Density(density.density, fontScale = 2f),
                    ) {
                        HomeContent(HomeUiState.Error, imageLoader, { retries++ }, {})
                    }
                }
            }
        }

        compose.onNodeWithText("Retry").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(1, retries)
    }

    @Test
    fun GIVEN_a_failed_Home_request_WHEN_retry_succeeds_THEN_the_route_displays_received_characters() {
        var requests = 0
        val responseGate = CompletableDeferred<Unit>()
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
                requests++
                return if (requests == 1) {
                    CharactersPageResult.Failure(CharacterRequestFailure.Service)
                } else {
                    responseGate.await()
                    CharactersPageResult.Success(
                        CharacterPage(
                            listOf(
                                CharacterSummary(
                                    1,
                                    "Rick Sanchez",
                                    "Human",
                                    CharacterStatus.Alive,
                                    null,
                                ),
                            ),
                            1,
                            null,
                        ),
                    )
                }
            }
        }

        homeViewModel = HomeViewModel(repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent { RickAndMortyTheme { HomeRoute(homeViewModel, imageLoader, {}) } }
            compose.onNodeWithText("Retry").assertIsDisplayed().performClick()
            compose.onNodeWithText("Retry").assertDoesNotExist()
            assertTrue(
                compose.onAllNodesWithContentDescription(
                    "Loading character",
                ).fetchSemanticsNodes().isNotEmpty(),
            )
            compose.runOnIdle {
                assertEquals(2, requests)
            }

            responseGate.complete(Unit)
            compose.waitUntil {
                compose.onAllNodesWithContentDescription(
                    "Portrait unavailable",
                ).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Rick Sanchez").assertIsDisplayed()
        } finally {
            compose.runOnIdle { store.clear() }
        }
    }

    @Test
    fun GIVEN_an_empty_catalogue_WHEN_Home_is_rendered_THEN_it_shows_feedback_without_actions() {
        compose.setContent {
            RickAndMortyTheme { HomeContent(HomeUiState.Empty, imageLoader, {}, {}) }
        }

        compose.onNodeWithText("No characters available.").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.onNodeWithText("Back").assertDoesNotExist()
    }

    @Test
    fun GIVEN_a_loading_catalogue_WHEN_Home_is_rendered_THEN_it_shows_noninteractive_skeletons() {
        compose.setContent {
            RickAndMortyTheme { HomeContent(HomeUiState.Loading, imageLoader, {}, {}) }
        }

        val skeletons = compose.onAllNodesWithContentDescription("Loading character")
        assertTrue(skeletons.fetchSemanticsNodes().isNotEmpty())
        skeletons[0].assertIsDisplayed().assertHasNoClickAction()
        compose.onNodeWithText("Rick Sanchez").assertDoesNotExist()
        compose.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun GIVEN_a_Home_error_WHEN_retry_is_tapped_THEN_it_requests_retry_without_navigating() {
        var retries = 0
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(HomeUiState.Error, imageLoader, { retries++ }, {})
            }
        }

        compose.onNodeWithText("Couldn't load characters").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertIsDisplayed().performClick()
        assertEquals(1, retries)
        compose.onNodeWithText("Back").assertDoesNotExist()
    }

    @Test
    fun GIVEN_loaded_characters_WHEN_a_card_is_selected_THEN_it_emits_that_character_ID() {
        var selectedId: Int? = null
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    state = HomeUiState.Content(
                        listOf(
                            CharacterCardUiModel(
                                1,
                                "Rick Sanchez",
                                "Human",
                                CharacterStatus.Alive,
                                "test://rick",
                            ),
                        ),
                    ),
                    imageLoader = imageLoader,
                    onRetry = {},
                    onCharacterSelected = { selectedId = it },
                )
            }
        }

        compose.onNodeWithText("Rick Sanchez").assertIsDisplayed().performClick()
        compose.onAllNodesWithText("Alive")[1].assertIsDisplayed()
        assertEquals(1, selectedId)
    }

    private fun verifyStableAppendFeedback(fontScale: Float) {
        var append by mutableStateOf<HomeAppendState>(HomeAppendState.Error)
        var retries = 0
        val characters = (1..20).map {
            CharacterCardUiModel(it, "Character $it", "Human", CharacterStatus.Alive, null)
        }
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                RickAndMortyTheme {
                    Box(Modifier.width(HomeLayoutTokens.twoColumnMinWidth + Spacing.extraLarge)) {
                        HomeContent(
                            HomeUiState.Content(characters, 40, append),
                            imageLoader,
                            {
                                retries++
                                append = HomeAppendState.Loading
                            },
                            {},
                        )
                    }
                }
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        val retryBounds = compose.onNodeWithText("Retry").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val counterBounds = compose.onNodeWithText("Loaded 20 of 40 characters").fetchSemanticsNode().boundsInRoot
        assertTrue(retryBounds.bottom <= counterBounds.top)

        compose.onNodeWithText("Retry").performTouchInput { click() }

        assertEquals(1, retries)
        val loadingBounds = compose.onNodeWithContentDescription("Loading more characters")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(loadingBounds.bottom <= counterBounds.top)
        assertEquals(retryBounds.center.y, loadingBounds.center.y, 1f)
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.runOnIdle { append = HomeAppendState.Error }
        compose.onNodeWithText("Retry").assertIsDisplayed()
        assertEquals(retryBounds, compose.onNodeWithText("Retry").fetchSemanticsNode().boundsInRoot)
        assertEquals(counterBounds, compose.onNodeWithText("Loaded 20 of 40 characters").fetchSemanticsNode().boundsInRoot)
    }
}
