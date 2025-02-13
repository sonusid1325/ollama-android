package com.sonusid.ollama.ui.screens.chats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonusid.ollama.R
import com.sonusid.ollama.db.entity.Chat
import com.sonusid.ollama.viewmodels.OllamaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chats(navController: NavController, viewModel: OllamaViewModel) {
    val allChats = viewModel.chats.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var chatTitle by remember { mutableStateOf("") }
    println(allChats.value)

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {

                    }) {
                        Icon(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = "logo",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text("Ollama", fontSize = 20.sp)
                    IconButton(onClick = {
                        navController.navigate("setting")
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.settings),
                            contentDescription = "settings",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .padding(20.dp)
                    .size(60.dp),
                onClick = { showDialog = true }) {
                Icon(
                    painterResource(R.drawable.add),
                    "add",
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    ) { paddingValues ->
        if (allChats.value.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(
                        R.drawable.logo
                    ), "logo", Modifier.size(150.dp)
                )
                Spacer(Modifier.height(60.dp))
                Text("Click on + to start a new chat")
            }
        } else {
            LazyColumn(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                items(allChats.value.size) { index ->
                    ElevatedButton(
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .size(40.dp),
                        onClick = {
                            navController.navigate("chat/${allChats.value[index].chatId}")
                        }) {
                        Row(Modifier.padding(10.dp)) { Text(allChats.value[index].title) }
                    }
                }
            }
        }
    }

    // Chat Name Input Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (chatTitle.isNotBlank()) {
                        viewModel.insertChat(chat = Chat(title = chatTitle))
                        chatTitle = ""
                        showDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("New Chat") },
            text = {
                OutlinedTextField(
                    value = chatTitle,
                    onValueChange = { chatTitle = it },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.logo), "logo", Modifier.size(25.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Chat Title")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { showDialog = false })
                )
            }
        )
    }
}

