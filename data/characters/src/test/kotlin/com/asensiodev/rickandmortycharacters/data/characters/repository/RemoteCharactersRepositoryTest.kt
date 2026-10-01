@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.data.characters.repository

import com.asensiodev.rickandmortycharacters.data.characters.remote.createCharactersApi
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class RemoteCharactersRepositoryTest {
    private val server = MockWebServer()

    private lateinit var charactersRepository: CharactersRepository

    @Before
    fun setUp() {
        server.start()
        val api = createCharactersApi(server.url("/api/"), OkHttpClient.Builder().build())
        charactersRepository = RemoteCharactersRepository(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `GIVEN supported status filters WHEN pages are requested THEN each retains its constraints`() = runTest {
        for ((status, expected) in listOf(
            CharacterStatus.Alive to "alive",
            CharacterStatus.Dead to "dead",
            CharacterStatus.Unknown to "unknown",
            null to null,
        )) {
            for (page in 1..2) {
                server.enqueue(MockResponse.Builder().body("""{"info":{"count":0,"next":null},"results":[]}""").build())

                charactersRepository.getPage(page, " Rick ", status)

                val request = server.takeRequest()
                assertEquals(expected, request.url.queryParameter("status"))
                assertEquals("Rick", request.url.queryParameter("name"))
                assertEquals(page.toString(), request.url.queryParameter("page"))
            }
        }
    }

    @Test
    fun `GIVEN a recognized filtered no match WHEN its first page is requested THEN status alone is sufficient for emptiness`() = runTest {
        for (name in listOf(null, "Rick")) {
            server.enqueue(MockResponse.Builder().code(404).body("""{"error":"There is nothing here"}""").build())

            val result = charactersRepository.getPage(1, name, CharacterStatus.Unknown)

            assertEquals(CharactersPageResult.Success(CharacterPage(emptyList(), 0, null)), result)
        }
        server.enqueue(MockResponse.Builder().code(404).body("{} ").build())
        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Service),
            charactersRepository.getPage(1, null, CharacterStatus.Unknown),
        )
    }

    @Test
    fun `GIVEN a status constraint WHEN a page is requested THEN it sends the API status value`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"info":{"count":0,"next":null},"results":[]}""").build())

        charactersRepository.getPage(1, "Rick", CharacterStatus.Dead)

        val request = server.takeRequest()
        assertEquals("dead", request.url.queryParameter("status"))
        assertEquals("Rick", request.url.queryParameter("name"))
    }

    @Test
    fun `GIVEN a spaced name WHEN each page is requested THEN it retains the trimmed encoded name`() = runTest {
        val body = requireNotNull(javaClass.getResource("/characters-page.json")).readText()
        for (page in 1..2) {
            server.enqueue(MockResponse.Builder().body(body).build())

            charactersRepository.getPage(page, "  Rick & Morty  ")

            val request = server.takeRequest()
            assertEquals("Rick & Morty", request.url.queryParameter("name"))
            assertEquals(page.toString(), request.url.queryParameter("page"))
        }
    }

    @Test
    fun `GIVEN a blank name WHEN its page is requested THEN it omits the name constraint`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"info":{"count":0,"next":null},"results":[]}""").build())

        charactersRepository.getPage(1, "   ")

        assertEquals(null, server.takeRequest().url.queryParameter("name"))
    }

    @Test
    fun `GIVEN unknown filtered errors WHEN their first page is requested THEN they remain failures`() = runTest {
        for (body in listOf("{broken", "{}", """{"error":"Unavailable"}""")) {
            server.enqueue(MockResponse.Builder().code(404).body(body).build())

            val result = charactersRepository.getPage(1, "Rick")

            assertEquals(CharactersPageResult.Failure(CharacterRequestFailure.Service), result)
        }
    }

    @Test
    fun `GIVEN a name without matches WHEN its first page is requested THEN it returns an empty result`() = runTest {
        server.enqueue(MockResponse.Builder().code(404).body("""{"error":"There is nothing here"}""").build())

        val result = charactersRepository.getPage(1, "Missing name")

        assertEquals(CharactersPageResult.Success(CharacterPage(emptyList(), 0, null)), result)
        assertEquals("Missing name", server.takeRequest().url.queryParameter("name"))
    }

    @Test
    fun `GIVEN a recognized end response WHEN the next page is requested THEN it confirms the catalogue has ended`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(
                404,
            ).body("""{"error":"There is nothing here"}""").build(),
        )

        val result = charactersRepository.getPage(2)

        assertEquals(CharactersPageResult.EndOfCatalogue, result)
        assertEquals("2", server.takeRequest().url.queryParameter("page"))
    }

    @Test
    fun `GIVEN unexpected append errors WHEN the next page is requested THEN they remain request failures`() = runTest {
        val bodies = listOf("{broken", "{}", "", """{"error":"Unavailable"}""")
        for (body in bodies) {
            server.enqueue(MockResponse.Builder().code(404).body(body).build())

            val result = charactersRepository.getPage(2)

            assertEquals(CharactersPageResult.Failure(CharacterRequestFailure.Service), result)
        }
        server.enqueue(
            MockResponse.Builder().code(
                503,
            ).body("""{"error":"There is nothing here"}""").build(),
        )

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Service),
            charactersRepository.getPage(2),
        )
    }

    @Test
    fun `GIVEN the final page WHEN it is requested THEN it preserves the API total and has no next page`() = runTest {
        val body = requireNotNull(javaClass.getResource("/characters-page.json")).readText()
            .replace("\"https://rickandmortyapi.com/api/character?page=2\"", "null")
        server.enqueue(MockResponse.Builder().body(body).build())

        val result = charactersRepository.getPage(3) as CharactersPageResult.Success

        assertEquals(57, result.page.totalCount)
        assertEquals(null, result.page.nextPage)
        assertEquals(2, result.page.characters.size)
        assertEquals("3", server.takeRequest().url.queryParameter("page"))
    }

    @Test
    fun `GIVEN a pending page request WHEN it is cancelled THEN cancellation propagates to the caller`() = runTest {
        server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.Stall).build())
        val pending = async { charactersRepository.getPage(1) }
        val request = withContext(Dispatchers.IO) { server.takeRequest(5, TimeUnit.SECONDS) }
        assertNotNull(request)

        pending.cancel()
        try {
            pending.await()
            fail("Cancellation must not become a repository result")
        } catch (_: kotlinx.coroutines.CancellationException) {
            assertTrue(pending.isCancelled)
        }
    }

    @Test
    fun `GIVEN missing required fields WHEN a page is requested THEN it reports an invalid response`() = runTest {
        server.enqueue(
            MockResponse.Builder().body("""{"info":{"count":1,"next":null}}""").build(),
        )

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse),
            result,
        )
    }

    @Test
    fun `GIVEN an absent response body WHEN a page is requested THEN it reports an invalid response`() = runTest {
        server.enqueue(MockResponse.Builder().code(204).build())

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse),
            result,
        )
    }

    @Test
    fun `GIVEN an unfiltered first page returning 404 WHEN it is requested THEN it reports a service failure`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(
                404,
            ).body("""{"error":"There is nothing here"}""").build(),
        )

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Service),
            result,
        )
    }

    @Test
    fun `GIVEN an empty API result WHEN a page is requested THEN it returns an empty character page`() = runTest {
        server.enqueue(
            MockResponse.Builder().body(
                """{"info":{"count":0,"next":null},"results":[]}""",
            ).build(),
        )

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Success(CharacterPage(emptyList(), 0, null)),
            result,
        )
    }

    @Test
    fun `GIVEN malformed JSON WHEN a page is requested THEN it reports an invalid response`() = runTest {
        server.enqueue(MockResponse.Builder().body("{broken").build())

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse),
            result,
        )
    }

    @Test
    fun `GIVEN a connection failure WHEN a page is requested THEN it reports a network failure`() = runTest {
        server.close()

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Network),
            result,
        )
    }

    @Test
    fun `GIVEN a server error WHEN a page is requested THEN it reports a service failure`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(
                503,
            ).body("""{"error":"Service unavailable"}""").build(),
        )

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Service),
            result,
        )
    }

    @Test
    fun `GIVEN a valid response WHEN a page is requested THEN it maps character summaries and pagination metadata`() = runTest {
        val body = requireNotNull(javaClass.getResource("/characters-page.json")).readText()
        server.enqueue(MockResponse.Builder().body(body).build())

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Success(
                CharacterPage(
                    listOf(
                        CharacterSummary(
                            1,
                            "Rick Sanchez",
                            "Human",
                            CharacterStatus.Alive,
                            "https://images.example/rick.jpeg",
                        ),
                        CharacterSummary(
                            2,
                            "Morty Smith",
                            "Human",
                            CharacterStatus.Unknown,
                            null,
                        ),
                    ),
                    totalCount = 57,
                    nextPage = 2,
                ),
            ),
            result,
        )
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/character", request.url.encodedPath)
        assertEquals("1", request.url.queryParameter("page"))
    }
}
