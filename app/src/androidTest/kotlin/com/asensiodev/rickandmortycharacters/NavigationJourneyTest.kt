@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationJourneyTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    @BindValue
    @JvmField
    val fakeCharactersRepository = JourneyCharactersRepository()

    private lateinit var mainActivityScenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hilt.inject()
        mainActivityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        mainActivityScenario.close()
    }

    @Test
    fun GIVEN_a_scrolled_search_WHEN_a_different_name_is_submitted_THEN_it_resets_the_grid_to_the_new_first_page() {
        compose.runOnIdle { fakeCharactersRepository.pageCount = 2 }
        compose.onNodeWithContentDescription("Search characters").performClick().performTextInput("Rick")
        compose.onNodeWithContentDescription("Search characters").performImeAction()
        compose.waitUntil { compose.onAllNodesWithText("Rick 1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Rick 19"))
        compose.waitUntil { compose.onAllNodesWithText("Loaded 40 of 40 characters").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Rick 40"))

        compose.onNodeWithContentDescription("Search characters").performClick().performTextReplacement("Beth")
        compose.onNodeWithContentDescription("Search characters").performImeAction()
        compose.waitUntil { compose.onAllNodesWithText("Beth 1").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("Beth 1").assertIsDisplayed()
        compose.onNodeWithText("Rick 40").assertDoesNotExist()
        compose.onNodeWithText("Loaded 20 of 40 characters").assertIsDisplayed()
        assertEquals("Beth", fakeCharactersRepository.requestedNames.last())
        assertEquals(1, fakeCharactersRepository.requestedPages.last())
    }

    @Test
    fun GIVEN_a_scrolled_search_WHEN_status_changes_THEN_it_keeps_the_name_and_resets_the_grid_and_counter() {
        compose.runOnIdle { fakeCharactersRepository.pageCount = 2 }
        compose.onNodeWithContentDescription("Search characters").performClick().performTextInput("Rick")
        compose.onNodeWithContentDescription("Search characters").performImeAction()
        compose.waitUntil { compose.onAllNodesWithText("Rick 1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Rick 19"))
        compose.waitUntil { compose.onAllNodesWithText("Loaded 40 of 40 characters").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Rick 40"))

        compose.onNode(hasText("Unknown") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performClick()
        compose.waitUntil { compose.onAllNodesWithText("Rick 1").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("Rick 1").assertIsDisplayed()
        compose.onNodeWithText("Rick 40").assertDoesNotExist()
        assertEquals(
            "pages=${fakeCharactersRepository.requestedPages} statuses=${fakeCharactersRepository.requestedStatuses}",
            1,
            fakeCharactersRepository.requestedPages.last(),
        )
        compose.waitUntil { compose.onAllNodesWithText("Loaded 20 of 40 characters").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Loaded 20 of 40 characters").assertIsDisplayed()
        assertEquals("Rick", fakeCharactersRepository.requestedNames.last())
        assertEquals(1, fakeCharactersRepository.requestedPages.last())
        assertEquals(CharacterStatus.Unknown, fakeCharactersRepository.requestedStatuses.last())
    }

    @Test
    fun GIVEN_a_combined_search_WHEN_returning_from_later_detail_THEN_it_retains_query_filters_pages_and_scroll_without_the_keyboard() {
        compose.runOnIdle { fakeCharactersRepository.pageCount = 2 }
        compose.onNode(hasText("Dead") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performClick()
        compose.onNodeWithContentDescription("Search characters").performClick().performTextInput("Rick")
        compose.onNodeWithContentDescription("Search characters").performImeAction()
        compose.waitUntil { compose.onAllNodesWithText("Rick 1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Rick 19"))
        compose.waitUntil { compose.onAllNodesWithText("Loaded 40 of 40 characters").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Rick 40"))
        val priorBounds = compose.onNodeWithText("Rick 40").fetchSemanticsNode().boundsInRoot
        val requests = fakeCharactersRepository.requestedNames.toList()

        compose.onNodeWithText("Rick 40").performClick()
        compose.onNodeWithText("ENTITY PROFILE #40").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("Rick 40").assertIsDisplayed()
        compose.onNodeWithText("Loaded 40 of 40 characters").assertIsDisplayed()
        compose.onNodeWithContentDescription("Search characters").assertIsNotFocused()
        compose.onNode(hasText("Dead") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).assertIsSelected()
        assertEquals(priorBounds, compose.onNodeWithText("Rick 40").fetchSemanticsNode().boundsInRoot)
        assertEquals(requests, fakeCharactersRepository.requestedNames)
        assertEquals(listOf("Rick", "Rick"), requests.filterNotNull())
        assertEquals(listOf(CharacterStatus.Dead, CharacterStatus.Dead), fakeCharactersRepository.requestedStatuses.takeLast(2))
    }

    @Test
    fun GIVEN_multiple_pages_WHEN_returning_from_later_detail_THEN_it_retains_count_cards_and_scroll() {
        mainActivityScenario.close()
        fakeCharactersRepository.pageCount = 2
        fakeCharactersRepository.pageRequests = 0
        fakeCharactersRepository.requestedPages.clear()
        mainActivityScenario = ActivityScenario.launch(MainActivity::class.java)
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 19"))
        compose.waitUntil {
            compose.onAllNodesWithText(
                "Loaded 40 of 40 characters",
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 40"))
        val priorBounds = compose.onNodeWithText("Character 40").fetchSemanticsNode().boundsInRoot

        compose.onNodeWithText("Character 40").performClick()
        compose.onNodeWithText("ENTITY PROFILE #40").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("Loaded 40 of 40 characters").assertIsDisplayed()
        compose.onNodeWithText("Character 40").assertIsDisplayed()
        assertEquals(
            priorBounds,
            compose.onNodeWithText("Character 40").fetchSemanticsNode().boundsInRoot,
        )
        assertEquals(listOf(1, 2), fakeCharactersRepository.requestedPages)
        assertEquals(listOf(40), fakeCharactersRepository.requestedIds)
    }

    @Test
    fun GIVEN_scrolled_Home_WHEN_detail_is_opened_and_closed_THEN_it_uses_the_selected_ID_and_retains_scroll() {
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Character 15"))
        val priorBounds = compose.onNodeWithText("Character 15").fetchSemanticsNode().boundsInRoot

        compose.onNodeWithText("Character 15").performClick()
        compose.onNodeWithText("ENTITY PROFILE #15").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("Character 15").assertIsDisplayed()
        assertEquals(
            priorBounds,
            compose.onNodeWithText("Character 15").fetchSemanticsNode().boundsInRoot,
        )
        assertEquals(listOf(15), fakeCharactersRepository.requestedIds)
        assertEquals(1, fakeCharactersRepository.pageRequests)
    }

    @Test
    fun GIVEN_loaded_Home_WHEN_detail_is_reopened_after_back_THEN_it_creates_a_new_detail_owner_and_keeps_Home() {
        compose.onNodeWithText("Character 1").performClick()
        compose.onNodeWithText("ENTITY PROFILE #1").assertIsDisplayed()

        Espresso.pressBack()
        compose.onNodeWithText("Character 1").assertIsDisplayed().performClick()
        compose.onNodeWithText("ENTITY PROFILE #1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Character 2").performClick()
        compose.onNodeWithText("ENTITY PROFILE #2").assertIsDisplayed()

        assertEquals(listOf(1, 1, 2), fakeCharactersRepository.requestedIds)
        assertEquals(1, fakeCharactersRepository.pageRequests)
    }

    @Test
    fun GIVEN_pending_detail_WHEN_back_is_pressed_THEN_it_cancels_the_request_and_retains_Home() {
        compose.runOnIdle { fakeCharactersRepository.detailGate = CompletableDeferred() }
        compose.onNodeWithText("Character 1").performClick()
        compose.onNodeWithContentDescription("Loading character details").assertIsDisplayed()

        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("Character 1").assertIsDisplayed()
        compose.waitUntil { fakeCharactersRepository.cancelledId == 1 }
        assertEquals(1, fakeCharactersRepository.pageRequests)
    }

    @Test
    fun GIVEN_a_character_WHEN_it_is_tapped_twice_quickly_THEN_it_opens_only_one_detail_entry() {
        compose.onNodeWithText("Character 1").performTouchInput {
            click()
            click()
        }
        compose.onNodeWithText("ENTITY PROFILE #1").assertIsDisplayed()

        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("Character 1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").assertDoesNotExist()
        assertEquals(listOf(1), fakeCharactersRepository.requestedIds)
    }

    @Test
    fun GIVEN_a_detail_error_WHEN_retry_succeeds_THEN_it_shows_loading_and_content_for_the_same_ID() {
        compose.runOnIdle {
            fakeCharactersRepository.detailResult = CharacterDetailsResult.Failure(
                CharacterRequestFailure.Network,
            )
        }
        compose.onNodeWithText("Character 2").performClick()
        compose.onNodeWithText("Couldn't load character").assertIsDisplayed()
        val responseGate = CompletableDeferred<Unit>()
        compose.runOnIdle {
            fakeCharactersRepository.detailResult = null
            fakeCharactersRepository.detailGate = responseGate
        }

        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithContentDescription("Loading character details").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.runOnIdle { responseGate.complete(Unit) }

        compose.onNodeWithText("ENTITY PROFILE #2").assertIsDisplayed()
        assertEquals(listOf(2, 2), fakeCharactersRepository.requestedIds)
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Character 2").assertIsDisplayed()
        assertEquals(1, fakeCharactersRepository.pageRequests)
    }

    @Test
    fun GIVEN_missing_detail_WHEN_system_back_is_pressed_THEN_it_returns_to_Home_without_adding_a_route() {
        compose.runOnIdle {
            fakeCharactersRepository.detailResult = CharacterDetailsResult.NotFound
        }
        compose.onNodeWithText("Character 1").performClick()
        compose.onNodeWithText("Character not found").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()

        Espresso.pressBack()

        compose.onNodeWithText("Character 1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").assertDoesNotExist()
        assertEquals(1, fakeCharactersRepository.pageRequests)
    }
}
