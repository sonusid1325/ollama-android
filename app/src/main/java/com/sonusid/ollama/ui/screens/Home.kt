package com.sonusid.ollama.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navHostController: NavHostController){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        IconButton(onClick = {}) {

                        }
                    }
                }
            )
        }
    ) {
        paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("Null")
        }
    }
}

@Preview
@Composable
fun HomePreview(){
    val dummyHost = rememberNavController()
    Home(dummyHost)
}