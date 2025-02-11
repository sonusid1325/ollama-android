package com.sonusid.ollama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sonusid.ollama.db.AppDatabase
import com.sonusid.ollama.db.ChatDatabase
import com.sonusid.ollama.db.repository.ChatRepository
import com.sonusid.ollama.ui.screens.chats.Chats
import com.sonusid.ollama.ui.screens.home.Home
import com.sonusid.ollama.ui.screens.settings.About
import com.sonusid.ollama.ui.screens.settings.Settings
import com.sonusid.ollama.ui.theme.OllamaTheme
import com.sonusid.ollama.viewmodels.OllamaViewModel
import com.sonusid.ollama.viewmodels.OllamaViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: OllamaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Database & Repository
        val database = ChatDatabase.Companion.getDatabase(applicationContext)
        val repository = ChatRepository(chatDao = database.chatDao(), messageDao = database.messageDao())

        // Initialize ViewModel with Factory
        val factory = OllamaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[OllamaViewModel::class.java]

        setContent {
            // Initialise navigation
            val navController = rememberNavController()
            OllamaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                        NavHost(
                            navController = navController,
                            startDestination = "chats"
                        ) {
                            composable("home") {
                                Home(navController, viewModel, 0)
                            }
                            composable("chats") {
                                Chats(navController, viewModel)
                            }
                            composable(
                                route = "chat/{chatID}",
                                arguments = listOf(navArgument("chatID") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val chatId = backStackEntry.arguments?.getInt("chatID") ?: 0
                                Home(navController, viewModel, chatId)
                            }
                            composable("setting") {
                                Settings(navController)
                            }
                            composable("about") {
                                About(navController)
                            }
                        }
                    }
                }
            }
        }
    }
}