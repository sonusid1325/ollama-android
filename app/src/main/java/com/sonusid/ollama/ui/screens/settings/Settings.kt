package com.sonusid.ollama.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sonusid.ollama.R

@Composable
fun Settings() {
    var gateway by remember { mutableStateOf("https://localhost:12345") }
    var valid by remember { mutableStateOf(true) }
    var social = listOf<SettingsData>(
        SettingsData(url = "https//github.com/sonusid1325", name = "GitHub", R.drawable.github),
        SettingsData(
            url = "https://github.com/sponsors/sonusid1325",
            name = "GitHub Sponsor",
            R.drawable.source
        ),
        SettingsData(
            url = "https://buymeacoffee.com/sonusid1325",
            name = "Buy me Coffee",
            R.drawable.coffee
        ),
    )
    Scaffold(
        topBar = {
            OutlinedTextField(
                value = gateway,
                onValueChange = { gateway = it },
                placeholder = { Text("https://localhost:12345") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (valid) Color.Green else Color.Red,
                    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                ),
                suffix = {
                    IconButton(onClick = {}, modifier = Modifier.size(25.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.save),
                            contentDescription = "Save Address"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(social.size) { index ->
                val value = social[index]
                ElevatedButton(onClick = {}, modifier = Modifier.padding(10.dp).padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painterResource(value.logo),
                            contentDescription = value.name,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(value.name)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Settings()
    }
}