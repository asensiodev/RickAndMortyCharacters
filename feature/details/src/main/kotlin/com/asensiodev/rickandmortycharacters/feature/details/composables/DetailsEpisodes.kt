package com.asensiodev.rickandmortycharacters.feature.details.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.asensiodev.rickandmortycharacters.core.designsystem.composables.LoadingPlaceholder
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.domain.characters.model.Episode
import com.asensiodev.rickandmortycharacters.feature.details.R
import com.asensiodev.rickandmortycharacters.feature.details.model.EpisodesUiState

@Composable
internal fun DetailsEpisodes(state: EpisodesUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Text(
            text = stringResource(R.string.episodes_heading),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        when (state) {
            EpisodesUiState.Loading -> {
                val description = stringResource(R.string.episodes_loading)
                LoadingPlaceholder(
                    modifier = Modifier.fillMaxWidth().heightIn(min = DetailsTokens.episodeCardMinHeight)
                        .clip(DetailsTokens.factsShape).progressSemantics()
                        .semantics { contentDescription = description },
                )
            }

            is EpisodesUiState.Content -> {
                val description = stringResource(R.string.episodes_list)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = description },
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    items(items = state.episodes, key = { it.id }, contentType = { "episode" }) { episode ->
                        EpisodeCard(episode = episode)
                    }
                }
            }

            EpisodesUiState.Empty -> Text(
                text = stringResource(R.string.episodes_empty),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            EpisodesUiState.Error -> {
                Text(
                    text = stringResource(R.string.episodes_error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) { Text(text = stringResource(R.string.retry_episodes)) }
            }
        }
    }
}

@Composable
private fun EpisodeCard(episode: Episode) {
    Surface(
        modifier = Modifier.width(DetailsTokens.episodeCardWidth).heightIn(min = DetailsTokens.episodeCardMinHeight)
            .semantics(mergeDescendants = true) {},
        shape = DetailsTokens.factsShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Text(
                text = episode.code,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(text = episode.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = episode.airDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
