package com.asensiodev.rickandmortycharacters.data.characters.repository

import com.asensiodev.rickandmortycharacters.data.characters.remote.CharacterDetailsDto
import com.asensiodev.rickandmortycharacters.data.characters.remote.CharacterErrorDto
import com.asensiodev.rickandmortycharacters.data.characters.remote.CharacterPageDto
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
import kotlinx.coroutines.delay
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Response

private const val HTTP_NOT_FOUND = 404
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val MAX_RETRY_WAIT_SECONDS = 60L
private const val MILLIS_PER_SECOND = 1_000L

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
        val response = api.getPageWithRateLimitRetry(page = page, name = requestedName, status = requestedStatus)
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
            response.body()?.toResult(requestedPage = page)
                ?: CharactersPageResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
        }
    } catch (_: SerializationException) {
        CharactersPageResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
    } catch (_: IOException) {
        CharactersPageResult.Failure(reason = CharacterRequestFailure.Network)
    }
}

private suspend fun CharactersApi.getPageWithRateLimitRetry(
    page: Int,
    name: String?,
    status: String?,
): Response<CharacterPageDto> {
    val response = getPage(page = page, name = name, status = status)
    val waitSeconds = response.headers()["Retry-After"]?.trim()?.toLongOrNull()
    if (response.code() != HTTP_TOO_MANY_REQUESTS || waitSeconds == null || waitSeconds !in 0..MAX_RETRY_WAIT_SECONDS) {
        return response
    }
    response.errorBody()?.close()
    delay((waitSeconds + 1) * MILLIS_PER_SECOND)
    return getPage(page = page, name = name, status = status)
}

private fun CharacterPageDto.toResult(requestedPage: Int): CharactersPageResult {
    val nextPage = info.next?.toHttpUrlOrNull()?.queryParameter("page")?.toIntOrNull()
    val hasInvalidNextPage = info.next != null && (nextPage == null || nextPage <= requestedPage)
    val hasInvalidCount = info.count < results.size ||
        (requestedPage == 1 && results.isEmpty() && info.count != 0)
    val hasInvalidCharacters = results.any { it.id <= 0 || it.name.isBlank() } ||
        results.map { it.id }.distinct().size != results.size
    if (hasInvalidNextPage || hasInvalidCount || hasInvalidCharacters) {
        return CharactersPageResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
    }
    return CharactersPageResult.Success(
        page = CharacterPage(
            characters = results.map { character ->
                CharacterSummary(
                    id = character.id,
                    name = character.name,
                    species = character.species,
                    status = character.status.toCharacterStatus(),
                    imageUrl = character.image,
                )
            },
            totalCount = info.count,
            nextPage = nextPage,
        ),
    )
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
            status = status.toCharacterStatus(),
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

private fun String.toCharacterStatus(): CharacterStatus = when (this) {
    "Alive" -> CharacterStatus.Alive
    "Dead" -> CharacterStatus.Dead
    else -> CharacterStatus.Unknown
}

private fun CharacterStatus?.toApiValue(): String? = when (this) {
    CharacterStatus.Alive -> "alive"
    CharacterStatus.Dead -> "dead"
    CharacterStatus.Unknown -> "unknown"
    null -> null
}
