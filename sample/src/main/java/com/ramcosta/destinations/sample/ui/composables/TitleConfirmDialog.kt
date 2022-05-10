package com.ramcosta.destinations.sample.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TitleConfirmDialog(
    type: String,
    title: String,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val inputService = LocalTextInputService.current
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Absolute.spacedBy(8.dp)
    ) {
        Text("Add a new $type:")

        OutlinedTextField(
            placeholder = { Text("${type.replaceFirstChar { it.uppercase() }} title") },
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.focusRequester(focusRequester)
        )

        Button(
            onClick = onConfirm
        ) {
            Text("Confirm")
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        inputService?.showSoftwareKeyboard()
        focusRequester.requestFocus()
    }
}