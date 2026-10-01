@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModelStore
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
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAction
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeContentTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var imageLoader: ImageLoader
    private val portraitGate = CompletableDeferred<Unit>()

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

    @After
    fun tearDown() {
        imageLoader.shutdown()
    }

    @Test
    fun GIVEN_portraitStates_WHEN_selecting_THEN_metadataAndIdsRemain() {
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
    fun GIVEN_narrowLargeText_WHEN_errorRenders_THEN_retryReachable() {
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
    fun GIVEN_failedRouteLoad_WHEN_retrying_THEN_receivedContent() {
        var requests = 0
        val responseGate = CompletableDeferred<Unit>()
        val repository = object : CharactersRepository {
            override suspend fun getDetails(characterId: Int): CharacterDetailsResult =
                CharacterDetailsResult.NotFound

            override suspend fun getPage(page: Int): CharactersPageResult {
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

        lateinit var homeViewModel: HomeViewModel
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
                repeat(3) { homeViewModel.process(HomeAction.Retry) }
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
    fun GIVEN_emptyCatalogue_WHEN_rendering_THEN_feedbackWithoutActions() {
        compose.setContent {
            RickAndMortyTheme { HomeContent(HomeUiState.Empty, imageLoader, {}, {}) }
        }

        compose.onNodeWithText("No characters available.").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.onNodeWithText("Back").assertDoesNotExist()
    }

    @Test
    fun GIVEN_loading_WHEN_rendering_THEN_noninteractiveSkeletons() {
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
    fun GIVEN_error_WHEN_retrying_THEN_callbackWithoutNavigation() {
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
    fun GIVEN_characterContent_WHEN_selecting_THEN_characterId() {
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
