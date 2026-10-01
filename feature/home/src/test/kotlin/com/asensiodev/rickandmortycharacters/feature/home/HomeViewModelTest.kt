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
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeSearchAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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
    fun `GIVEN a pending filtered append WHEN status changes THEN late old items cannot enter the new generation`() =
        runTest(mainDispatcher.dispatcher) {
            val appendStarted = CompletableDeferred<Unit>()
            val obsoleteAppend = CompletableDeferred<Unit>()
            fakeCharactersRepository.statusResponse = { number, _, status ->
                if (status == CharacterStatus.Alive && number == 2) {
                    appendStarted.complete(Unit)
                    withContext(NonCancellable) { obsoleteAppend.await() }
                    CharactersPageResult.Success(page(2, 60, null))
                } else {
                    CharactersPageResult.Success(
                        page(
                            1,
                            if (status == CharacterStatus.Alive) 60 else 20,
                            if (status == CharacterStatus.Alive) 2 else null,
                        ),
                    )
                }
            }
            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Alive))
            backgroundScope.launch { homeViewModel.characters.asSnapshot { scrollTo(19) } }
            repeat(10) {
                advanceTimeBy(100)
                runCurrent()
            }
            assertTrue(appendStarted.isCompleted)

            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Dead))
            repeat(10) {
                advanceTimeBy(100)
                runCurrent()
            }
            obsoleteAppend.complete(Unit)
            val snapshot = homeViewModel.characters.asSnapshot()

            assertEquals(20, snapshot.size)
            assertTrue(snapshot.all { it.generation == 2L })
            assertEquals(20, homeViewModel.state.value.totalCount)
            assertEquals(CharacterStatus.Dead, homeViewModel.state.value.selectedStatus)
        }

    @Test
    fun `GIVEN pending typing with an active filter WHEN that chip is selected again THEN debounce still applies the name`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Alive))
            homeViewModel.characters.asSnapshot()
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            advanceTimeBy(200)

            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Alive))
            assertEquals(1L, homeViewModel.state.value.generation)
            advanceTimeBy(101)
            runCurrent()
            homeViewModel.characters.asSnapshot()

            assertEquals(listOf(null, "Rick"), fakeCharactersRepository.requestedNames)
            assertEquals(listOf(CharacterStatus.Alive, CharacterStatus.Alive), fakeCharactersRepository.requestedStatuses)
            assertEquals(2L, homeViewModel.state.value.generation)
        }

    @Test
    fun `GIVEN combined constraints WHEN Clear suggestions and All are applied THEN each preserves the other constraint`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Unknown))
            homeViewModel.characters.asSnapshot()

            homeViewModel.onSearchAction(HomeSearchAction.Clear)
            homeViewModel.characters.asSnapshot()
            homeViewModel.onSearchAction(HomeSearchAction.Suggest("Beth"))
            homeViewModel.characters.asSnapshot()
            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(null))
            assertEquals(null, homeViewModel.state.value.totalCount)
            homeViewModel.characters.asSnapshot()

            assertEquals(listOf("Rick", null, "Beth", "Beth"), fakeCharactersRepository.requestedNames)
            assertEquals(
                listOf(CharacterStatus.Unknown, CharacterStatus.Unknown, CharacterStatus.Unknown, null),
                fakeCharactersRepository.requestedStatuses,
            )
            assertEquals("Beth", homeViewModel.state.value.searchText)
            assertEquals(4L, homeViewModel.state.value.generation)
        }

    @Test
    fun `GIVEN loaded filtered pages WHEN the selected chip is repeated THEN recollection preserves pages and total`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.addThreePages()
            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Dead))
            val first = homeViewModel.characters.asSnapshot { scrollTo(45) }

            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Dead))
            val second = homeViewModel.characters.asSnapshot()

            assertEquals(first, second)
            assertEquals(listOf(1, 2, 3), fakeCharactersRepository.requestedPages)
            assertTrue(fakeCharactersRepository.requestedStatuses.all { it == CharacterStatus.Dead })
            assertEquals(60, homeViewModel.state.value.totalCount)
            assertEquals(1L, homeViewModel.state.value.generation)
        }

    @Test
    fun `GIVEN a pending name edit WHEN a new status is selected THEN it immediately applies one combined query`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.onSearchAction(HomeSearchAction.Edit(" Rick "))
            advanceTimeBy(200)

            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Dead))
            assertEquals("Rick", homeViewModel.state.value.appliedName)
            val snapshot = homeViewModel.characters.asSnapshot()
            advanceTimeBy(500)
            runCurrent()

            assertEquals(CharacterStatus.Dead, homeViewModel.state.value.selectedStatus)
            assertEquals(listOf("Rick"), fakeCharactersRepository.requestedNames)
            assertEquals(listOf(CharacterStatus.Dead), fakeCharactersRepository.requestedStatuses)
            assertEquals(1L, homeViewModel.state.value.generation)
            assertTrue(snapshot.all { it.generation == 1L })
        }

    @Test
    fun `GIVEN a pending name request WHEN another name is applied THEN it cancels obsolete loading without a user error`() =
        runTest(mainDispatcher.dispatcher) {
            val cancelled = CompletableDeferred<Unit>()
            val pending = CompletableDeferred<Unit>()
            fakeCharactersRepository.response = { _, name ->
                if (name == "Rick") {
                    try {
                        pending.await()
                    } catch (error: CancellationException) {
                        cancelled.complete(Unit)
                        throw error
                    }
                }
                CharactersPageResult.Success(
                    CharacterPage(
                        listOf(CharacterSummary(1, "Beth", "Human", CharacterStatus.Alive, null)),
                        1,
                        null,
                    ),
                )
            }
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            backgroundScope.launch { homeViewModel.characters.asSnapshot() }
            repeat(10) { runCurrent() }

            homeViewModel.onSearchAction(HomeSearchAction.Edit("Beth"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            repeat(10) { runCurrent() }
            val snapshot = homeViewModel.characters.asSnapshot()

            assertTrue(cancelled.isCompleted)
            assertEquals(listOf("Beth"), snapshot.map { it.name })
            assertEquals(1, homeViewModel.state.value.totalCount)
        }

    @Test
    fun `GIVEN an applied name WHEN an equivalent name is submitted THEN it preserves the generation and pages`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            val first = homeViewModel.characters.asSnapshot()

            homeViewModel.onSearchAction(HomeSearchAction.Edit(" Rick "))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            advanceTimeBy(500)
            val second = homeViewModel.characters.asSnapshot()

            assertEquals(first, second)
            assertEquals(1L, homeViewModel.state.value.generation)
            assertEquals(listOf("Rick"), fakeCharactersRepository.requestedNames)
        }

    @Test
    fun `GIVEN a pending name edit WHEN search is cleared THEN it reloads the unfiltered result immediately`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            homeViewModel.characters.asSnapshot()
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Beth"))

            homeViewModel.onSearchAction(HomeSearchAction.Clear)
            homeViewModel.characters.asSnapshot()
            advanceTimeBy(500)
            runCurrent()

            assertEquals("", homeViewModel.state.value.searchText)
            assertEquals(null, homeViewModel.state.value.appliedName)
            assertEquals(listOf("Rick", null), fakeCharactersRepository.requestedNames)
            assertEquals(2L, homeViewModel.state.value.generation)
        }

    @Test
    fun `GIVEN a combined query WHEN appending fails and is retried THEN every page retains its name and status`() =
        runTest(mainDispatcher.dispatcher) {
            fakeCharactersRepository.addThreePages()
            fakeCharactersRepository.failOnce += 2
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)

            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Dead))

            val snapshot = homeViewModel.characters.asSnapshot(onError = { ErrorRecovery.RETRY }) { scrollTo(45) }

            assertEquals(60, snapshot.size)
            assertEquals(listOf(1, 2, 2, 3), fakeCharactersRepository.requestedPages)
            assertEquals(listOf("Rick", "Rick", "Rick", "Rick"), fakeCharactersRepository.requestedNames)
            assertTrue(fakeCharactersRepository.requestedStatuses.all { it == CharacterStatus.Dead })
            assertTrue(snapshot.all { it.generation == 2L })
        }

    @Test
    fun `GIVEN a late first query WHEN names change away and back THEN its old total cannot replace the current result`() =
        runTest(mainDispatcher.dispatcher) {
            val obsoleteResponse = CompletableDeferred<Unit>()
            var firstRick = true
            fakeCharactersRepository.response = { _, name ->
                if (name == "Rick" && firstRick) {
                    firstRick = false
                    withContext(NonCancellable) { obsoleteResponse.await() }
                    CharactersPageResult.Success(CharacterPage(emptyList(), 999, null))
                } else {
                    CharactersPageResult.Success(
                        CharacterPage(
                            listOf(CharacterSummary(42, name ?: "All", "Human", CharacterStatus.Alive, null)),
                            1,
                            null,
                        ),
                    )
                }
            }
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            backgroundScope.launch { homeViewModel.characters.asSnapshot() }
            repeat(10) { runCurrent() }
            assertEquals(listOf("Rick"), fakeCharactersRepository.requestedNames)

            homeViewModel.onSearchAction(HomeSearchAction.Edit("Beth"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            repeat(10) { runCurrent() }
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Rick"))
            homeViewModel.onSearchAction(HomeSearchAction.Submit)
            repeat(10) { runCurrent() }
            obsoleteResponse.complete(Unit)
            val snapshot = homeViewModel.characters.asSnapshot()

            assertEquals(1, homeViewModel.state.value.totalCount)
            assertEquals(3L, homeViewModel.state.value.generation)
            assertEquals(listOf(42), snapshot.map { it.id })
            assertTrue(snapshot.all { it.generation == 3L })
        }

    @Test
    fun `GIVEN a late filtered query WHEN status changes away and back THEN its old total cannot replace the current result`() =
        runTest(mainDispatcher.dispatcher) {
            val obsoleteResponse = CompletableDeferred<Unit>()
            var firstAlive = true
            fakeCharactersRepository.statusResponse = { _, name, status ->
                if (status == CharacterStatus.Alive && firstAlive) {
                    firstAlive = false
                    withContext(NonCancellable) { obsoleteResponse.await() }
                    CharactersPageResult.Success(CharacterPage(emptyList(), 999, null))
                } else {
                    CharactersPageResult.Success(
                        CharacterPage(
                            listOf(CharacterSummary(42, name ?: "All", "Human", CharacterStatus.Alive, null)),
                            1,
                            null,
                        ),
                    )
                }
            }
            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Alive))
            backgroundScope.launch { homeViewModel.characters.asSnapshot() }
            repeat(10) { runCurrent() }
            assertEquals(listOf(CharacterStatus.Alive), fakeCharactersRepository.requestedStatuses)

            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Dead))
            repeat(10) { runCurrent() }
            homeViewModel.onSearchAction(HomeSearchAction.SelectStatus(CharacterStatus.Alive))
            repeat(10) { runCurrent() }
            obsoleteResponse.complete(Unit)
            val snapshot = homeViewModel.characters.asSnapshot()

            assertEquals(1, homeViewModel.state.value.totalCount)
            assertEquals(3L, homeViewModel.state.value.generation)
            assertEquals(listOf(42), snapshot.map { it.id })
            assertTrue(snapshot.all { it.generation == 3L })
        }

    @Test
    fun `GIVEN loaded pages WHEN a new name is submitted THEN it starts a fresh named first page`() = runTest(mainDispatcher.dispatcher) {
        homeViewModel.characters.asSnapshot()
        fakeCharactersRepository.pages[1] = CharacterPage(
            listOf(CharacterSummary(90, "Beth", "Human", CharacterStatus.Alive, null)),
            1,
            null,
        )
        homeViewModel.onSearchAction(HomeSearchAction.Edit(" Beth "))

        homeViewModel.onSearchAction(HomeSearchAction.Submit)
        runCurrent()
        val snapshot = homeViewModel.characters.asSnapshot()
        advanceTimeBy(500)
        runCurrent()

        assertEquals(listOf(90), snapshot.map { it.id })
        assertEquals(listOf(null, "Beth"), fakeCharactersRepository.requestedNames)
        assertEquals(listOf(1, 1), fakeCharactersRepository.requestedPages)
        assertEquals(" Beth ", homeViewModel.state.value.searchText)
        assertEquals(1, homeViewModel.state.value.totalCount)
    }

    @Test
    fun `GIVEN successive name edits WHEN the debounce elapses THEN only the latest name is applied`() =
        runTest(mainDispatcher.dispatcher) {
            homeViewModel.onSearchAction(HomeSearchAction.Edit("Ri"))
            advanceTimeBy(200)
            homeViewModel.onSearchAction(HomeSearchAction.Edit(" Rick "))
            advanceTimeBy(299)
            runCurrent()
            assertEquals(null, homeViewModel.state.value.appliedName)

            advanceTimeBy(1)
            runCurrent()

            assertEquals("Rick", homeViewModel.state.value.appliedName)
            assertEquals(" Rick ", homeViewModel.state.value.searchText)
            assertEquals(1L, homeViewModel.state.value.generation)
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
            listOf(CharacterCardUiModel(1, "Rick", "Human", CharacterStatus.Alive, null)),
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
    val requestedNames = mutableListOf<String?>()
    val requestedStatuses = mutableListOf<CharacterStatus?>()
    val failOnce = mutableSetOf<Int>()
    var pending: CompletableDeferred<Unit>? = null
    var cancelled = false
    var statusResponse: (suspend (Int, String?, CharacterStatus?) -> CharactersPageResult)? = null
    var response: (suspend (Int, String?) -> CharactersPageResult)? = null

    fun addThreePages() {
        pages[1] = page(1, 60, 2)
        pages[2] = page(2, 60, 3)
        pages[3] = page(3, 60, null)
    }

    override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound

    override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
        requestedPages += page
        requestedNames += name
        requestedStatuses += status
        return try {
            val controlled = statusResponse?.invoke(page, name, status) ?: response?.invoke(page, name)
            if (controlled != null) return controlled
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
