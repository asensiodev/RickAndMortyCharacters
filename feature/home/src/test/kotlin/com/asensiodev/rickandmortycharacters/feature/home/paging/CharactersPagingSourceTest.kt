@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters.feature.home.paging

import androidx.paging.PagingSource
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CharactersPagingSourceTest {
    private val character = CharacterSummary(1, "Rick", "Human", CharacterStatus.Alive, null)
    private val fakeCharactersRepository = PagingRepository(
        CharactersPageResult.Success(CharacterPage(listOf(character), 57, 2)),
    )
    private var totalCount: Int? = null

    private lateinit var charactersPagingSource: CharactersPagingSource

    @Before
    fun setUp() {
        charactersPagingSource =
            CharactersPagingSource(fakeCharactersRepository) { totalCount = it }
    }

    @Test
    fun `GIVEN a confirmed append end WHEN loading it THEN pagination stops without replacing the total`() = runTest {
        charactersPagingSource.load(
            PagingSource.LoadParams.Refresh(null, 20, false),
        )
        fakeCharactersRepository.result = CharactersPageResult.EndOfCatalogue

        val result = charactersPagingSource.load(PagingSource.LoadParams.Append(2, 20, false))

        assertEquals(
            PagingSource.LoadResult.Page<Int, CharacterSummary>(emptyList(), null, null),
            result,
        )
        assertEquals(57, totalCount)
    }

    @Test
    fun `GIVEN an append failure WHEN loading the next page THEN it returns an error and preserves the total`() = runTest {
        charactersPagingSource.load(PagingSource.LoadParams.Refresh(null, 20, false))
        fakeCharactersRepository.result =
            CharactersPageResult.Failure(CharacterRequestFailure.Network)

        val result = charactersPagingSource.load(PagingSource.LoadParams.Append(2, 20, false))

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(57, totalCount)
    }

    @Test
    fun `GIVEN the final append WHEN loading succeeds THEN it has no next key and retains the first page total`() = runTest {
        charactersPagingSource.load(PagingSource.LoadParams.Refresh(null, 20, false))
        fakeCharactersRepository.result =
            CharactersPageResult.Success(CharacterPage(listOf(character), 58, null))

        val result = charactersPagingSource.load(PagingSource.LoadParams.Append(2, 20, false))

        assertEquals(
            PagingSource.LoadResult.Page<Int, CharacterSummary>(listOf(character), null, null),
            result,
        )
        assertEquals(57, totalCount)
    }

    @Test(expected = CancellationException::class)
    fun `GIVEN a cancelled repository request WHEN loading a page THEN cancellation propagates to the caller`() = runTest {
        fakeCharactersRepository.cancelled = true

        charactersPagingSource.load(PagingSource.LoadParams.Refresh(null, 20, false))
    }

    @Test
    fun `GIVEN the first page WHEN loading succeeds THEN it returns characters next key and API total`() = runTest {
        val result = charactersPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        )

        assertEquals(
            PagingSource.LoadResult.Page<Int, CharacterSummary>(listOf(character), null, 2),
            result,
        )
        assertEquals(57, totalCount)
        assertEquals(listOf(1), fakeCharactersRepository.requestedPages)
    }
}

private class PagingRepository(var result: CharactersPageResult) : CharactersRepository {
    val requestedPages = mutableListOf<Int>()
    var cancelled = false

    override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
        if (cancelled) throw CancellationException()
        requestedPages += page
        return result
    }

    override suspend fun getDetails(characterId: Int): CharacterDetailsResult = CharacterDetailsResult.NotFound
}
