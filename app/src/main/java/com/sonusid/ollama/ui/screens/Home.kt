package com.sonusid.ollama.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sonusid.ollama.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navHostController: NavHostController) {
    var userPrompt by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "logo",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text("llama3.2", fontSize = 20.sp)
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.settings),
                                contentDescription = "settings",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .padding(bottom = 20.dp),
                placeholder = { Text("Enter your prompt...", fontSize = 15.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("Null")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val dummyHost = rememberNavController()
    MaterialTheme(colorScheme = darkColorScheme()) {
        Home(dummyHost)
    }
}
