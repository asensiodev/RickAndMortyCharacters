@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.details

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.asensiodev.rickandmortycharacters.core.testing.MainDispatcherRule
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesResult
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsAction
import com.asensiodev.rickandmortycharacters.feature.details.model.DetailsUiState
import com.asensiodev.rickandmortycharacters.feature.details.model.EpisodesUiState
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
    private val fakeEpisodesRepository = FakeEpisodesRepository()

    private lateinit var detailsViewModel: DetailsViewModel

    @Before
    fun setUp() {
        detailsViewModel = DetailsViewModel(repository = fakeCharactersRepository, episodesRepository = fakeEpisodesRepository)
    }

    @Test
    fun `GIVEN missing episode references WHEN character detail loads THEN it keeps facts and reports a section error`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = emptyList())
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)

            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Error), detailsViewModel.state.value)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
        }

    @Test
    fun `GIVEN partially decoded episode references WHEN character detail loads THEN it reports an error without requesting a subset`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeCount = 2)
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)

            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Error), detailsViewModel.state.value)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
        }

    @Test
    fun `GIVEN incomplete references WHEN repeated retries recover them THEN it retains facts and requests episodes once`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = emptyList())
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            val recoveredCharacter = character.copy(episodeIds = listOf(27))
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = recoveredCharacter)
            fakeCharactersRepository.pending = CompletableDeferred()

            repeat(4) { detailsViewModel.process(action = DetailsAction.RetryEpisodes) }
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Loading), detailsViewModel.state.value)
            assertEquals(listOf(361, 361), fakeCharactersRepository.requestedIds)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
            fakeCharactersRepository.pending?.complete(Unit)
            advanceUntilIdle()
            assertEquals(listOf(listOf(27)), fakeEpisodesRepository.requestedIds)
            assertEquals(
                DetailsUiState.Content(
                    character = recoveredCharacter,
                    episodes = EpisodesUiState.Content(episodes = fakeEpisodesRepository.episodes),
                ),
                detailsViewModel.state.value,
            )
        }

    @Test
    fun `GIVEN incomplete references WHEN their refresh fails THEN it retains facts and restores the section error`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = emptyList())
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            fakeCharactersRepository.result = CharacterDetailsResult.Failure(reason = CharacterRequestFailure.Network)

            detailsViewModel.process(action = DetailsAction.RetryEpisodes)
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Error), detailsViewModel.state.value)
            assertEquals(listOf(361, 361), fakeCharactersRepository.requestedIds)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
        }

    @Test
    fun `GIVEN incomplete references WHEN refresh still returns incomplete references THEN it keeps the section error`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = emptyList())
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            detailsViewModel.process(action = DetailsAction.RetryEpisodes)
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Error), detailsViewModel.state.value)
            assertEquals(listOf(361, 361), fakeCharactersRepository.requestedIds)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
        }

    @Test
    fun `GIVEN incomplete references WHEN refresh confirms no appearances THEN it shows empty episodes with updated facts`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = emptyList())
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            val recoveredCharacter = character.copy(episodeCount = 0)
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = recoveredCharacter)

            detailsViewModel.process(action = DetailsAction.RetryEpisodes)
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = recoveredCharacter), detailsViewModel.state.value)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
        }

    @Test
    fun `GIVEN a pending reference refresh WHEN the ViewModel is cleared THEN it cancels while retaining character facts`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = emptyList())
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            fakeCharactersRepository.pending = CompletableDeferred()
            val store = ViewModelStore()
            store.put("details", detailsViewModel)
            detailsViewModel.process(action = DetailsAction.RetryEpisodes)
            advanceUntilIdle()

            store.clear()
            advanceUntilIdle()

            assertTrue(fakeCharactersRepository.cancelled)
            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Loading), detailsViewModel.state.value)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
        }

    @Test
    fun `GIVEN failed episodes WHEN repeated section retries succeed THEN it keeps the character and makes one episode retry`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = listOf(27))
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            fakeEpisodesRepository.result = EpisodesResult.Failure(reason = CharacterRequestFailure.Network)
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Error), detailsViewModel.state.value)
            fakeEpisodesRepository.pending = CompletableDeferred()
            fakeEpisodesRepository.result = EpisodesResult.Success(episodes = fakeEpisodesRepository.episodes)

            repeat(4) { detailsViewModel.process(action = DetailsAction.RetryEpisodes) }
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Loading), detailsViewModel.state.value)
            assertEquals(listOf(361), fakeCharactersRepository.requestedIds)
            assertEquals(listOf(listOf(27), listOf(27)), fakeEpisodesRepository.requestedIds)
            fakeEpisodesRepository.pending?.complete(Unit)
            advanceUntilIdle()
            assertEquals(
                DetailsUiState.Content(
                    character = character,
                    episodes = EpisodesUiState.Content(episodes = fakeEpisodesRepository.episodes),
                ),
                detailsViewModel.state.value,
            )
        }

    @Test
    fun `GIVEN pending episodes WHEN the ViewModel is cleared THEN it cancels without hiding character facts`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = listOf(27))
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            fakeEpisodesRepository.pending = CompletableDeferred()
            val store = ViewModelStore()
            store.put("details", detailsViewModel)
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            store.clear()
            advanceUntilIdle()

            assertTrue(fakeEpisodesRepository.cancelled)
            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Loading), detailsViewModel.state.value)
        }

    @Test
    fun `GIVEN no episode references WHEN character detail loads THEN it shows empty episodes without a request`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(
                episodeCount = 0,
                episodeIds = emptyList(),
            )
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)

            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            val content = detailsViewModel.state.value as DetailsUiState.Content
            assertEquals(EpisodesUiState.Empty, content.episodes)
            assertTrue(fakeEpisodesRepository.requestedIds.isEmpty())
        }

    @Test
    fun `GIVEN pending episodes WHEN character detail loads THEN facts remain available while episodes load`() =
        runTest(mainDispatcher.dispatcher) {
            val character = (fakeCharactersRepository.result as CharacterDetailsResult.Success).character.copy(episodeIds = listOf(27))
            fakeCharactersRepository.result = CharacterDetailsResult.Success(character = character)
            fakeEpisodesRepository.pending = CompletableDeferred()

            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            assertEquals(DetailsUiState.Content(character = character, episodes = EpisodesUiState.Loading), detailsViewModel.state.value)
            assertEquals(listOf(listOf(27)), fakeEpisodesRepository.requestedIds)
            fakeEpisodesRepository.pending?.complete(Unit)
            advanceUntilIdle()
            assertEquals(
                DetailsUiState.Content(
                    character = character,
                    episodes = EpisodesUiState.Content(episodes = fakeEpisodesRepository.episodes),
                ),
                detailsViewModel.state.value,
            )
        }

    @Test
    fun `GIVEN a failed detail request WHEN retry succeeds THEN it loads content for the same character`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result = CharacterDetailsResult.Failure(
                reason = CharacterRequestFailure.Network,
            )
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            fakeCharactersRepository.result = CharacterDetailsResult.Success(
                character = CharacterDetails(
                    id = 361,
                    name = "Toxic Rick",
                    status = CharacterStatus.Dead,
                    species = "Humanoid",
                    gender = "Male",
                    type = null,
                    origin = "Detoxifier",
                    location = "Earth (Replacement Dimension)",
                    episodeCount = 1,
                    episodeIds = listOf(27),
                    imageUrl = null,
                ),
            )

            detailsViewModel.state.test {
                assertEquals(DetailsUiState.Error, awaitItem())
                detailsViewModel.process(action = DetailsAction.Retry)
                assertEquals(DetailsUiState.Loading, detailsViewModel.state.value)
                assertEquals(DetailsUiState.Loading, awaitItem())
                advanceUntilIdle()
                assertTrue(expectMostRecentItem() is DetailsUiState.Content)
            }
            assertEquals(listOf(361, 361), fakeCharactersRepository.requestedIds)
        }

    @Test
    fun `GIVEN a pending retry WHEN retry is requested again THEN it keeps one request for the same character`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result = CharacterDetailsResult.Failure(
                reason = CharacterRequestFailure.Service,
            )
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            fakeCharactersRepository.pending = gate

            repeat(4) {
                detailsViewModel.process(action = DetailsAction.Retry)
                detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
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
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            store.clear()
            advanceUntilIdle()

            assertTrue(fakeCharactersRepository.cancelled)
            assertEquals(DetailsUiState.Loading, detailsViewModel.state.value)
        }

    @Test
    fun `GIVEN loaded detail WHEN loading and observation repeat THEN it reuses content without another request`() =
        runTest(mainDispatcher.dispatcher) {
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()

            detailsViewModel.state.test { assertTrue(awaitItem() is DetailsUiState.Content) }
            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            detailsViewModel.process(action = DetailsAction.Load(characterId = 2))
            detailsViewModel.process(action = DetailsAction.Retry)
            advanceUntilIdle()

            assertEquals(listOf(361), fakeCharactersRepository.requestedIds)
            assertEquals(361, (detailsViewModel.state.value as DetailsUiState.Content).character.id)
        }

    @Test
    fun `GIVEN a missing character WHEN retry is requested THEN it keeps not found without another request`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.result = CharacterDetailsResult.NotFound

            detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
            advanceUntilIdle()
            detailsViewModel.process(action = DetailsAction.Retry)
            advanceUntilIdle()

            assertEquals(DetailsUiState.NotFound, detailsViewModel.state.value)
            assertEquals(listOf(361), fakeCharactersRepository.requestedIds)
        }

    @Test
    fun `GIVEN a pending initial request WHEN initialization repeats THEN it keeps only one detail request`() =
        runTest(mainDispatcher.dispatcher) {
            val gate = CompletableDeferred<Unit>()
            fakeCharactersRepository.pending = gate

            repeat(4) { detailsViewModel.process(action = DetailsAction.Load(characterId = 361)) }
            detailsViewModel.process(action = DetailsAction.Retry)
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

                detailsViewModel.process(action = DetailsAction.Load(characterId = 361))
                advanceUntilIdle()

                val content = expectMostRecentItem()
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
        character = CharacterDetails(
            id = 361, name = "Toxic Rick", status = CharacterStatus.Dead, species = "Humanoid", gender = "Male",
            type = "Rick's toxic side",
            origin = "Detoxifier",
            location = "Earth (Replacement Dimension)",
            episodeCount = 1,
            episodeIds = listOf(27),
            imageUrl = null,
        ),
    )
    var pending: CompletableDeferred<Unit>? = null
    var cancelled = false

    override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult =
        CharactersPageResult.Failure(reason = CharacterRequestFailure.Service)

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

private class FakeEpisodesRepository : EpisodesRepository {
    val requestedIds = mutableListOf<List<Int>>()
    val episodes = listOf(Episode(id = 27, name = "Rest and Ricklaxation", code = "S03E06", airDate = "August 27, 2017"))
    var result: EpisodesResult = EpisodesResult.Success(episodes = episodes)
    var pending: CompletableDeferred<Unit>? = null
    var cancelled = false

    override suspend fun getEpisodes(episodeIds: List<Int>): EpisodesResult {
        requestedIds += episodeIds
        return try {
            pending?.await()
            result
        } catch (error: CancellationException) {
            cancelled = true
            throw error
        }
    }
}
