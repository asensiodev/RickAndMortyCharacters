@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.data.characters.repository

import com.asensiodev.rickandmortycharacters.data.characters.remote.createCharactersApi
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
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

class RemoteCharacterDetailsTest {
    private val server = MockWebServer()

    private lateinit var charactersRepository: CharactersRepository

    @Before
    fun setUp() {
        server.start()
        charactersRepository = RemoteCharactersRepository(
            createCharactersApi(server.url("/api/"), OkHttpClient.Builder().build()),
        )
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `GIVEN Toxic Rick WHEN his detail is requested THEN it maps his identity facts and episode count`() = runTest {
        server.enqueue(MockResponse.Builder().body(detailJson()).build())

        val result = charactersRepository.getDetails(361)

        assertEquals(
            CharacterDetailsResult.Success(
                CharacterDetails(
                    361, "Toxic Rick", CharacterStatus.Dead, "Humanoid", "Male",
                    "Rick's toxic side", "Detoxifier", "Earth (Replacement Dimension)",
                    1, "https://rickandmortyapi.com/api/character/avatar/361.jpeg",
                ),
            ),
            result,
        )
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/character/361", request.url.encodedPath)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `GIVEN a recognized missing character response WHEN detail is requested THEN it reports not found`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(
                404,
            ).body("""{"error":"Character not found"}""").build(),
        )

        val result = charactersRepository.getDetails(999999999)

        assertEquals(CharacterDetailsResult.NotFound, result)
    }

    @Test
    fun `GIVEN unexpected HTTP errors WHEN detail is requested THEN it reports a service failure`() = runTest {
        val errors = listOf(
            404 to """{"error":"There is nothing here"}""",
            404 to "{broken",
            404 to "[]",
            503 to """{"error":"Character not found"}""",
        )
        for ((code, body) in errors) {
            server.enqueue(MockResponse.Builder().code(code).body(body).build())

            val result = charactersRepository.getDetails(361)

            assertEquals(
                CharacterDetailsResult.Failure(CharacterRequestFailure.Service),
                result,
            )
        }
    }

    @Test
    fun `GIVEN invalid required data WHEN detail is requested THEN it reports an invalid response`() = runTest {
        for (body in listOf("{broken", "{}", detailJson().replace("361", "362"))) {
            server.enqueue(MockResponse.Builder().body(body).build())

            val result = charactersRepository.getDetails(361)

            assertEquals(
                CharacterDetailsResult.Failure(CharacterRequestFailure.InvalidResponse),
                result,
            )
        }
        server.enqueue(MockResponse.Builder().code(204).build())
        assertEquals(
            CharacterDetailsResult.Failure(CharacterRequestFailure.InvalidResponse),
            charactersRepository.getDetails(361),
        )
    }

    @Test
    fun `GIVEN a connection failure WHEN detail is requested THEN it reports a network failure`() = runTest {
        server.close()

        val result = charactersRepository.getDetails(361)

        assertEquals(CharacterDetailsResult.Failure(CharacterRequestFailure.Network), result)
    }

    @Test
    fun `GIVEN a pending detail request WHEN it is cancelled THEN cancellation propagates to the caller`() = runTest {
        server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.Stall).build())
        val request = async { charactersRepository.getDetails(361) }
        assertNotNull(withContext(Dispatchers.IO) { server.takeRequest(5, TimeUnit.SECONDS) })

        request.cancel()
        try {
            request.await()
            fail("Cancellation must not become a repository outcome")
        } catch (_: kotlinx.coroutines.CancellationException) {
            assertTrue(request.isCancelled)
        }
    }

    @Test
    fun `GIVEN empty optional values WHEN detail is requested THEN type and portrait remain absent`() = runTest {
        val body = detailJson().replace("Rick's toxic side", "")
            .replace("https://rickandmortyapi.com/api/character/avatar/361.jpeg", "")
        server.enqueue(MockResponse.Builder().body(body).build())

        val result = charactersRepository.getDetails(361) as CharacterDetailsResult.Success

        assertEquals(null, result.character.type)
        assertEquals(null, result.character.imageUrl)
        assertEquals("Detoxifier", result.character.origin)
    }

    @Test
    fun `GIVEN absent optional fields and unknown values WHEN detail is requested THEN it preserves the supplied facts`() = runTest {
        server.enqueue(
            MockResponse.Builder().body(
                """{
              "id":361,"name":"Toxic Rick","status":"unknown","species":"unknown",
              "gender":"unknown","origin":{"name":"unknown"},"location":{"name":"unknown"},
              "episode":[]
            }""",
            ).build(),
        )

        val result = charactersRepository.getDetails(361) as CharacterDetailsResult.Success

        assertEquals(null, result.character.type)
        assertEquals(null, result.character.imageUrl)
        assertEquals(CharacterStatus.Unknown, result.character.status)
        assertEquals("unknown", result.character.gender)
        assertEquals("unknown", result.character.origin)
        assertEquals(0, result.character.episodeCount)
    }

    private fun detailJson(): String = requireNotNull(
        javaClass.getResource("/character-detail.json"),
    ).readText()
}
