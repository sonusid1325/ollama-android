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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sonusid.ollama.db.AppDatabase
import com.sonusid.ollama.db.repository.UserRepository
import com.sonusid.ollama.ui.screens.Home
import com.sonusid.ollama.ui.theme.OllamaTheme
import com.sonusid.ollama.viewmodels.UserViewModel
import com.sonusid.ollama.viewmodels.UserViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = UserRepository(database.userDao())

        // Initialize ViewModel with Factory
        val factory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
        setContent {
        // Initialise navigation
        val navController = rememberNavController()
            OllamaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController=navController, startDestination = "home"){
                            composable("home") { Home(navController) }
                        }
                    }
                }
            }
        }
    }
}