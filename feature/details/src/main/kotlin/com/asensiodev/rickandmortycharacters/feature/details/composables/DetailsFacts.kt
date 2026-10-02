package com.asensiodev.rickandmortycharacters.feature.details.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterDetails
import com.asensiodev.rickandmortycharacters.feature.details.R

@Composable
internal fun DetailsFacts(character: CharacterDetails, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = DetailsTokens.factsShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge),
        ) {
            Text(
                stringResource(R.string.character_specifications),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            character.type?.takeIf { it.isNotBlank() }?.let {
                DetailFact(label = R.string.type, icon = R.drawable.ic_fingerprint, value = displayFact(value = it))
            }
            DetailFact(label = R.string.gender, icon = R.drawable.ic_wc, value = displayFact(value = character.gender))
            DetailFact(
                label = R.string.origin,
                icon = R.drawable.ic_public,
                value = displayFact(value = character.origin),
            )
            DetailFact(
                label = R.string.last_location,
                icon = R.drawable.ic_location_on,
                value = displayFact(value = character.location),
            )
            Row(
                Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(Spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FactLabel(
                    label = R.string.episode_appearances,
                    icon = R.drawable.ic_movie,
                    modifier = Modifier.weight(DetailsTokens.FACT_VALUE_WEIGHT),
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Text(
                        character.episodeCount.toString(),
                        Modifier.padding(horizontal = Spacing.small, vertical = Spacing.tiny),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailFact(label: Int, icon: Int, value: String) {
    Row(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
        verticalAlignment = Alignment.Top,
    ) {
        FactLabel(label = label, icon = icon, modifier = Modifier.weight(DetailsTokens.FACT_VALUE_WEIGHT))
        Text(
            value,
            Modifier.weight(DetailsTokens.FACT_VALUE_WEIGHT),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun FactLabel(label: Int, icon: Int, modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DetailsTokens.iconSize),
        )
        Text(
            stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun displayFact(value: String): String =
    if (value.equals("unknown", ignoreCase = true)) stringResource(R.string.unknown) else value
