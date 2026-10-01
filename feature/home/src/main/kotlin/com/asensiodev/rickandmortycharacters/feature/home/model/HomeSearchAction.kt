package com.asensiodev.rickandmortycharacters.feature.home.model

sealed interface HomeSearchAction {
    data class Edit(val name: String) : HomeSearchAction

    data object Submit : HomeSearchAction

    data object Clear : HomeSearchAction

    data class Suggest(val name: String) : HomeSearchAction
}
