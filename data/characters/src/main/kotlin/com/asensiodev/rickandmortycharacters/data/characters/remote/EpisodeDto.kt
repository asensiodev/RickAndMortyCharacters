package com.asensiodev.rickandmortycharacters.data.characters.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EpisodeDto(
    val id: Int,
    val name: String,
    val episode: String,
    @SerialName("air_date") val airDate: String,
)
