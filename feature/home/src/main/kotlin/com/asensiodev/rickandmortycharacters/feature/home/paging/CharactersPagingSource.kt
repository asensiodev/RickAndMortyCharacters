package com.asensiodev.rickandmortycharacters.feature.home.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository

internal class CharactersPagingSource(
    private val repository: CharactersRepository,
    private val name: String? = null,
    private val generation: Long = 0,
    private val status: CharacterStatus? = null,
    private val onTotalCount: (Int) -> Unit,
) : PagingSource<Int, CharacterSummary>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterSummary> {
        val page = params.key ?: 1
        return when (val result = repository.getPage(page, name, status)) {
            is CharactersPageResult.Success -> {
                if (page == 1) onTotalCount(result.page.totalCount)
                LoadResult.Page(
                    data = result.page.characters,
                    prevKey = null,
                    nextKey = result.page.nextPage,
                )
            }

            CharactersPageResult.EndOfCatalogue -> LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null,
            )

            is CharactersPageResult.Failure -> LoadResult.Error(
                CharactersPagingException(result.reason, generation),
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CharacterSummary>): Int? = null
}
