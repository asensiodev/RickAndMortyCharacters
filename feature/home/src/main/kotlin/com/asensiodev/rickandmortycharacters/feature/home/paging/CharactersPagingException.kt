package com.asensiodev.rickandmortycharacters.feature.home.paging

import com.asensiodev.rickandmortycharacters.domain.characters.repository.CharacterRequestFailure

internal class CharactersPagingException(reason: CharacterRequestFailure, val generation: Long) :
    IllegalStateException(reason.name)
