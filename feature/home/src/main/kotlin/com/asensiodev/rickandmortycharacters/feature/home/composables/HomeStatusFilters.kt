package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.Spacing
import com.asensiodev.rickandmortycharacters.core.designsystem.theme.StatusColors
import com.asensiodev.rickandmortycharacters.domain.characters.model.CharacterStatus
import com.asensiodev.rickandmortycharacters.feature.home.R

@Composable
internal fun HomeStatusFilters(
    selectedStatus: CharacterStatus?,
    onSelect: (CharacterStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statuses = listOf(null, CharacterStatus.Alive, CharacterStatus.Dead, CharacterStatus.Unknown)
    val labels = listOf(
        stringResource(R.string.status_all),
        stringResource(R.string.status_alive),
        stringResource(R.string.status_dead),
        stringResource(R.string.status_unknown),
    )
    val textStyle = MaterialTheme.typography.labelLarge
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        statuses.forEachIndexed { index, status ->
            FilterChip(
                selected = status == selectedStatus,
                onClick = { onSelect(status) },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusFilterIndicator(status, status == selectedStatus)
                        Text(
                            labels[index],
                            style = textStyle,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                },
                modifier = Modifier.heightIn(min = HomeLayoutTokens.filterMinHeight),
                shape = MaterialTheme.shapes.medium,
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun StatusFilterIndicator(status: CharacterStatus?, selected: Boolean) {
    if (selected) {
        Icon(
            painterResource(R.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier.size(HomeLayoutTokens.filterCheckSize),
            tint = MaterialTheme.colorScheme.primary,
        )
    } else if (status != null) {
        val color = when (status) {
            CharacterStatus.Alive -> StatusColors.onAliveContainer
            CharacterStatus.Dead -> StatusColors.onDeadContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Box(Modifier.size(CharacterCardTokens.statusDotSize).background(color, CircleShape))
    }
}
