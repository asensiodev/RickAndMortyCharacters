@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.data.characters.repository

import com.asensiodev.rickandmortycharacters.data.characters.remote.createCharactersApi
import com.asensiodev.rickandmortycharacters.data.characters.remote.createCharactersHttpClient
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CharacterHttpCacheTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val server = MockWebServer()
    private val cacheDirectory by lazy { temporaryFolder.newFolder("cache") }
    private val client by lazy { createCharactersHttpClient(cacheDirectory) }
    private val reopenedClients = mutableListOf<OkHttpClient>()

    private lateinit var charactersRepository: CharactersRepository

    @Before
    fun setUp() {
        server.start()
        charactersRepository = RemoteCharactersRepository(createCharactersApi(server.url("/api/"), client))
    }

    @After
    fun tearDown() {
        (reopenedClients + client).forEach(::closeClient)
        server.close()
    }

    @Test
    fun `GIVEN a fresh page WHEN the same page is requested twice THEN it reuses the stored response`() = runTest {
        server.enqueue(pageResponse(20))
        server.enqueue(pageResponse(99))

        val first = charactersRepository.getPage(1)
        val repeated = charactersRepository.getPage(1)

        assertEquals(20, (first as CharactersPageResult.Success).page.totalCount)
        assertEquals(first, repeated)
        assertEquals(1, server.requestCount)
        server.takeRequest()
        assertNull(server.takeRequest(100, TimeUnit.MILLISECONDS))
    }

    @Test
    fun `GIVEN a fresh detail WHEN its cache is reopened THEN it reuses the persisted response`() = runTest {
        server.enqueue(detailResponse(361))
        server.enqueue(detailResponse(361, "Changed Rick"))
        val first = charactersRepository.getDetails(361)
        closeClient(client)
        val reopened = createCharactersHttpClient(cacheDirectory).also(reopenedClients::add)
        charactersRepository = RemoteCharactersRepository(createCharactersApi(server.url("/api/"), reopened))

        val repeated = charactersRepository.getDetails(361)

        assertEquals("Toxic Rick", (first as CharacterDetailsResult.Success).character.name)
        assertEquals(first, repeated)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `GIVEN a fresh detail WHEN its ID is requested twice THEN it avoids another HTTP request`() = runTest {
        server.enqueue(detailResponse(361))
        server.enqueue(detailResponse(361, "Changed Rick"))

        val first = charactersRepository.getDetails(361)
        val repeated = charactersRepository.getDetails(361)

        assertEquals(first, repeated)
        assertEquals("Toxic Rick", (repeated as CharacterDetailsResult.Success).character.name)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `GIVEN a stale ETag WHEN validation returns 304 THEN it reuses the body with renewed freshness`() = runTest {
        server.enqueue(pageResponse(20, "max-age=60", age = 120))
        server.enqueue(
            MockResponse.Builder().code(304).addHeader("ETag", "\"version-1\"")
                .addHeader("Cache-Control", "max-age=3600").addHeader("Age", "0").build(),
        )
        server.enqueue(pageResponse(99))
        val first = charactersRepository.getPage(1)

        val validated = charactersRepository.getPage(1)
        val repeated = charactersRepository.getPage(1)

        assertEquals(first, validated)
        assertEquals(first, repeated)
        assertEquals(2, server.requestCount)
        assertNull(server.takeRequest().headers["If-None-Match"])
        assertEquals("\"version-1\"", server.takeRequest().headers["If-None-Match"])
    }

    @Test
    fun `GIVEN a stale ETag WHEN validation returns changed content THEN it stores the replacement body`() = runTest {
        server.enqueue(pageResponse(20, "max-age=60", age = 120))
        server.enqueue(pageResponse(99, etag = "\"version-2\""))
        server.enqueue(pageResponse(50))
        charactersRepository.getPage(1)

        val changed = charactersRepository.getPage(1)
        val repeated = charactersRepository.getPage(1)

        assertEquals(99, (changed as CharactersPageResult.Success).page.totalCount)
        assertEquals(changed, repeated)
        assertEquals(2, server.requestCount)
        server.takeRequest()
        assertEquals("\"version-1\"", server.takeRequest().headers["If-None-Match"])
    }

    @Test
    fun `GIVEN a stale response without a validator WHEN it is requested again THEN it fetches normally`() = runTest {
        server.enqueue(pageResponse(20, "max-age=60", age = 120, etag = null))
        server.enqueue(pageResponse(99))
        charactersRepository.getPage(1)

        val result = charactersRepository.getPage(1)

        assertEquals(99, (result as CharactersPageResult.Success).page.totalCount)
        assertEquals(2, server.requestCount)
        server.takeRequest()
        val request = server.takeRequest()
        assertNull(request.headers["If-None-Match"])
        assertNull(request.headers["If-Modified-Since"])
    }

    @Test
    fun `GIVEN distinct page name and status URLs WHEN each is repeated THEN it returns its own stored result`() = runTest {
        val queries = listOf(
            Triple(1, "Rick", null),
            Triple(2, "Rick", null),
            Triple(1, "Morty", null),
            Triple(1, "Rick", CharacterStatus.Unknown),
            Triple(1, "Rick", CharacterStatus.Alive),
            Triple(1, "Rick", CharacterStatus.Dead),
        )
        queries.forEachIndexed { index, (page, name, status) ->
            server.enqueue(pageResponse(index + 20))
            charactersRepository.getPage(page, name, status)
        }
        server.enqueue(pageResponse(99))

        val repeated = queries.map { (page, name, status) -> charactersRepository.getPage(page, name, status) }

        assertEquals(listOf(20, 21, 22, 23, 24, 25), repeated.map { (it as CharactersPageResult.Success).page.totalCount })
        assertEquals(6, server.requestCount)
        queries.forEach { (page, name, status) ->
            val request = server.takeRequest()
            assertEquals(page.toString(), request.url.queryParameter("page"))
            assertEquals(name, request.url.queryParameter("name"))
            assertEquals(status?.name?.lowercase(), request.url.queryParameter("status"))
        }
    }

    @Test
    fun `GIVEN distinct detail IDs and a page WHEN each is repeated THEN their bodies remain separate`() = runTest {
        server.enqueue(detailResponse(361))
        server.enqueue(detailResponse(362, "Other Rick"))
        server.enqueue(pageResponse(20))
        charactersRepository.getDetails(361)
        charactersRepository.getDetails(362)
        charactersRepository.getPage(1)
        server.enqueue(pageResponse(99))

        val first = charactersRepository.getDetails(361) as CharacterDetailsResult.Success
        val second = charactersRepository.getDetails(362) as CharacterDetailsResult.Success
        val page = charactersRepository.getPage(1) as CharactersPageResult.Success

        assertEquals(361, first.character.id)
        assertEquals("Toxic Rick", first.character.name)
        assertEquals(362, second.character.id)
        assertEquals("Other Rick", second.character.name)
        assertEquals(20, page.page.totalCount)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `GIVEN a Vary response WHEN its request header changes THEN it fetches the matching variant`() = runTest {
        val englishClient = client.newBuilder().addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("Accept-Language", "en").build())
        }.build()
        charactersRepository = RemoteCharactersRepository(createCharactersApi(server.url("/api/"), englishClient))
        server.enqueue(pageResponse(20, vary = "Accept-Language"))
        charactersRepository.getPage(1)
        val spanishClient = client.newBuilder().addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("Accept-Language", "es").build())
        }.build()
        charactersRepository = RemoteCharactersRepository(createCharactersApi(server.url("/api/"), spanishClient))
        server.enqueue(pageResponse(99, vary = "Accept-Language"))
        server.enqueue(pageResponse(50))

        val changed = charactersRepository.getPage(1)
        val repeated = charactersRepository.getPage(1)

        assertEquals(99, (changed as CharactersPageResult.Success).page.totalCount)
        assertEquals(changed, repeated)
        assertEquals(2, server.requestCount)
        assertEquals("en", server.takeRequest().headers["Accept-Language"])
        assertEquals("es", server.takeRequest().headers["Accept-Language"])
    }

    @Test
    fun `GIVEN no-store WHEN a request is repeated THEN it fetches a new response`() = runTest {
        server.enqueue(pageResponse(20, "no-store"))
        server.enqueue(pageResponse(99, "no-store"))
        charactersRepository.getPage(1)

        val result = charactersRepository.getPage(1)

        assertEquals(99, (result as CharactersPageResult.Success).page.totalCount)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `GIVEN no-cache with an ETag WHEN a request is repeated THEN it validates before reuse`() = runTest {
        server.enqueue(pageResponse(20, "no-cache, max-age=3600"))
        server.enqueue(MockResponse.Builder().code(304).addHeader("Cache-Control", "no-cache").build())
        val first = charactersRepository.getPage(1)

        val result = charactersRepository.getPage(1)

        assertEquals(first, result)
        assertEquals(2, server.requestCount)
        server.takeRequest()
        assertEquals("\"version-1\"", server.takeRequest().headers["If-None-Match"])
    }

    @Test
    fun `GIVEN an expired entry WHEN transport fails THEN it returns a network failure instead of stale content`() = runTest {
        server.enqueue(pageResponse(20, "max-age=60", age = 120))
        charactersRepository.getPage(1)
        server.close()

        val result = charactersRepository.getPage(1)

        assertEquals(CharactersPageResult.Failure(CharacterRequestFailure.Network), result)
    }

    @Test
    fun `GIVEN an expired entry WHEN validation returns 503 THEN it returns a service failure instead of stale content`() = runTest {
        server.enqueue(pageResponse(20, "max-age=60", age = 120))
        server.enqueue(MockResponse.Builder().code(503).body("unavailable").build())
        charactersRepository.getPage(1)

        val result = charactersRepository.getPage(1)

        assertEquals(CharactersPageResult.Failure(CharacterRequestFailure.Service), result)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `GIVEN cache storage was cleared WHEN a page is requested THEN it uses the normal network path`() = runTest {
        server.enqueue(pageResponse(20))
        charactersRepository.getPage(1)
        client.cache?.evictAll()
        server.enqueue(pageResponse(99))

        val result = charactersRepository.getPage(1)

        assertEquals(99, (result as CharactersPageResult.Success).page.totalCount)
        assertEquals(2, server.requestCount)
    }

    private fun pageResponse(
        total: Int,
        policy: String = "public, max-age=3600",
        age: Int = 0,
        etag: String? = "\"version-1\"",
        vary: String? = null,
    ): MockResponse {
        val body = requireNotNull(javaClass.getResource("/characters-page.json")).readText()
            .replace("\"count\": 57", "\"count\": $total")
        return MockResponse.Builder().addHeader("Cache-Control", policy).addHeader("Age", age)
            .apply {
                etag?.let { addHeader("ETag", it) }
                vary?.let { addHeader("Vary", it) }
            }.body(body).build()
    }

    private fun detailResponse(id: Int, name: String = "Toxic Rick"): MockResponse {
        val body = requireNotNull(javaClass.getResource("/character-detail.json")).readText()
            .replace("361", id.toString()).replace("Toxic Rick", name)
        return MockResponse.Builder().addHeader("Cache-Control", "public, max-age=3600").body(body).build()
    }

    private fun closeClient(httpClient: OkHttpClient) {
        httpClient.cache?.close()
        httpClient.connectionPool.evictAll()
        httpClient.dispatcher.executorService.shutdown()
    }
}
