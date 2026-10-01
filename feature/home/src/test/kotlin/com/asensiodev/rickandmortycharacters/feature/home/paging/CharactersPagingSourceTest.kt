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
    private val character = CharacterSummary(id = 1, name = "Rick", species = "Human", status = CharacterStatus.Alive, imageUrl = null)
    private val fakeCharactersRepository = PagingRepository(
        result = CharactersPageResult.Success(page = CharacterPage(characters = listOf(character), totalCount = 57, nextPage = 2)),
    )
    private var totalCount: Int? = null

    private lateinit var charactersPagingSource: CharactersPagingSource

    @Before
    fun setUp() {
        charactersPagingSource =
            CharactersPagingSource(repository = fakeCharactersRepository) { totalCount = it }
    }

    @Test
    fun `GIVEN a confirmed append end WHEN loading it THEN pagination stops without replacing the total`() = runTest {
        charactersPagingSource.load(
            params = PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )
        fakeCharactersRepository.result = CharactersPageResult.EndOfCatalogue

        val result = charactersPagingSource.load(
            params = PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false),
        )

        assertEquals(
            PagingSource.LoadResult.Page<Int, CharacterSummary>(data = emptyList(), prevKey = null, nextKey = null),
            result,
        )
        assertEquals(57, totalCount)
    }

    @Test
    fun `GIVEN an append failure WHEN loading the next page THEN it returns an error and preserves the total`() = runTest {
        charactersPagingSource.load(params = PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        fakeCharactersRepository.result =
            CharactersPageResult.Failure(reason = CharacterRequestFailure.Network)

        val result = charactersPagingSource.load(
            params = PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false),
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(57, totalCount)
    }

    @Test
    fun `GIVEN the final append WHEN loading succeeds THEN it has no next key and retains the first page total`() = runTest {
        charactersPagingSource.load(params = PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        fakeCharactersRepository.result =
            CharactersPageResult.Success(page = CharacterPage(characters = listOf(character), totalCount = 58, nextPage = null))

        val result = charactersPagingSource.load(
            params = PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false),
        )

        assertEquals(
            PagingSource.LoadResult.Page<Int, CharacterSummary>(data = listOf(character), prevKey = null, nextKey = null),
            result,
        )
        assertEquals(57, totalCount)
    }

    @Test(expected = CancellationException::class)
    fun `GIVEN a cancelled repository request WHEN loading a page THEN cancellation propagates to the caller`() = runTest {
        fakeCharactersRepository.cancelled = true

        charactersPagingSource.load(params = PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
    }

    @Test
    fun `GIVEN the first page WHEN loading succeeds THEN it returns characters next key and API total`() = runTest {
        val result = charactersPagingSource.load(
            params = PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        )

        assertEquals(
            PagingSource.LoadResult.Page<Int, CharacterSummary>(data = listOf(character), prevKey = null, nextKey = 2),
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
