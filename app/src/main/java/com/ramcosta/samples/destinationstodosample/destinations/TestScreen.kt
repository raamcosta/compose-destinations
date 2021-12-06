package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.destinationstodosample.destinations.profile.Stuff
import com.ramcosta.samples.destinationstodosample.destinations.profile.Things

@Destination
@Composable
fun TestScreen(
    id: String,
    stuff1: Long = 1L,
    stuff2: Stuff?,
    stuff3: Things? = Things()
) {
    Text(
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center,
        text = "id = $id \n stuff1 = $stuff1 \n stuff2 = $stuff2 \n stuff3 = $stuff3"
    )
}