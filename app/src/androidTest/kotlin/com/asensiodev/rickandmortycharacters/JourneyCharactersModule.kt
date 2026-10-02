@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters

import com.asensiodev.rickandmortycharacters.data.characters.di.CharactersBindings
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesResult
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CharactersBindings::class])
object JourneyCharactersModule {
    @Provides
    fun episodesRepository(fixture: JourneyCharactersRepository): EpisodesRepository = fixture

    @Provides
    fun repository(fixture: JourneyCharactersRepository): CharactersRepository = fixture
}

class JourneyCharactersRepository :
    CharactersRepository,
    EpisodesRepository {
    val requestedIds = mutableListOf<Int>()
    var pageRequests = 0
    val requestedPages = mutableListOf<Int>()
    val requestedStatuses = mutableListOf<CharacterStatus?>()
    val requestedNames = mutableListOf<String?>()
    var pageCount = 1
    var detailResult: CharacterDetailsResult? = null
    var detailGate: CompletableDeferred<Unit>? = null

    @Volatile
    var cancelledId: Int? = null

    override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult {
        pageRequests++
        requestedPages += page
        requestedNames += name
        requestedStatuses += status
        return CharactersPageResult.Success(
            page = CharacterPage(
                characters = ((page - 1) * 20 + 1..page * 20).map {
                    CharacterSummary(
                        id = it,
                        name = if (name ==
                            null
                        ) {
                            "Character $it"
                        } else {
                            "$name $it"
                        },
                        species = "Human",
                        status = status ?: CharacterStatus.Alive,
                        imageUrl = null,
                    )
                },
                totalCount = pageCount * 20,
                nextPage = if (page < pageCount) page + 1 else null,
            ),
        )
    }

    override suspend fun getEpisodes(episodeIds: List<Int>): EpisodesResult = EpisodesResult.Success(
        episodes = listOf(
            Episode(id = 1, name = "Pilot", code = "S01E01", airDate = "December 2, 2013"),
            Episode(id = 2, name = "Lawnmower Dog", code = "S01E02", airDate = "December 9, 2013"),
            Episode(id = 3, name = "Anatomy Park", code = "S01E03", airDate = "December 16, 2013"),
        ).filter { it.id in episodeIds },
    )

    override suspend fun getDetails(characterId: Int): CharacterDetailsResult {
        requestedIds += characterId
        return try {
            detailGate?.await()
            detailResult ?: CharacterDetailsResult.Success(
                character = CharacterDetails(
                    id = characterId, name = "Character $characterId", status = CharacterStatus.Alive, species = "Human", gender = "Male",
                    type = null, origin = "Earth", location = "Earth", episodeCount = 3, imageUrl = null, episodeIds = listOf(1, 2, 3),
                ),
            )
        } catch (error: CancellationException) {
            cancelledId = characterId
            throw error
        }
    }
}
