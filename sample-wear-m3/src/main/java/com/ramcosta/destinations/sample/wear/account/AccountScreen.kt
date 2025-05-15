package com.ramcosta.destinations.sample.wear.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel

@Destination<RootGraph>
@Composable
fun AccountScreen(
    vm: AccountViewModel = viewModel(),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { vm.onLogoutClick() }) {
            Text("Logout", modifier = Modifier.padding(6.dp))
        }
    }
}