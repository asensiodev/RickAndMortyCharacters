package com.asensiodev.rickandmortycharacters.feature.home.model

data class HomePagingState(
    val totalCount: Int? = null,
    val searchText: String = "",
    val appliedName: String? = null,
    val generation: Long = 0,
)
