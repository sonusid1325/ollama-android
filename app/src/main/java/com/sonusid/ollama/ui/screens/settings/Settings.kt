package com.sonusid.ollama.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sonusid.ollama.R

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Composable
fun Settings() {
    val context = LocalContext.current
    var gateway by remember { mutableStateOf("https://localhost:11434") }
    var valid by remember { mutableStateOf(true) }
    var social = listOf<SettingsData>(
        SettingsData(url = "https://github.com/sonusid1325", name = "GitHub", R.drawable.github),
        SettingsData(
            url = "https://github.com/sponsors/sonusid1325",
            name = "GitHub Sponsor",
            R.drawable.sponsor
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
                placeholder = { Text("https://localhost:11434") },
                label = { Text("Server") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .padding(top = 20.dp),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (valid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
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
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
            ) { Text("Ollama v1.0.0") }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(social.size) { index ->
                val value = social[index]
                ElevatedButton(
                    onClick = {
                        openUrl(context, value.url)
                    }, modifier = Modifier
                        .padding(10.dp)
//                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Icon(
                            painterResource(value.logo),
                            contentDescription = value.name,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(20.dp))
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