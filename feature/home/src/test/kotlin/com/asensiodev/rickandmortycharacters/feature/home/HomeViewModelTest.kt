@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home

import androidx.lifecycle.ViewModelStore
import androidx.paging.LoadState
import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import com.asensiodev.rickandmortycharacters.core.testing.MainDispatcherRule
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterCardUiModel
import com.asensiodev.rickandmortycharacters.feature.home.model.CharacterStatusUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()
    private val fakeCharactersRepository = FakeCharactersRepository()
    private val viewModelStore = ViewModelStore()

    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        homeViewModel = HomeViewModel(fakeCharactersRepository)
        viewModelStore.put("home", homeViewModel)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
    }

    @Test
    fun `GIVEN remote characters WHEN paging is collected THEN it emits the repository catalogue`() = runTest(mainDispatcher.dispatcher) {
        fakeCharactersRepository.pages[1] = CharacterPage(
            listOf(CharacterSummary(1, "Rick", "Human", CharacterStatus.Alive, null)),
            1,
            null,
        )

        val snapshot = homeViewModel.characters.asSnapshot()

        assertEquals(
            listOf(CharacterCardUiModel(1, "Rick", "Human", CharacterStatusUi.Alive, null)),
            snapshot,
        )
        assertEquals(1, homeViewModel.state.value.totalCount)
    }

    @Test
    fun `GIVEN several pages WHEN scrolling through them THEN all characters appear in page order`() = runTest(mainDispatcher.dispatcher) {
        fakeCharactersRepository.addThreePages()

        val snapshot = homeViewModel.characters.asSnapshot { scrollTo(45) }

        assertEquals((1..60).toList(), snapshot.map { it.id })
        assertEquals(listOf(1, 2, 3), fakeCharactersRepository.requestedPages)
        assertEquals(60, homeViewModel.state.value.totalCount)
    }

    @Test
    fun `GIVEN an append failure WHEN retry is requested THEN it retains earlier pages and retries the failed page`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.addThreePages()
            fakeCharactersRepository.failOnce += 2
            var errors = 0

            val snapshot = homeViewModel.characters.asSnapshot(onError = { states ->
                assertTrue(states.append is LoadState.Error)
                errors++
                ErrorRecovery.RETRY
            }) { scrollTo(45) }

            assertEquals(1, errors)
            assertEquals((1..60).toList(), snapshot.map { it.id })
            assertEquals(listOf(1, 2, 2, 3), fakeCharactersRepository.requestedPages)
        }

    @Test
    fun `GIVEN an initial load failure WHEN retry succeeds THEN it emits the first page and its total`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.failOnce += 1

            val snapshot = homeViewModel.characters.asSnapshot(onError = { states ->
                assertTrue(states.refresh is LoadState.Error)
                assertEquals(null, homeViewModel.state.value.totalCount)
                ErrorRecovery.RETRY
            })

            assertEquals((1..20).toList(), snapshot.map { it.id })
            assertEquals(listOf(1, 1), fakeCharactersRepository.requestedPages)
            assertEquals(20, homeViewModel.state.value.totalCount)
        }

    @Test
    fun `GIVEN a loaded catalogue WHEN it is collected again THEN it reuses pages without reloading`() =
        runTest(mainDispatcher.dispatcher) {
            val first = homeViewModel.characters.asSnapshot()

            val second = homeViewModel.characters.asSnapshot()

            assertEquals(first, second)
            assertEquals(listOf(1), fakeCharactersRepository.requestedPages)
        }

    @Test
    fun `GIVEN an empty first page WHEN paging is collected THEN it emits no characters`() = runTest(mainDispatcher.dispatcher) {
        fakeCharactersRepository.pages[1] = CharacterPage(emptyList(), 0, null)

        val snapshot = homeViewModel.characters.asSnapshot()

        assertTrue(snapshot.isEmpty())
        assertEquals(0, homeViewModel.state.value.totalCount)
        assertEquals(listOf(1), fakeCharactersRepository.requestedPages)
    }

    @Test
    fun `GIVEN a service failure WHEN paging is collected THEN it reports an error without emitting content`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.failOnce += 1
            var errorObserved = false

            val snapshot = homeViewModel.characters.asSnapshot(onError = { states ->
                errorObserved = states.refresh is LoadState.Error
                ErrorRecovery.RETURN_CURRENT_SNAPSHOT
            })

            assertTrue(errorObserved)
            assertTrue(snapshot.isEmpty())
            assertEquals(null, homeViewModel.state.value.totalCount)
        }

    @Test
    fun `GIVEN a pending request WHEN the ViewModel is cleared THEN it cancels loading without reporting an error`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.pending = CompletableDeferred()
            backgroundScope.launch { homeViewModel.characters.asSnapshot() }
            repeat(10) { runCurrent() }
            assertEquals(listOf(1), fakeCharactersRepository.requestedPages)

            viewModelStore.clear()
            repeat(10) { runCurrent() }

            assertTrue(fakeCharactersRepository.cancelled)
            assertEquals(null, homeViewModel.state.value.totalCount)
        }
}

private class FakeCharactersRepository : CharactersRepository {
    val pages = mutableMapOf(1 to page(1, 20, null))
    val requestedPages = mutableListOf<Int>()
    val failOnce = mutableSetOf<Int>()
    var pending: CompletableDeferred<Unit>? = null
    var cancelled = false

    fun addThreePages() {
        pages[1] = page(1, 60, 2)
        pages[2] = page(2, 60, 3)
        pages[3] = page(3, 60, null)
    }

    override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

    override suspend fun getPage(page: Int): CharactersPageResult {
        requestedPages += page
        return try {
            pending?.await()
            if (failOnce.remove(page)) {
                CharactersPageResult.Failure(CharacterRequestFailure.Service)
            } else {
                pages[page]?.let(CharactersPageResult::Success)
                    ?: CharactersPageResult.EndOfCatalogue
            }
        } catch (error: CancellationException) {
            cancelled = true
            throw error
        }
    }
}

private fun page(number: Int, total: Int, next: Int?): CharacterPage = CharacterPage(
    ((number - 1) * 20 + 1..number * 20).map {
        CharacterSummary(it, "Character $it", "Human", CharacterStatus.Alive, null)
    },
    total,
    next,
)
