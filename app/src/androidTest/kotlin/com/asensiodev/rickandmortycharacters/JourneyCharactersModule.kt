@file:Suppress("MagicNumber")

package com.asensiodev.rickandmortycharacters

import com.asensiodev.rickandmortycharacters.data.characters.di.CharactersBindings
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CharactersBindings::class])
object JourneyCharactersModule {
    @Provides
    @Singleton
    fun fixture(): JourneyCharactersRepository = JourneyCharactersRepository()

    @Provides
    fun repository(fixture: JourneyCharactersRepository): CharactersRepository = fixture
}

class JourneyCharactersRepository : CharactersRepository {
    val requestedIds = mutableListOf<Int>()
    var pageRequests = 0
    var detailResult: CharacterDetailsResult? = null
    var detailGate: CompletableDeferred<Unit>? = null

    @Volatile
    var cancelledId: Int? = null

    override suspend fun getPage(page: Int): CharactersPageResult {
        pageRequests++
        return CharactersPageResult.Success(
            CharacterPage(
                (1..20).map {
                    CharacterSummary(it, "Character $it", "Human", CharacterStatus.Alive, null)
                },
                20,
                null,
            ),
        )
    }

    override suspend fun getDetails(characterId: Int): CharacterDetailsResult {
        requestedIds += characterId
        return try {
            detailGate?.await()
            detailResult ?: CharacterDetailsResult.Success(
                CharacterDetails(
                    characterId, "Character $characterId", CharacterStatus.Alive, "Human", "Male",
                    null, "Earth", "Earth", 3, null,
                ),
            )
        } catch (error: CancellationException) {
            cancelledId = characterId
            throw error
        }
    }
}
