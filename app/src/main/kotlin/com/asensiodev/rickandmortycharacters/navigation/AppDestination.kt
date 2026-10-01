package com.asensiodev.rickandmortycharacters.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

internal sealed interface AppDestination : NavKey {
    @Serializable
    data object Home : AppDestination

    @Serializable
    data class Detail(val characterId: Int) : AppDestination
}
