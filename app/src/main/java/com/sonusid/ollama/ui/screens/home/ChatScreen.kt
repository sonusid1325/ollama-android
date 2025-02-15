package com.sonusid.ollama.ui.screens.home

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
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
fun Home(
    navHostController: NavHostController,
    viewModel: OllamaViewModel,
    chatId: Int = viewModel.chats.value.last().chatId + 1,
) {

    val uiState by viewModel.uiState.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    var userPrompt: String by remember { mutableStateOf("") }
    remember { mutableStateListOf<String>() }
    var prompt: String by remember { mutableStateOf("") }
    val allChats = viewModel.allMessages(chatId).collectAsState(initial = emptyList())
    var isEnabled by remember { mutableStateOf(true) }
    var toggle by remember { mutableStateOf(false) }
    var placeholder by remember { mutableStateOf("Enter your prompt ...") }
    var showModelSelectionDialog by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    val availableModels by viewModel.availableModels.collectAsState()
    val listState = rememberLazyListState()


    LaunchedEffect(allChats.value.size) {
        if (allChats.value.isNotEmpty()) {
            listState.animateScrollToItem(allChats.value.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAvailableModels()
    }

    LaunchedEffect(uiState) {
        if (toggle) {
            when (uiState) {
                is UiState.Success -> {
                    val response = (uiState as UiState.Success).outputText
                    viewModel.insert(Message(message = response, chatId = chatId))
                    isEnabled = true
                    placeholder = "Enter your prompt..."
                }

                is UiState.Error -> {
                    viewModel.insert(
                        Message(
                            message = (uiState as UiState.Error).errorMessage, chatId = chatId
                        )
                    )
                    isEnabled = true
                    placeholder = "Enter your prompt..."
                }

                else -> {
                    isEnabled = true
                }
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
                    navHostController.navigate("chats")
                }) {
                    Icon(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "logo",
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Model selection button:
                TextButton(onClick = {
                    showModelSelectionDialog = true
                }) {
                    Text(
                        if (selectedModel.isNullOrEmpty()) {
                            "Ollama"
                        } else {
                            selectedModel.toString()
                        }, fontSize = 20.sp
                    ) // Display selected model
                }

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
            interactionSource = interactionSource,
            label = {
                Row {
                    Icon(
                        painterResource(R.drawable.logo),
                        contentDescription = "Logo",
                        Modifier.size(25.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Ask llama")
                }
            },
            value = userPrompt,
            onValueChange = { userPrompt = it },
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 5.dp)
                .imePadding(),
            singleLine = true,

            suffix = {
                ElevatedButton(enabled = isEnabled,
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (selectedModel != null) {
                            if (userPrompt.isNotEmpty()) {
                                isEnabled = false
                                placeholder = "I'm thinking ... "
                                viewModel.insert(Message(chatId = chatId, message = userPrompt))
                                toggle = true
                                prompt = userPrompt
                                userPrompt = ""
                                viewModel.sendPrompt(prompt, selectedModel)
                                prompt = ""
                            }
                        } else {
                            viewModel.insert(Message(chatId = chatId, message = userPrompt))
                            userPrompt = ""
                            viewModel.insert(
                                Message(
                                    chatId = chatId, message = "Please Choose a model"
                                )
                            )
                        }
                    }) {
                    Icon(
                        painterResource(R.drawable.send), contentDescription = "Send Button"
                    )
                }
            },
            placeholder = { Text(placeholder, fontSize = 15.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }) { paddingValues ->

        // Model Selection Dialog
        if (showModelSelectionDialog) {
            AlertDialog(onDismissRequest = { showModelSelectionDialog = false },
                title = { Text("Select Model") },
                text = {
                    Column {
                        availableModels.forEach { model ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = model.name == selectedModel,
                                    onClick = { selectedModel = model.name })
                                Text(model.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showModelSelectionDialog = false }) {
                        Text("OK")
                    }
                })
        }

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


