@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
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
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
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
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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
class HomeSearchTest {
    @get:Rule
    val compose = createComposeRule()
    private val imageLoader by lazy {
        ImageLoader.Builder(InstrumentationRegistry.getInstrumentation().targetContext).components {
            add(Interceptor { chain -> SuccessResult(image = ColorImage(android.graphics.Color.DKGRAY), request = chain.request) })
        }.build()
    }

    private lateinit var homeViewModel: HomeViewModel

    @After
    fun tearDown() {
        imageLoader.shutdown()
    }

    @Test
    fun GIVEN_an_active_search_WHEN_the_user_drags_results_THEN_it_hides_the_keyboard_and_clears_focus_without_changing_the_query() {
        var hideRequests = 0
        val actions = mutableListOf<HomeSearchAction>()
        val keyboard = object : SoftwareKeyboardController {
            override fun show() = Unit
            override fun hide() {
                hideRequests++
            }
        }
        val search = HomePagingState(searchText = "Rick", appliedName = "Rick", selectedStatus = CharacterStatus.Alive)
        val characters = (1..30).map {
            CharacterCardUiModel(id = it, name = "Character $it", species = "Human", status = CharacterStatus.Alive, imageUrl = null)
        }
        compose.setContent {
            CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboard) {
                RickAndMortyTheme {
                    HomeContent(
                        state = HomeUiState.Content(characters = characters, totalCount = 30),
                        imageLoader = imageLoader,
                        onRetry = {},
                        onCharacterSelected = {},
                        searchState = search,
                        onSearchAction = actions::add,
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("Search characters").performClick()

        compose.onNode(hasScrollToIndexAction()).performTouchInput { swipeUp() }

        compose.runOnIdle {
            assertTrue(hideRequests > 0)
            assertTrue(actions.isEmpty())
        }
        compose.onNodeWithContentDescription("Search characters").assertIsNotFocused()
        val input = compose.onNodeWithContentDescription("Search characters").fetchSemanticsNode().config
        assertEquals("Rick", input[SemanticsProperties.EditableText].text)
        compose.onNode(
            hasText("Alive") and SemanticsMatcher.expectValue(key = SemanticsProperties.Selected, expectedValue = true),
        ).assertIsSelected()
    }

    @Test
    @OptIn(ExperimentalLayoutApi::class)
    fun GIVEN_a_visible_search_keyboard_WHEN_system_Back_closes_it_THEN_focus_clears_and_editing_can_resume() {
        var keyboardVisible by mutableStateOf(false)
        val actions = mutableListOf<HomeSearchAction>()
        val search = HomePagingState(searchText = "Rick", appliedName = "Rick", selectedStatus = CharacterStatus.Alive)
        compose.setContent {
            val visible = WindowInsets.isImeVisible
            SideEffect { keyboardVisible = visible }
            RickAndMortyTheme {
                HomeContent(
                    state = HomeUiState.Empty,
                    imageLoader = imageLoader,
                    onRetry = {},
                    onCharacterSelected = {},
                    searchState = search,
                    onSearchAction = actions::add,
                )
            }
        }
        compose.onNodeWithContentDescription("Search characters").performClick()
        compose.waitUntil { keyboardVisible }

        Espresso.pressBack()
        compose.waitUntil { !keyboardVisible }

        compose.onNodeWithContentDescription("Search characters").assertIsNotFocused()
        val input = compose.onNodeWithContentDescription("Search characters").fetchSemanticsNode().config
        assertEquals("Rick", input[SemanticsProperties.EditableText].text)
        compose.onNode(
            hasText("Alive") and SemanticsMatcher.expectValue(key = SemanticsProperties.Selected, expectedValue = true),
        ).assertIsSelected()
        compose.runOnIdle { assertTrue(actions.isEmpty()) }
        compose.onNodeWithContentDescription("Search characters").performClick()
        compose.waitUntil { keyboardVisible }
        compose.onNodeWithContentDescription("Search characters").assertIsFocused()
        Espresso.pressBack()
    }

    @Test
    fun GIVEN_a_focused_search_WHEN_the_grid_scrolls_and_resets_programmatically_THEN_it_does_not_dismiss_the_keyboard() {
        var hideRequests = 0
        val keyboard = object : SoftwareKeyboardController {
            override fun show() = Unit
            override fun hide() {
                hideRequests++
            }
        }
        var search by mutableStateOf(HomePagingState(searchText = "Rick", appliedName = "Rick"))
        val characters = (1..30).map {
            CharacterCardUiModel(id = it, name = "Character $it", species = "Human", status = CharacterStatus.Alive, imageUrl = null)
        }
        compose.setContent {
            CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboard) {
                RickAndMortyTheme {
                    HomeContent(
                        state = HomeUiState.Content(
                            characters = characters,
                            totalCount = 30,
                        ),
                        imageLoader = imageLoader,
                        onRetry = {
                        },
                        onCharacterSelected = {},
                        searchState = search,
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("Search characters").performClick()

        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        compose.runOnIdle { search = search.copy(generation = 1) }

        compose.onNodeWithText("Character 1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Search characters").assertIsFocused()
        compose.runOnIdle { assertEquals(0, hideRequests) }
    }

    @Test
    fun GIVEN_an_ordinary_screen_WHEN_status_filters_are_rendered_THEN_all_filters_fit_and_Unknown_is_fully_visible_on_one_line() {
        compose.setContent {
            RickAndMortyTheme {
                Box(modifier = Modifier.width(400.dp)) {
                    HomeContent(state = HomeUiState.Empty, imageLoader = imageLoader, onRetry = {}, onCharacterSelected = {})
                }
            }
        }

        val scrollRange = compose.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange),
        ).fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange]
        assertEquals(0f, scrollRange.maxValue(), 0.5f)
        val textLayouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("Unknown", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(textLayouts) }

        assertEquals(1, textLayouts.single().lineCount)
        val layout = textLayouts.single()
        val finalCharacter = layout.layoutInput.text.lastIndex
        assertEquals(layout.layoutInput.text.length, layout.getLineEnd(0, visibleEnd = true))
        assertTrue(layout.getBoundingBox(finalCharacter).right <= layout.size.width)
        assertTrue(!layout.isLineEllipsized(0))
        listOf("All", "Alive", "Dead", "Unknown").forEach {
            compose.onNodeWithText(it).assertIsDisplayed().assertTouchHeightIsEqualTo(48.dp)
        }
    }

    @Test
    fun GIVEN_a_narrow_screen_with_large_text_WHEN_Unknown_is_selected_THEN_all_filters_remain_reachable() {
        var selectedStatus by mutableStateOf<CharacterStatus?>(null)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                RickAndMortyTheme {
                    Box(modifier = Modifier.width(HomeLayoutTokens.twoColumnMinWidth)) {
                        HomeContent(
                            state = HomeUiState.Empty,
                            imageLoader = imageLoader,
                            onRetry = {},
                            onCharacterSelected = {},
                            searchState = HomePagingState(selectedStatus = selectedStatus),
                            onSearchAction = { action ->
                                if (action is HomeSearchAction.SelectStatus) selectedStatus = action.status
                            },
                        )
                    }
                }
            }
        }

        compose.onNodeWithText("Unknown").performScrollTo().performClick()

        compose.onNodeWithText("Unknown").assertIsSelected()
        listOf("All", "Alive", "Dead", "Unknown").forEach { label ->
            compose.onNodeWithText(label).performScrollTo().assertIsDisplayed()
                .assertTouchHeightIsEqualTo(48.dp)
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(label, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            val layout = layouts.single()
            assertEquals(1, layout.lineCount)
            assertTrue(layout.getBoundingBox(label.lastIndex).right <= layout.size.width)
            assertTrue(!layout.isLineEllipsized(0))
        }
        compose.onNodeWithText("No characters found").assertIsDisplayed()
        compose.onNodeWithText("All").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun GIVEN_visible_status_filters_WHEN_Dead_is_selected_THEN_the_screen_exposes_the_selected_constraint() {
        var selectedStatus by mutableStateOf<CharacterStatus?>(null)
        compose.setContent {
            RickAndMortyTheme {
                HomeContent(
                    state = HomeUiState.Loading,
                    imageLoader = imageLoader,
                    onRetry = {},
                    onCharacterSelected = {},
                    searchState = HomePagingState(selectedStatus = selectedStatus),
                    onSearchAction = { action ->
                        if (action is HomeSearchAction.SelectStatus) selectedStatus = action.status
                    },
                )
            }
        }
        compose.onNodeWithText("All").assertIsSelected()
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("Unknown", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(1, layouts.single().lineCount)
        assertTrue(!layouts.single().isLineEllipsized(0))

        compose.onNodeWithText("Dead").performClick()

        compose.onNodeWithText("Dead").assertIsSelected()
        compose.onNodeWithText("All").assertIsNotSelected()
        compose.onNodeWithContentDescription("Search characters").assertIsDisplayed()
    }

    @Test
    fun GIVEN_a_failed_combined_request_WHEN_Retry_is_tapped_twice_THEN_it_retries_the_query_once_and_keeps_the_controls() {
        val statuses = mutableListOf<CharacterStatus?>()
        val names = mutableListOf<String?>()
        val retryGate = CompletableDeferred<Unit>()
        var failSearch = true
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
                names += name
                statuses += status
                if (name != null && failSearch) {
                    failSearch = false
                    return CharactersPageResult.Failure(reason = CharacterRequestFailure.Service)
                }
                if (name != null) retryGate.await()
                return CharactersPageResult.Success(
                    page = CharacterPage(
                        characters = listOf(
                            CharacterSummary(
                                id = 1,
                                name = "Result ${name ?: "All"}",
                                species = "Human",
                                status = CharacterStatus.Alive,
                                imageUrl = null,
                            ),
                        ),
                        totalCount = 1,
                        nextPage = null,
                    ),
                )
            }
        }
        homeViewModel = HomeViewModel(repository = repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent {
                RickAndMortyTheme { HomeRoute(viewModel = homeViewModel, imageLoader = imageLoader, onCharacterSelected = {}) }
            }
            compose.onNodeWithContentDescription("Search characters").performClick().performTextInput("Rick")
            compose.onNodeWithText("Dead").performClick()
            compose.onNodeWithContentDescription("Search characters").performImeAction()
            compose.waitUntil { compose.onAllNodesWithText("Couldn't load characters").fetchSemanticsNodes().isNotEmpty() }

            compose.onNodeWithText("Retry").performTouchInput {
                click()
                click()
            }
            compose.waitUntil { names.size == 3 }

            compose.onNodeWithText("Rick").assertIsDisplayed()
            compose.onNodeWithText("Dead").assertIsSelected()
            compose.onNodeWithText("Retry").assertDoesNotExist()
            compose.runOnIdle {
                assertEquals(listOf(null, "Rick", "Rick"), names)
                assertEquals(listOf(null, CharacterStatus.Dead, CharacterStatus.Dead), statuses)
            }
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

            override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
                names += name
                return CharactersPageResult.Success(
                    page = CharacterPage(
                        characters = if (name == "Missing") {
                            emptyList()
                        } else {
                            listOf(
                                CharacterSummary(
                                    id = 1,
                                    name = "Result ${name ?: "All"}",
                                    species = "Human",
                                    status = CharacterStatus.Alive,
                                    imageUrl = null,
                                ),
                            )
                        },
                        totalCount = if (name == "Missing") 0 else 1,
                        nextPage = null,
                    ),
                )
            }
        }
        homeViewModel = HomeViewModel(repository = repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent {
                RickAndMortyTheme { HomeRoute(viewModel = homeViewModel, imageLoader = imageLoader, onCharacterSelected = {}) }
            }
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

            override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
                names += name
                if (name != null) pending.await()
                return CharactersPageResult.Success(
                    page = CharacterPage(
                        characters = listOf(
                            CharacterSummary(
                                id = 1,
                                name = if (name ==
                                    null
                                ) {
                                    "Old card"
                                } else {
                                    "New card"
                                },
                                species = "Human",
                                status = CharacterStatus.Alive,
                                imageUrl = null,
                            ),
                        ),
                        totalCount = if (name == null) 50 else 1,
                        nextPage = null,
                    ),
                )
            }
        }
        homeViewModel = HomeViewModel(repository = repository)
        val store = ViewModelStore()
        store.put("home", homeViewModel)
        try {
            compose.setContent {
                RickAndMortyTheme { HomeRoute(viewModel = homeViewModel, imageLoader = imageLoader, onCharacterSelected = {}) }
            }
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
                    state = HomeUiState.Empty,
                    imageLoader = imageLoader,
                    onRetry = {},
                    onCharacterSelected = {},
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
                HomeContent(state = results, imageLoader = imageLoader, onRetry = {
                }, onCharacterSelected = {}, searchState = search, onSearchAction = { action ->
                    when (action) {
                        is HomeSearchAction.Edit -> search = search.copy(searchText = action.name)
                        is HomeSearchAction.SelectStatus -> search = search.copy(selectedStatus = action.status)
                        else -> Unit
                    }
                })
            }
        }

        compose.onNodeWithText("Search characters").performClick().performTextInput("Rick")
        compose.onNodeWithContentDescription("Search characters").performSemanticsAction(SemanticsActions.SetSelection) { it(1, 3, false) }
        compose.onNodeWithText("Dead").performClick()
        compose.onNodeWithText("Dead").assertIsSelected()
        for (state in listOf(HomeUiState.Empty, HomeUiState.Error, HomeUiState.Loading)) {
            compose.runOnIdle { results = state }

            compose.onNodeWithContentDescription("Search characters").assertIsFocused()
            assertEquals(
                TextRange(1, 3),
                compose.onNodeWithContentDescription(
                    "Search characters",
                ).fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange],
            )
        }
        compose.runOnIdle { results = HomeUiState.Error }
        compose.onNodeWithText("Couldn't load characters").assertIsDisplayed()
        compose.onNodeWithText("Retry").performScrollTo().assertIsDisplayed()
    }
}
