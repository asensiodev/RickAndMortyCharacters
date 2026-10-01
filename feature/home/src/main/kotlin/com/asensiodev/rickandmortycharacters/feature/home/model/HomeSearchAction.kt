package com.asensiodev.rickandmortycharacters.feature.home.model

import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus

sealed interface HomeSearchAction {
    data class SelectStatus(val status: CharacterStatus?) : HomeSearchAction

    data class Edit(val name: String) : HomeSearchAction

    data object Submit : HomeSearchAction

    data object Clear : HomeSearchAction

    data class Suggest(val name: String) : HomeSearchAction
}
