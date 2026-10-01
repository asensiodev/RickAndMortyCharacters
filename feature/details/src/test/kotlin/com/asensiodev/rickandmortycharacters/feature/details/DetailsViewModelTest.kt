@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.details

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.asensiodev.rickandmortycharacters.core.testing.MainDispatcherRule
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsAction
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState
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
class DetailsViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()
    private val fakeCharactersRepository = FakeCharactersRepository()

    private lateinit var detailsViewModel: DetailsViewModel

    @Before
    fun setUp() {
        detailsViewModel = DetailsViewModel(fakeCharactersRepository)
    }

    @Test
    fun `GIVEN a failed detail request WHEN retry succeeds THEN it loads content for the same character`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result = CharacterDetailsResult.Failure(
                CharacterRequestFailure.Network,
            )
            detailsViewModel.process(DetailsAction.Load(361))
            advanceUntilIdle()
            fakeCharactersRepository.result = CharacterDetailsResult.Success(
                CharacterDetails(
                    361, "Toxic Rick", CharacterStatus.Dead, "Humanoid", "Male", null,
                    "Detoxifier", "Earth (Replacement Dimension)", 1, null,
                ),
            )

            detailsViewModel.state.test {
                assertEquals(DetailsUiState.Error, awaitItem())
                detailsViewModel.process(DetailsAction.Retry)
                assertEquals(DetailsUiState.Loading, detailsViewModel.state.value)
                assertEquals(DetailsUiState.Loading, awaitItem())
                advanceUntilIdle()
                assertTrue(awaitItem() is DetailsUiState.Content)
            }
            assertEquals(listOf(361, 361), fakeCharactersRepository.requestedIds)
        }

    @Test
    fun `GIVEN a pending retry WHEN retry is requested again THEN it keeps one request for the same character`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result = CharacterDetailsResult.Failure(
                CharacterRequestFailure.Service,
            )
            detailsViewModel.process(DetailsAction.Load(361))
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            fakeCharactersRepository.pending = gate

            repeat(4) {
                detailsViewModel.process(DetailsAction.Retry)
                detailsViewModel.process(DetailsAction.Load(361))
            }
            advanceUntilIdle()

            assertEquals(DetailsUiState.Loading, detailsViewModel.state.value)
            assertEquals(listOf(361, 361), fakeCharactersRepository.requestedIds)
            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals(DetailsUiState.Error, detailsViewModel.state.value)
        }

    @Test
    fun `GIVEN a pending detail request WHEN the ViewModel is cleared THEN it cancels without reporting an error`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.pending = CompletableDeferred()
            val store = ViewModelStore()
            store.put("details", detailsViewModel)
            detailsViewModel.process(DetailsAction.Load(361))
            advanceUntilIdle()

            store.clear()
            advanceUntilIdle()

            assertTrue(fakeCharactersRepository.cancelled)
            assertEquals(DetailsUiState.Loading, detailsViewModel.state.value)
        }

    @Test
    fun `GIVEN loaded detail WHEN loading and observation repeat THEN it reuses content without another request`() =
        runTest(mainDispatcher.dispatcher) {
            detailsViewModel.process(DetailsAction.Load(361))
            advanceUntilIdle()

            detailsViewModel.state.test { assertTrue(awaitItem() is DetailsUiState.Content) }
            detailsViewModel.process(DetailsAction.Load(361))
            detailsViewModel.process(DetailsAction.Load(2))
            detailsViewModel.process(DetailsAction.Retry)
            advanceUntilIdle()

            assertEquals(listOf(361), fakeCharactersRepository.requestedIds)
            assertEquals(361, (detailsViewModel.state.value as DetailsUiState.Content).character.id)
        }

    @Test
    fun `GIVEN a missing character WHEN retry is requested THEN it keeps not found without another request`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result = CharacterDetailsResult.NotFound

            detailsViewModel.process(DetailsAction.Load(361))
            advanceUntilIdle()
            detailsViewModel.process(DetailsAction.Retry)
            advanceUntilIdle()

            assertEquals(DetailsUiState.NotFound, detailsViewModel.state.value)
            assertEquals(listOf(361), fakeCharactersRepository.requestedIds)
        }

    @Test
    fun `GIVEN a pending initial request WHEN initialization repeats THEN it keeps only one detail request`() =
        runTest(mainDispatcher.dispatcher) {
            val gate = CompletableDeferred<Unit>()
            fakeCharactersRepository.pending = gate

            repeat(4) { detailsViewModel.process(DetailsAction.Load(361)) }
            detailsViewModel.process(DetailsAction.Retry)
            advanceUntilIdle()

            assertEquals(listOf(361), fakeCharactersRepository.requestedIds)
            assertEquals(DetailsUiState.Loading, detailsViewModel.state.value)
            gate.complete(Unit)
            advanceUntilIdle()
            assertTrue(detailsViewModel.state.value is DetailsUiState.Content)
        }

    @Test
    fun `GIVEN a character WHEN detail loading succeeds THEN it exposes content for the requested ID`() =
        runTest(mainDispatcher.dispatcher) {
            detailsViewModel.state.test {
                assertEquals(DetailsUiState.Loading, awaitItem())

                detailsViewModel.process(DetailsAction.Load(361))
                advanceUntilIdle()

                val content = awaitItem()
                assertTrue(content is DetailsUiState.Content)
                val character = (content as DetailsUiState.Content).character
                assertEquals(361, character.id)
                assertEquals("Toxic Rick", character.name)
                assertEquals(1, character.episodeCount)
                assertEquals(listOf(361), fakeCharactersRepository.requestedIds)
            }
        }
}

private class FakeCharactersRepository : CharactersRepository {
    val requestedIds = mutableListOf<Int>()
    var result: CharacterDetailsResult = CharacterDetailsResult.Success(
        CharacterDetails(
            361, "Toxic Rick", CharacterStatus.Dead, "Humanoid", "Male",
            "Rick's toxic side", "Detoxifier", "Earth (Replacement Dimension)", 1, null,
        ),
    )
    var pending: CompletableDeferred<Unit>? = null
    var cancelled = false

    override suspend fun getPage(page: Int, name: String?): CharactersPageResult =
        CharactersPageResult.Failure(CharacterRequestFailure.Service)

    override suspend fun getDetails(characterId: Int): CharacterDetailsResult {
        requestedIds += characterId
        return try {
            pending?.await()
            result
        } catch (error: CancellationException) {
            cancelled = true
            throw error
        }
    }
}
