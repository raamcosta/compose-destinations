package com.ramcosta.destinations.sample.account

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.destinations.sample.core.viewmodel.viewModel

@Destination
@Composable
fun AccountScreen(
    vm: AccountViewModel = viewModel(),
) {

    Button(onClick = { vm.onLogoutClick() }) {
        Text("Logout")
    }

}