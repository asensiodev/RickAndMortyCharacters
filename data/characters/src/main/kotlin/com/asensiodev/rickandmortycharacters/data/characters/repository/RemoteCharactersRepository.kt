package com.asensiodev.rickandmortycharacters.data.characters.repository

import com.asensiodev.rickandmortycharacters.data.characters.remote.CharacterDetailsDto
import com.asensiodev.rickandmortycharacters.data.characters.remote.CharacterErrorDto
import com.asensiodev.rickandmortycharacters.data.characters.remote.CharactersApi
import com.asensiodev.rickandmortycharacters.data.characters.remote.CharactersJson
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterPage
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterSummary
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterDetailsResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersPageResult
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharactersRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Response

private const val HTTP_NOT_FOUND = 404

internal class RemoteCharactersRepository @Inject constructor(private val api: CharactersApi) :
    CharactersRepository {
    override suspend fun getDetails(characterId: Int): CharacterDetailsResult = try {
        val response = api.getDetails(characterId = characterId)
        if (!response.isSuccessful) {
            if (response.hasApiError(expectedMessage = "Character not found")) {
                CharacterDetailsResult.NotFound
            } else {
                CharacterDetailsResult.Failure(reason = CharacterRequestFailure.Service)
            }
        } else {
            response.body()?.toResult(requestedId = characterId)
                ?: CharacterDetailsResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
        }
    } catch (_: SerializationException) {
        CharacterDetailsResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
    } catch (_: IOException) {
        CharacterDetailsResult.Failure(reason = CharacterRequestFailure.Network)
    }

    override suspend fun getPage(page: Int, name: String?, status: CharacterStatus?): CharactersPageResult = try {
        val requestedName = name?.trim()?.takeIf { it.isNotEmpty() }
        val requestedStatus = status.toApiValue()
        val hasConstraints = requestedName != null || status != null
        val response = api.getPage(page = page, name = requestedName, status = requestedStatus)
        if (!response.isSuccessful) {
            if ((page > 1 || hasConstraints) && response.hasApiError(expectedMessage = "There is nothing here")) {
                if (page > 1) {
                    CharactersPageResult.EndOfCatalogue
                } else {
                    CharactersPageResult.Success(
                        page = CharacterPage(characters = emptyList(), totalCount = 0, nextPage = null),
                    )
                }
            } else {
                response.errorBody()?.close()
                CharactersPageResult.Failure(reason = CharacterRequestFailure.Service)
            }
        } else {
            val body = response.body()
            if (body == null) {
                CharactersPageResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
            } else {
                val nextPage = body.info.next?.toHttpUrlOrNull()?.queryParameter(
                    "page",
                )?.toIntOrNull()
                CharactersPageResult.Success(
                    page = CharacterPage(
                        characters = body.results.map { character ->
                            CharacterSummary(
                                id = character.id,
                                name = character.name,
                                species = character.species,
                                status = when (character.status) {
                                    "Alive" -> CharacterStatus.Alive
                                    "Dead" -> CharacterStatus.Dead
                                    else -> CharacterStatus.Unknown
                                },
                                imageUrl = character.image,
                            )
                        },
                        totalCount = body.info.count,
                        nextPage = nextPage,
                    ),
                )
            }
        }
    } catch (_: SerializationException) {
        CharactersPageResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
    } catch (_: IOException) {
        CharactersPageResult.Failure(reason = CharacterRequestFailure.Network)
    }
}

private fun Response<*>.hasApiError(expectedMessage: String): Boolean = errorBody()?.use { errorBody ->
    if (code() != HTTP_NOT_FOUND) return@use false
    try {
        CharactersJson.decodeFromString<CharacterErrorDto>(errorBody.string()).error == expectedMessage
    } catch (_: SerializationException) {
        false
    }
} ?: false

private fun CharacterDetailsDto.toResult(requestedId: Int): CharacterDetailsResult {
    if (id != requestedId || id <= 0 || name.isBlank()) {
        return CharacterDetailsResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
    }
    return CharacterDetailsResult.Success(
        character = CharacterDetails(
            id = id,
            name = name,
            status = when (status) {
                "Alive" -> CharacterStatus.Alive
                "Dead" -> CharacterStatus.Dead
                else -> CharacterStatus.Unknown
            },
            species = species,
            gender = gender,
            type = type?.takeIf { it.isNotBlank() },
            origin = origin.name,
            location = location.name,
            episodeCount = episode.size,
            episodeIds = episode.mapNotNull {
                it.toHttpUrlOrNull()?.pathSegments?.lastOrNull()?.toIntOrNull()?.takeIf { id ->
                    id >
                        0
                }
            }.distinct(),
            imageUrl = image?.takeIf { it.isNotBlank() },
        ),
    )
}

private fun CharacterStatus?.toApiValue(): String? = when (this) {
    CharacterStatus.Alive -> "alive"
    CharacterStatus.Dead -> "dead"
    CharacterStatus.Unknown -> "unknown"
    null -> null
}
