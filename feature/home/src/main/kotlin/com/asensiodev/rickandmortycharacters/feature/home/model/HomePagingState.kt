package com.asensiodev.rickandmortycharacters.feature.home.model

import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus

data class HomePagingState(
    val totalCount: Int? = null,
    val searchText: String = "",
    val appliedName: String? = null,
    val selectedStatus: CharacterStatus? = null,
    val generation: Long = 0,
)
