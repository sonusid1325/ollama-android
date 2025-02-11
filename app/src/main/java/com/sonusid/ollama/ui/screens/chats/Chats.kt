package com.sonusid.ollama.ui.screens.chats

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@Composable
fun Chats(navController: NavController, viewModel: OllamaViewModel){
    val allChats = viewModel.chats.collectAsState()
    Scaffold {
        paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(allChats.value.size){
                index ->
                ElevatedButton(
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp),
                    onClick = {

                    }) {
                    Row { Text(allChats.value[index].title) }
                }
            }
            item{
                ElevatedButton(
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp),
                    onClick = {

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

@Preview(showBackground = true)
@Composable
fun ChatsPreview(){
    val dummy = rememberNavController()
    MaterialTheme(){
        Chats(dummy, viewModel())
    }
}