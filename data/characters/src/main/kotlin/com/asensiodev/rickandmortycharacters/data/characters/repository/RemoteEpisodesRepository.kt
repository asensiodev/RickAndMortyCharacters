package com.asensiodev.rickandmortycharacters.data.characters.repository

import com.asensiodev.rickandmortycharacters.data.characters.remote.CharactersApi
import com.asensiodev.rickandmortycharacters.data.characters.remote.CharactersJson
import com.asensiodev.rickandmortycharacters.data.characters.remote.EpisodeDto
import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode
import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesRepository
import com.asensiodev.rickandmortycharacters.domain.characters.repository.EpisodesResult
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

internal class RemoteEpisodesRepository @Inject constructor(private val api: CharactersApi) : EpisodesRepository {
    override suspend fun getEpisodes(episodeIds: List<Int>): EpisodesResult {
        val ids = episodeIds.distinct()
        return when {
            ids.isEmpty() -> EpisodesResult.Success(episodes = emptyList())

            ids.any { it <= 0 } -> EpisodesResult.Failure(reason = CharacterRequestFailure.InvalidResponse)

            else -> try {
                val response = api.getEpisodes(episodeIds = ids.joinToString(","))
                if (!response.isSuccessful) {
                    response.errorBody()?.close()
                    EpisodesResult.Failure(reason = CharacterRequestFailure.Service)
                } else {
                    response.body()?.toEpisodesResult(ids = ids)
                        ?: EpisodesResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
                }
            } catch (_: SerializationException) {
                EpisodesResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
            } catch (_: IOException) {
                EpisodesResult.Failure(reason = CharacterRequestFailure.Network)
            }
        }
    }
}

private fun JsonElement.toEpisodesResult(ids: List<Int>): EpisodesResult {
    val entries = if (this is JsonArray) toList() else listOf(this)
    val episodes = entries.map { CharactersJson.decodeFromJsonElement<EpisodeDto>(it) }
    val hasMissingFacts = episodes.any { it.name.isBlank() || it.episode.isBlank() || it.airDate.isBlank() }
    if (episodes.size != ids.size || episodes.map { it.id }.toSet() != ids.toSet() || hasMissingFacts) {
        return EpisodesResult.Failure(reason = CharacterRequestFailure.InvalidResponse)
    }
    val byId = episodes.associateBy { it.id }
    return EpisodesResult.Success(
        episodes = ids.map { id ->
            val episode = byId.getValue(id)
            Episode(id = episode.id, name = episode.name, code = episode.episode, airDate = episode.airDate)
        },
    )
}
