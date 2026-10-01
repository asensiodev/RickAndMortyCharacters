package com.asensiodev.rickandmortycharacters.feature.home.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.asensiodev.rickandmortycharacters.feature.home.R
import com.asensiodev.rickandmortycharacters.feature.home.model.HomeSearchAction

@Composable
internal fun HomeSearchField(text: String, onAction: (HomeSearchAction) -> Unit, modifier: Modifier = Modifier) {
    var input by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text))
    }
    LaunchedEffect(text) {
        if (input.text != text) input = TextFieldValue(text, TextRange(text.length))
    }
    val description = stringResource(R.string.search_characters)
    TextField(
        value = input,
        onValueChange = {
            input = it
            if (it.text != text) onAction(HomeSearchAction.Edit(it.text))
        },
        modifier = modifier.fillMaxWidth().semantics { contentDescription = description },
        placeholder = { Text(stringResource(R.string.search_characters)) },
        leadingIcon = {
            Icon(painterResource(R.drawable.ic_search), null, Modifier.size(HomeLayoutTokens.searchIconSize))
        },
        trailingIcon = if (text.isNotEmpty()) {
            {
                IconButton(onClick = { onAction(HomeSearchAction.Clear) }) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        stringResource(R.string.clear_search),
                        Modifier.size(HomeLayoutTokens.searchIconSize),
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onAction(HomeSearchAction.Submit) }),
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}
