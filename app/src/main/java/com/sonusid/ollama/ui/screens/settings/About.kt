package com.sonusid.ollama.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sonusid.ollama.R
import com.sonusid.ollama.api.RetrofitClient
import com.sonusid.ollama.navigation.Routes
import com.sonusid.ollama.ui.components.LamiAvatar
import com.sonusid.ollama.ui.components.LamiStatusSprite
import com.sonusid.ollama.ui.components.mapToLamiSpriteStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(navController: NavController) {
    val baseUrl = remember { RetrofitClient.currentBaseUrl().trimEnd('/') }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LamiAvatar(
                            baseUrl = baseUrl,
                            selectedModel = null,
                            lastError = null,
                            onNavigateSettings = { navController.navigate(Routes.SETTINGS) }
                        )
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(painterResource(R.drawable.back), "exit")
                        }
                    }
                },
                title = { Text("About") }
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 100.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Box(
                modifier = Modifier
                    .clip(shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                LamiStatusSprite(
                    status = mapToLamiSpriteStatus(),
                    sizeDp = 100.dp,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceBright
                        )
                        .size(100.dp)
                        .padding(20.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("Ollama", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Text("v1.0.0 (Beta)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    val dummyNav = rememberNavController()
   MaterialTheme(colorScheme = lightColorScheme()) { About(dummyNav) }
}
