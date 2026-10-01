@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
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

    @Inject
    lateinit var fakeCharactersRepository: JourneyCharactersRepository

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
    fun GIVEN_scrolledHome_WHEN_detailAndBack_THEN_selectedIdAndScrollRetained() {
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
    fun GIVEN_content_WHEN_systemBackAndReopen_THEN_newDetailOwnerAndSameHome() {
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
    fun GIVEN_pendingDetail_WHEN_back_THEN_requestCancelledAndHomeRetained() {
        compose.runOnIdle { fakeCharactersRepository.detailGate = CompletableDeferred() }
        compose.onNodeWithText("Character 1").performClick()
        compose.onNodeWithContentDescription("Loading character details").assertIsDisplayed()

        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("Character 1").assertIsDisplayed()
        compose.waitUntil { fakeCharactersRepository.cancelledId == 1 }
        assertEquals(1, fakeCharactersRepository.pageRequests)
    }

    @Test
    fun GIVEN_rapidSelection_WHEN_tappedTwice_THEN_onlyOneDetailEntry() {
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
    fun GIVEN_detailError_WHEN_retry_THEN_sameIdLoadingAndContent() {
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
    fun GIVEN_missingDetail_WHEN_systemBack_THEN_homeWithoutAnotherRoute() {
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
