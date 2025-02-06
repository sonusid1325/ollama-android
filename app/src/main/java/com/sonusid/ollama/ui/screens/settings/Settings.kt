package com.sonusid.ollama.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sonusid.ollama.R

@Composable
fun Settings() {
    var gateway by remember { mutableStateOf("https://localhost:12345") }
    Scaffold(
        topBar = {
            OutlinedTextField(
                value = gateway,
                onValueChange = { gateway = it },
                label = { "Server Address" },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                ),
                suffix = {
                    IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                        Icon(painter = painterResource(R.drawable.save), contentDescription = "Save Address")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    Settings()
}