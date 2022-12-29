package com.ramcosta.destinations.sample.wear.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel

@Destination
@Composable
fun AccountScreen(
    vm: AccountViewModel = viewModel(),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { vm.onLogoutClick() }) {
            Icon(Icons.Default.Close, contentDescription = "Logout")
        }
    }
}