package com.asensiodev.rickandmortycharacters.domain.characters.model

data class CharacterPage(val characters: List<CharacterSummary>, val totalCount: Int, val nextPage: Int?)
