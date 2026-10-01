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
        val response = api.getDetails(characterId)
        if (!response.isSuccessful) {
            if (isMissingCharacter(response)) {
                CharacterDetailsResult.NotFound
            } else {
                CharacterDetailsResult.Failure(CharacterRequestFailure.Service)
            }
        } else {
            response.body()?.toResult(characterId)
                ?: CharacterDetailsResult.Failure(CharacterRequestFailure.InvalidResponse)
        }
    } catch (_: SerializationException) {
        CharacterDetailsResult.Failure(CharacterRequestFailure.InvalidResponse)
    } catch (_: IOException) {
        CharacterDetailsResult.Failure(CharacterRequestFailure.Network)
    }

    override suspend fun getPage(page: Int): CharactersPageResult = try {
        val response = api.getPage(page)
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            CharactersPageResult.Failure(CharacterRequestFailure.Service)
        } else {
            val body = response.body()
            if (body == null) {
                CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse)
            } else {
                val nextPage = body.info.next?.toHttpUrlOrNull()?.queryParameter(
                    "page",
                )?.toIntOrNull()
                CharactersPageResult.Success(
                    CharacterPage(
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
        CharactersPageResult.Failure(CharacterRequestFailure.InvalidResponse)
    } catch (_: IOException) {
        CharactersPageResult.Failure(CharacterRequestFailure.Network)
    }
}

private fun isMissingCharacter(response: Response<*>): Boolean =
    response.errorBody()?.use { errorBody ->
        if (response.code() != HTTP_NOT_FOUND) return@use false
        try {
            CharactersJson.decodeFromString<CharacterErrorDto>(errorBody.string()).error ==
                "Character not found"
        } catch (_: SerializationException) {
            false
        }
    } ?: false

private fun CharacterDetailsDto.toResult(requestedId: Int): CharacterDetailsResult {
    if (id != requestedId || id <= 0 || name.isBlank()) {
        return CharacterDetailsResult.Failure(CharacterRequestFailure.InvalidResponse)
    }
    return CharacterDetailsResult.Success(
        CharacterDetails(
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
            imageUrl = image?.takeIf { it.isNotBlank() },
        ),
    )
}
