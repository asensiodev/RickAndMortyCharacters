@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.data.characters.repository

import com.asensiodev.rickandmortycharacters.data.characters.remote.createCharactersApi
import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesResult
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RemoteEpisodesRepositoryTest {
    private val server = MockWebServer()

    private lateinit var episodesRepository: EpisodesRepository

    @Before
    fun setUp() {
        server.start()
        episodesRepository = RemoteEpisodesRepository(
            api = createCharactersApi(baseUrl = server.url("/api/"), client = OkHttpClient.Builder().build()),
        )
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `GIVEN episode references WHEN their batch loads out of order THEN it preserves the requested order and real facts`() = runTest {
        server.enqueue(
            MockResponse.Builder().body(
                """[
            {"id":2,"name":"Lawnmower Dog","episode":"S01E02","air_date":"December 9, 2013"},
            {"id":1,"name":"Pilot","episode":"S01E01","air_date":"December 2, 2013"}
        ]""",
            ).build(),
        )

        val result = episodesRepository.getEpisodes(episodeIds = listOf(1, 2))

        assertEquals(
            EpisodesResult.Success(
                episodes = listOf(
                    Episode(id = 1, name = "Pilot", code = "S01E01", airDate = "December 2, 2013"),
                    Episode(id = 2, name = "Lawnmower Dog", code = "S01E02", airDate = "December 9, 2013"),
                ),
            ),
            result,
        )
        assertEquals("/api/episode/1,2", server.takeRequest().url.encodedPath)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `GIVEN one episode WHEN the API returns an object THEN it maps the single appearance`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"id":1,"name":"Pilot","episode":"S01E01","air_date":"December 2, 2013"}""").build())

        val result = episodesRepository.getEpisodes(episodeIds = listOf(1))

        assertEquals(
            EpisodesResult.Success(episodes = listOf(Episode(id = 1, name = "Pilot", code = "S01E01", airDate = "December 2, 2013"))),
            result,
        )
        assertEquals("/api/episode/1", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `GIVEN no references WHEN episodes are requested THEN it returns empty without network work`() = runTest {
        val result = episodesRepository.getEpisodes(episodeIds = emptyList())

        assertEquals(EpisodesResult.Success(episodes = emptyList()), result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `GIVEN malformed or unrelated episodes WHEN their response arrives THEN it reports invalid data`() = runTest {
        for (body in listOf("{broken", "[]", "null", """{"id":2,"name":"Pilot","episode":"S01E01","air_date":"December 2, 2013"}""")) {
            server.enqueue(MockResponse.Builder().body(body).build())

            val result = episodesRepository.getEpisodes(episodeIds = listOf(1))

            assertEquals(EpisodesResult.Failure(reason = CharacterRequestFailure.InvalidResponse), result)
        }
    }

    @Test
    fun `GIVEN a service failure WHEN episodes are requested THEN it returns a recoverable failure`() = runTest {
        server.enqueue(MockResponse.Builder().code(503).build())

        val result = episodesRepository.getEpisodes(episodeIds = listOf(1))

        assertEquals(EpisodesResult.Failure(reason = CharacterRequestFailure.Service), result)
    }

    @Test
    fun `GIVEN a connection failure WHEN episodes are requested THEN it reports a network failure`() = runTest {
        server.close()

        val result = episodesRepository.getEpisodes(episodeIds = listOf(1))

        assertEquals(EpisodesResult.Failure(reason = CharacterRequestFailure.Network), result)
    }
}
