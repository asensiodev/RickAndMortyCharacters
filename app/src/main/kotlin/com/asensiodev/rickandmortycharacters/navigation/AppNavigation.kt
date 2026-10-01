package com.asensiodev.rickandmortycharacters.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import com.asensiodev.rickandmortycharacters.feature.details.DetailsRoute
import com.asensiodev.rickandmortycharacters.feature.home.HomeRoute

@Composable
internal fun AppNavigation(imageLoader: ImageLoader, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(AppDestination.Home)
    val onBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<AppDestination.Home> {
                HomeRoute(imageLoader = imageLoader, onCharacterSelected = { id ->
                    if (backStack.lastOrNull() == AppDestination.Home) {
                        backStack.add(AppDestination.Detail(characterId = id))
                    }
                })
            }
            entry<AppDestination.Detail> { destination ->
                DetailsRoute(characterId = destination.characterId, imageLoader = imageLoader, onBack = {
                    if (backStack.lastOrNull() == destination) onBack()
                })
            }
        },
    )
}
