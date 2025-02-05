package com.sonusid.ollama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sonusid.ollama.ui.theme.OllamaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            OllamaTheme {
                NavHost(navController = navController, startDestination = "home"){
                    composable("home"){Home()}
                }
            }
        }
    }

    private fun enableEdgeToEdge() {
        TODO("Not yet implemented")
    }
}

@Composable
fun Home(){

}