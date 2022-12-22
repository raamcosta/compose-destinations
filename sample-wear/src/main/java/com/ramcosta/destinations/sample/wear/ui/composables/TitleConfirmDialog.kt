package com.ramcosta.destinations.sample.wear.ui.composables

import android.app.RemoteInput
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.input.RemoteInputIntentHelper

@Composable
fun TitleConfirmDialog(
    type: String,
    title: String,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(showDialog = true, onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            var textForUserInput by remember { mutableStateOf("") }
            val inputTextKey = "input_text"

            val launcher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    it.data?.let { data ->
                        val results: Bundle = RemoteInput.getResultsFromIntent(data)
                        val newInputText: CharSequence? = results.getCharSequence(inputTextKey)
                        textForUserInput = newInputText as String
                    }
                }

            val intent = remember {
                RemoteInputIntentHelper.createActionRemoteInputIntent().apply {
                    RemoteInputIntentHelper.putRemoteInputsExtra(
                        this, listOf(
                            RemoteInput.Builder(inputTextKey)
                                .setLabel("${type.replaceFirstChar { it.uppercase() }} title")
                                .build()
                        )
                    )
                }
            }

            Text("Add a new $type:")

            Text(
                modifier = Modifier.fillMaxWidth(0.8f).border(1.dp, Color.White).clickable {
                    launcher.launch(intent)
                },
                text = textForUserInput,
            )

            Chip(
                onClick = {
                    onTitleChange(textForUserInput)
                    onConfirm()
                },
                label = {
                    Text("Confirm")
                }
            )
        }
    }
}