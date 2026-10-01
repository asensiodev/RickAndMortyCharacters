@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterStatusUi
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeAction
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()
    private lateinit var fakeCharactersRepository: FakeCharactersRepository

    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        fakeCharactersRepository =
            FakeCharactersRepository(CharactersPageResult.Failure(CharacterRequestFailure.Service))
        homeViewModel = HomeViewModel(fakeCharactersRepository)
    }

    @Test
    fun `GIVEN pending retry WHEN actions repeat THEN one request runs`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.process(HomeAction.Load)
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            fakeCharactersRepository.pending = gate

            repeat(4) {
                homeViewModel.process(HomeAction.Retry)
                homeViewModel.process(HomeAction.Load)
            }
            advanceUntilIdle()

            assertEquals(HomeUiState.Loading, homeViewModel.state.value)
            assertEquals(listOf(1, 1), fakeCharactersRepository.requestedPages)

            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(HomeUiState.Error, homeViewModel.state.value)
            homeViewModel.process(HomeAction.Retry)
            advanceUntilIdle()
            assertEquals(listOf(1, 1, 1), fakeCharactersRepository.requestedPages)
        }

    @Test
    fun `GIVEN pending request WHEN owner clears THEN cancel without error`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result =
                CharactersPageResult.Failure(CharacterRequestFailure.Network)
            fakeCharactersRepository.pending = CompletableDeferred()
            val store = ViewModelStore()
            store.put("home", homeViewModel)
            homeViewModel.process(HomeAction.Load)
            advanceUntilIdle()

            store.clear()
            advanceUntilIdle()

            assertTrue(fakeCharactersRepository.cancelled)
            assertEquals(HomeUiState.Loading, homeViewModel.state.value)
        }

    @Test
    fun `GIVEN empty response WHEN loading THEN empty state`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result =
                CharactersPageResult.Success(CharacterPage(emptyList(), 0, null))

            homeViewModel.process(HomeAction.Load)
            advanceUntilIdle()

            assertEquals(HomeUiState.Empty, homeViewModel.state.value)
        }

    @Test
    fun `GIVEN loaded catalogue WHEN load and observation repeat THEN no reload`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result =
                CharactersPageResult.Success(CharacterPage(emptyList(), 0, null))
            homeViewModel.process(HomeAction.Load)
            advanceUntilIdle()

            homeViewModel.state.test { awaitItem() }
            homeViewModel.process(HomeAction.Load)
            advanceUntilIdle()

            assertEquals(listOf(1), fakeCharactersRepository.requestedPages)
        }

    @Test
    fun `GIVEN failed load WHEN retry succeeds THEN loading and content`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.process(HomeAction.Load)
            advanceUntilIdle()
            fakeCharactersRepository.result = CharactersPageResult.Success(
                CharacterPage(
                    listOf(
                        CharacterSummary(1, "Rick Sanchez", "Human", CharacterStatus.Alive, null),
                    ),
                    1,
                    null,
                ),
            )

            homeViewModel.state.test {
                assertEquals(HomeUiState.Error, awaitItem())
                homeViewModel.process(HomeAction.Retry)
                assertEquals(HomeUiState.Loading, homeViewModel.state.value)
                assertEquals(HomeUiState.Loading, awaitItem())
                advanceUntilIdle()
                assertEquals(
                    HomeUiState.Content(
                        persistentListOf(
                            CharacterCardUiModel(
                                1,
                                "Rick Sanchez",
                                "Human",
                                CharacterStatusUi.Alive,
                                null,
                            ),
                        ),
                    ),
                    awaitItem(),
                )
            }
            assertEquals(listOf(1, 1), fakeCharactersRepository.requestedPages)
        }

    @Test
    fun `GIVEN service failure WHEN loading THEN error state`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.process(HomeAction.Load)
            advanceUntilIdle()

            assertEquals(HomeUiState.Error, homeViewModel.state.value)
        }

    @Test
    fun `GIVEN characters WHEN first load THEN content state`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result = CharactersPageResult.Success(
                CharacterPage(
                    listOf(
                        CharacterSummary(
                            7,
                            "Abradolf Lincler",
                            "Human",
                            CharacterStatus.Unknown,
                            null,
                        ),
                    ),
                    1,
                    null,
                ),
            )
            val expected = HomeUiState.Content(
                persistentListOf(
                    CharacterCardUiModel(
                        7,
                        "Abradolf Lincler",
                        "Human",
                        CharacterStatusUi.Unknown,
                        null,
                    ),
                ),
            )

            homeViewModel.state.test {
                assertEquals(HomeUiState.Loading, awaitItem())
                homeViewModel.process(HomeAction.Load)
                advanceUntilIdle()
                assertEquals(expected, homeViewModel.state.value)
                assertEquals(expected, awaitItem())
            }
            assertEquals(listOf(1), fakeCharactersRepository.requestedPages)
        }
}

private class FakeCharactersRepository(var result: CharactersPageResult) : CharactersRepository {
    val requestedPages = mutableListOf<Int>()
    var pending: CompletableDeferred<Unit>? = null
    var cancelled = false

    override suspend fun getDetails(characterId: Int): CharacterDetailsResult =
        CharacterDetailsResult.NotFound

    override suspend fun getPage(page: Int): CharactersPageResult {
        requestedPages += page
        return try {
            pending?.await()
            result
        } catch (error: CancellationException) {
            cancelled = true
            throw error
        }
    }
}
