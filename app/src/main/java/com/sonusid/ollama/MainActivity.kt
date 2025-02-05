package com.sonusid.ollama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sonusid.ollama.db.AppDatabase
import com.sonusid.ollama.db.repo.ChatRepository
import com.sonusid.ollama.db.viewmodels.ChatViewModel
import com.sonusid.ollama.db.viewmodels.ChatViewModelFactory
import com.sonusid.ollama.ui.theme.OllamaTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ChatViewModel
    override fun onCreate(savedInstanceState: Bundle?) {

        // Initialize Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChatRepository(database.userDao())

        // Initialize ViewModel with Factory
        val factory = ChatViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            OllamaTheme {
                NavHost(navController = navController, startDestination = "home"){
                    composable("home"){App()}
                }
            }
        }
    }

    private fun enableEdgeToEdge() {
        TODO("Not yet implemented")
    }
}


@Composable
fun App() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Greeting(
            name = "Android",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OllamaTheme {
        Greeting("Android")
    }
}