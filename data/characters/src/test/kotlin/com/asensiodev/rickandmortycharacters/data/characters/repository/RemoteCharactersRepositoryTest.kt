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
    private lateinit var server: MockWebServer

    private lateinit var charactersRepository: CharactersRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = createCharactersApi(server.url("/api/"), OkHttpClient.Builder().build())
        charactersRepository = RemoteCharactersRepository(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `GIVEN pending request WHEN cancelled THEN cancellation propagates`() = runTest {
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
    fun `GIVEN missing fields WHEN requesting page THEN invalid response`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"info":{"count":1,"next":null}}""").build())

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse),
            result,
        )
    }

    @Test
    fun `GIVEN absent body WHEN requesting page THEN invalid response`() = runTest {
        server.enqueue(MockResponse.Builder().code(204).build())

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse),
            result,
        )
    }

    @Test
    fun `GIVEN unfiltered not found WHEN requesting page THEN service failure`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(404).body("""{"error":"There is nothing here"}""").build(),
        )

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Service),
            result,
        )
    }

    @Test
    fun `GIVEN empty response WHEN requesting page THEN empty page`() = runTest {
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
    fun `GIVEN malformed response WHEN requesting page THEN invalid response`() = runTest {
        server.enqueue(MockResponse.Builder().body("{broken").build())

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse),
            result,
        )
    }

    @Test
    fun `GIVEN connection failure WHEN requesting page THEN network failure`() = runTest {
        server.close()

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Network),
            result,
        )
    }

    @Test
    fun `GIVEN server failure WHEN requesting page THEN service failure`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(503).body("""{"error":"Service unavailable"}""").build(),
        )

        val result = charactersRepository.getPage(1)

        assertEquals(
            CharactersPageResult.Failure(CharacterRequestFailure.Service),
            result,
        )
    }

    @Test
    fun `GIVEN valid response WHEN requesting page THEN summaries and metadata`() = runTest {
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
                        CharacterSummary(2, "Morty Smith", "Human", CharacterStatus.Unknown, null),
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
