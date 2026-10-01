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
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.click
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
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterStatusUi
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAppendState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomePagingState
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeSearchAction
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import kotlinx.collections.immutable.persistentListOf
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
    fun GIVEN_a_failed_name_request_WHEN_Retry_is_tapped_twice_THEN_it_retries_that_name_once_and_keeps_the_input() {
        val names = mutableListOf<String?>()
        val retryGate = CompletableDeferred<Unit>()
        var failSearch = true
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int, name: String?): CharactersPageResult {
                names += name
                if (name != null && failSearch) {
                    failSearch = false
                    return CharactersPageResult.Failure(CharacterRequestFailure.Service)
                }
                if (name != null) retryGate.await()
                return CharactersPageResult.Success(
                    CharacterPage(
                        listOf(CharacterSummary(1, "Result ${name ?: "All"}", "Human", CharacterStatus.Alive, null)),
                        1,
                        null,
                    ),
                )
            }
        }
        homeViewModel = HomeViewModel(repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent { RickAndMortyTheme { HomeRoute(homeViewModel, imageLoader, {}) } }
            compose.onNodeWithContentDescription("Search characters").performClick().performTextInput("Rick")
            compose.onNodeWithContentDescription("Search characters").performImeAction()
            compose.waitUntil { compose.onAllNodesWithText("Couldn't load characters").fetchSemanticsNodes().isNotEmpty() }

            compose.onNodeWithText("Retry").performTouchInput {
                click()
                click()
            }
            compose.waitUntil { names.size == 3 }

            compose.onNodeWithText("Rick").assertIsDisplayed()
            compose.onNodeWithText("Retry").assertDoesNotExist()
            compose.runOnIdle { assertEquals(listOf(null, "Rick", "Rick"), names) }
            compose.runOnIdle { retryGate.complete(Unit) }
            compose.waitUntil { compose.onAllNodesWithText("Result Rick").fetchSemanticsNodes().isNotEmpty() }
        } finally {
            store.clear()
        }
    }

    @Test
    fun GIVEN_remote_search_WHEN_submitting_and_choosing_a_suggestion_THEN_it_uses_each_name_once_and_clear_removes_it() {
        val names = mutableListOf<String?>()
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int, name: String?): CharactersPageResult {
                names += name
                return CharactersPageResult.Success(
                    CharacterPage(
                        if (name == "Missing") {
                            emptyList()
                        } else {
                            listOf(
                                CharacterSummary(1, "Result ${name ?: "All"}", "Human", CharacterStatus.Alive, null),
                            )
                        },
                        if (name == "Missing") 0 else 1,
                        null,
                    ),
                )
            }
        }
        homeViewModel = HomeViewModel(repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent { RickAndMortyTheme { HomeRoute(homeViewModel, imageLoader, {}) } }
            compose.onNodeWithText("Result All").assertIsDisplayed()

            compose.onNodeWithContentDescription("Search characters").performClick().performTextInput("Missing")
            compose.onNodeWithContentDescription("Search characters").performImeAction()
            compose.waitUntil { compose.onAllNodesWithText("No characters found").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Rick").performClick()
            compose.waitUntil { compose.onAllNodesWithText("Result Rick").fetchSemanticsNodes().isNotEmpty() }

            compose.onNodeWithContentDescription("Search characters").assertIsNotFocused()
            compose.onNodeWithText("Loaded 1 of 1 characters").assertIsDisplayed()
            compose.runOnIdle { assertEquals(listOf(null, "Missing", "Rick"), names) }
            compose.onNodeWithContentDescription("Search characters").performClick()
            compose.onNodeWithContentDescription("Clear search").performClick()
            compose.waitUntil { compose.onAllNodesWithText("Result All").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithContentDescription("Search characters").assertIsFocused()
            compose.runOnIdle { assertEquals(listOf(null, "Missing", "Rick", null), names) }
        } finally {
            store.clear()
        }
    }

    @Test
    fun GIVEN_a_loaded_catalogue_WHEN_a_query_is_pending_THEN_old_cards_and_totals_are_hidden_until_the_new_result() {
        val pending = CompletableDeferred<Unit>()
        val names = mutableListOf<String?>()
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int, name: String?): CharactersPageResult {
                names += name
                if (name != null) pending.await()
                return CharactersPageResult.Success(
                    CharacterPage(
                        listOf(CharacterSummary(1, if (name == null) "Old card" else "New card", "Human", CharacterStatus.Alive, null)),
                        if (name == null) 50 else 1,
                        null,
                    ),
                )
            }
        }
        homeViewModel = HomeViewModel(repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent { RickAndMortyTheme { HomeRoute(homeViewModel, imageLoader, {}) } }
            compose.onNodeWithText("Old card").assertIsDisplayed()

            compose.onNodeWithContentDescription("Search characters").performClick().performTextInput("Rick")
            compose.onNodeWithContentDescription("Search characters").performImeAction()
            compose.waitUntil { names.size == 2 }

            compose.onNodeWithText("Old card").assertDoesNotExist()
            compose.onNodeWithText("Loaded 1 of 50 characters").assertDoesNotExist()
            compose.onAllNodesWithContentDescription("Loading character").fetchSemanticsNodes().also { assertTrue(it.isNotEmpty()) }
            compose.runOnIdle { pending.complete(Unit) }
            compose.waitUntil { compose.onAllNodesWithText("New card").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Loaded 1 of 1 characters").assertIsDisplayed()
        } finally {
            store.clear()
        }
    }

    @Test
    fun GIVEN_no_name_matches_WHEN_a_suggestion_is_selected_THEN_it_submits_on_Home_without_Retry_or_Back() {
        var selectedName: String? = null
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    HomeUiState.Empty,
                    imageLoader,
                    {},
                    {},
                    searchState = HomePagingState(searchText = "Missing", appliedName = "Missing"),
                    onSearchAction = { action -> if (action is HomeSearchAction.Suggest) selectedName = action.name },
                )
            }
        }

        compose.onNodeWithText("No characters found").assertIsDisplayed()
        compose.onNodeWithText("Rick").performClick()

        assertEquals("Rick", selectedName)
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test
    fun GIVEN_search_input_WHEN_results_fail_THEN_it_preserves_text_and_focus_and_keeps_Retry_reachable() {
        var results: HomeUiState by mutableStateOf(HomeUiState.Loading)
        var search by mutableStateOf(HomePagingState())
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(results, imageLoader, {}, {}, searchState = search, onSearchAction = { action ->
                    if (action is HomeSearchAction.Edit) search = search.copy(searchText = action.name)
                })
            }
        }

        compose.onNodeWithText("Search characters").performClick().performTextInput("Rick")
        compose.onNodeWithText("Rick").performSemanticsAction(SemanticsActions.SetSelection) { it(1, 3, false) }
        for (state in listOf(HomeUiState.Empty, HomeUiState.Error, HomeUiState.Loading)) {
            compose.runOnIdle { results = state }

            compose.onNodeWithText("Rick").assertIsFocused()
            assertEquals(
                TextRange(1, 3),
                compose.onNodeWithText("Rick").fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange],
            )
        }
        compose.runOnIdle { results = HomeUiState.Error }
        compose.onNodeWithText("Couldn't load characters").assertIsDisplayed()
        compose.onNodeWithText("Retry").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun GIVEN_a_long_species_WHEN_Home_is_rendered_THEN_it_uses_ellipsis_and_keeps_status_on_the_same_row() {
        val species = "Mythological Creature from another dimension"
        compose.setContent {
            RickAndMortyTheme {
                Box(Modifier.width(HomeLayoutTokens.twoColumnMinWidth)) {
                    HomeContent(
                        HomeUiState.Content(
                            listOf(CharacterCardUiModel(1, "Mr. Booby Buyer", species, CharacterStatusUi.Alive, null)),
                        ),
                        imageLoader,
                        {},
                        {},
                    )
                }
            }
        }

        val speciesNode = compose.onNodeWithText(species, useUnmergedTree = true).assertIsDisplayed()
        val statusNode = compose.onNodeWithText("Alive", useUnmergedTree = true).assertIsDisplayed()
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
                                        CharacterStatusUi.Alive,
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
            CharacterCardUiModel(1, "Rick", "Human", CharacterStatusUi.Alive, null),
        )
        var state by mutableStateOf<HomeUiState>(HomeUiState.Content(characters, 40))
        compose.setContent {
            RickAndMortyTheme { HomeContent(state, imageLoader, {}, {}) }
        }

        compose.onNodeWithText("Loaded 1 of 40 characters").assertIsDisplayed()
        compose.runOnIdle { state = HomeUiState.Content(characters, 40, HomeAppendState.Loading) }
        compose.onNodeWithText("Loaded 1 of 40 characters").assertIsDisplayed()
        compose.onNodeWithContentDescription("Loading more characters").assertIsDisplayed()
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
                                        CharacterStatusUi.Alive,
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
        compose.onNodeWithText("Retry").performScrollTo().assertIsDisplayed()
        val retryBounds = compose.onNodeWithText("Retry").fetchSemanticsNode().boundsInRoot
        val counterBounds = compose.onNodeWithText(
            "Loaded 20 of 40 characters",
        ).fetchSemanticsNode().boundsInRoot

        assertTrue(retryBounds.bottom <= counterBounds.top)
    }

    @Test
    fun GIVEN_an_append_failure_WHEN_scrolling_and_retrying_THEN_it_retains_cards_and_increases_the_count() {
        val requestedPages = mutableListOf<Int>()
        val responseGate = CompletableDeferred<Unit>()
        var failAppend = true
        var selectedId: Int? = null
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int, name: String?): CharactersPageResult {
                requestedPages += page
                if (page == 2 && failAppend) {
                    failAppend = false
                    return CharactersPageResult.Failure(CharacterRequestFailure.Network)
                }
                if (page == 2) responseGate.await()
                return CharactersPageResult.Success(
                    CharacterPage(
                        ((page - 1) * 20 + 1..page * 20).map {
                            CharacterSummary(
                                it,
                                "Character $it",
                                "Human",
                                CharacterStatus.Alive,
                                null,
                            )
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
                    HomeRoute(homeViewModel, imageLoader, {
                        selectedId =
                            it
                    })
                }
            }
            compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 19"))
            compose.waitUntil {
                compose.onAllNodesWithText(
                    "Couldn't load more characters",
                ).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Loaded 20 of 40 characters").assertIsDisplayed()

            compose.onNodeWithText("Retry").performScrollTo().performTouchInput {
                click()
                click()
            }
            compose.onNodeWithContentDescription("Loading more characters").assertIsDisplayed()
            compose.onNodeWithText("Retry").assertDoesNotExist()
            compose.runOnIdle { assertEquals(listOf(1, 2, 2), requestedPages) }
            compose.runOnIdle { responseGate.complete(Unit) }
            compose.waitUntil {
                compose.onAllNodesWithText(
                    "Loaded 40 of 40 characters",
                ).fetchSemanticsNodes().isNotEmpty()
            }

            compose.onNodeWithText("Couldn't load more characters").assertDoesNotExist()
            compose.onNodeWithContentDescription("Loading more characters").assertDoesNotExist()
            compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 40"))
            compose.onNodeWithText("Character 40").performClick()
            assertEquals(40, selectedId)
            assertEquals(listOf(1, 2, 2), requestedPages)
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
                                CharacterStatusUi.Alive,
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
                        persistentListOf(
                            CharacterCardUiModel(1, "Rick", "Human", CharacterStatusUi.Alive, null),
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
                        persistentListOf(
                            CharacterCardUiModel(
                                1,
                                "Rick Sanchez",
                                "Human",
                                CharacterStatusUi.Alive,
                                "test://pending",
                            ),
                            CharacterCardUiModel(
                                2,
                                "Morty Smith",
                                "Human",
                                CharacterStatusUi.Alive,
                                "test://failure",
                            ),
                            CharacterCardUiModel(
                                3,
                                "Summer Smith",
                                "Human",
                                CharacterStatusUi.Unknown,
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

            override suspend fun getPage(page: Int, name: String?): CharactersPageResult {
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
                        persistentListOf(
                            CharacterCardUiModel(
                                1,
                                "Rick Sanchez",
                                "Human",
                                CharacterStatusUi.Alive,
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
        compose.onNodeWithText("Alive").assertIsDisplayed()
        assertEquals(1, selectedId)
    }
}
