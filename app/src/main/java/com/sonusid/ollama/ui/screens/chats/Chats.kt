package com.sonusid.ollama.ui.screens.chats

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sonusid.ollama.ui.screens.settings.openUrl
import com.sonusid.ollama.viewmodels.OllamaViewModel
import com.sonusid.ollama.R
import com.sonusid.ollama.db.entity.Chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chats(navController: NavController, viewModel: OllamaViewModel) {
    val allChats = viewModel.chats.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painterResource(R.drawable.back), "exit")
                    }
                },
                title = { Text("All Chats") }
            )
        }
    ) { paddingValues ->
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
            item {
                ElevatedButton(
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .size(40.dp),
                    onClick = {
                        viewModel.insertChat(chat = Chat(title = "New Chat"))
                    }) {
                    Icon(
                        painterResource(R.drawable.add),
                        "add",
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
