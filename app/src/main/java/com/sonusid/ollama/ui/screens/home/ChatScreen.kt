package com.sonusid.ollama.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sonusid.ollama.R
import com.sonusid.ollama.UiState
import com.sonusid.ollama.db.entity.Message
import com.sonusid.ollama.viewmodels.OllamaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navHostController: NavHostController, viewModel: OllamaViewModel, chatId: Int = viewModel.chats.value.last().chatId+1) {

    val uiState by viewModel.uiState.collectAsState()
    var userPrompt: String by remember { mutableStateOf("") }
    remember { mutableStateListOf<String>() }
    var prompt: String by remember { mutableStateOf("") }
    val allChats = viewModel.allMessages(chatId).collectAsState(initial = emptyList())
    var isEnabled by remember { mutableStateOf(true) }
    var isLaunched by remember { mutableStateOf(true) }
    var toggle by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    LaunchedEffect(allChats.value.size) {
        if (allChats.value.isNotEmpty()) {
            listState.animateScrollToItem(allChats.value.size - 1)
        }
    }

    LaunchedEffect(uiState) {
        if (toggle) {
            when (uiState) {
                is UiState.Success -> {
                    val response = (uiState as UiState.Success).outputText
                    viewModel.insert(Message(message = response, chatId = chatId))
                }

                is UiState.Error -> {
                    viewModel.insert(
                        Message(
                            message = (uiState as UiState.Error).errorMessage,
                            chatId = chatId
                        )
                    )
                }

                else -> {}
            }
        }
    }


    Scaffold(topBar = {
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
                Text("llama3.2", fontSize = 20.sp)
                IconButton(onClick = {
                    navHostController.navigate("setting")
                }) {
                    Icon(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = "settings",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        })
    }, bottomBar = {
        OutlinedTextField(
            value = userPrompt,
            onValueChange = { userPrompt = it },
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 10.dp)
                .imePadding(),
            singleLine = true,
            suffix = {
                ElevatedButton(
                    enabled = isEnabled,
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (userPrompt.isNotEmpty()) {
                            viewModel.insert(Message(chatId = chatId, message = userPrompt))
                            toggle = true
                            prompt = userPrompt
                            userPrompt = ""
                            viewModel.sendPrompt(prompt)
                            prompt = ""
                        }
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.send), contentDescription = "Send Button"
                    )
                }
            },
            placeholder = { Text("Enter your prompt...", fontSize = 15.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }) { paddingValues ->
        if (allChats.value.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painterResource(R.drawable.logo), "logo", modifier = Modifier.size(100.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                state = listState
            ) {
                items(allChats.value.size) { index ->
                    ChatBubble(allChats.value[index].message, (index % 2 == 0))
                }
            }
        }
    }
}


