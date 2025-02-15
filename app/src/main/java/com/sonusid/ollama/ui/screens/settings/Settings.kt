package com.sonusid.ollama.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sonusid.ollama.R
import com.sonusid.ollama.db.AppDatabase
import com.sonusid.ollama.db.entity.BaseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navgationController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)
    val baseUrlDao = db.baseUrlDao()
    var gateway by remember { mutableStateOf("localhost:11434") }
    var valid by remember { mutableStateOf(true) }
    var social = listOf<SettingsData>(
        SettingsData(url = "https://github.com/sonusid1325", name = "GitHub", R.drawable.github),
        SettingsData(
            url = "https://github.com/sponsors/sonusid1325",
            name = "GitHub Sponsor",
            R.drawable.sponsor
        ),
        SettingsData(
            url = "https://buymeacoffee.com/sonusid1325",
            name = "Buy me Coffee",
            R.drawable.coffee
        ),
    )
    LaunchedEffect(Unit) {
        val storedUrl = baseUrlDao.getBaseUrl()
        if (storedUrl != null) {
            gateway = storedUrl.url
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navgationController.popBackStack() }) {
                        Icon(
                            painterResource(R.drawable.back),
                            "exit"
                        )
                    }
                },
                title = { Text("Settings") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
            ) { Text("Ollama v1.0.0") }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                OutlinedTextField(
                    value = gateway,
                    onValueChange = { gateway = it },
                    placeholder = { Text("localhost:11434") },
                    label = { Text("Server") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .padding(top = 0.dp),
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (valid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.error,
                        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    suffix = {
                        IconButton(onClick = {
                            scope.launch {
                                val baseUrl =
                                    BaseUrl(url = gateway)
                                baseUrlDao.insertBaseUrl(baseUrl)
                                val intent =
                                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Important flags
                                context.startActivity(intent)
                                Process.killProcess(Process.myPid())

                            }
                        }, modifier = Modifier.size(25.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.save),
                                contentDescription = "Save Address"
                            )
                        }
                    }
                )
            }
            items(social.size) { index ->
                val value = social[index]
                ElevatedButton(
                    onClick = {
                        openUrl(context, value.url)
                    }, modifier = Modifier
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Icon(
                            painterResource(value.logo),
                            contentDescription = value.name,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(20.dp))
                        Text(value.name)
                    }
                }
            }
            item {
                ElevatedButton(
                    onClick = {
                        navgationController.navigate("about")
                    }, modifier = Modifier
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Icon(
                            painterResource(R.drawable.about),
                            contentDescription = "About",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(20.dp))
                        Text("About")
                    }
                }
            }
        }
    }
}

suspend fun isValidURL(urlString: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val url = URL(urlString) // Check URL format
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()  // Make a request (empty)
        response.close() // Important: Close the response
        response.isSuccessful // Check if the response indicates success (2xx or 3xx status codes)

    } catch (e: MalformedURLException) {
        false // Invalid URL format
    } catch (e: IOException) {
        false // Network error or other IO issues
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    val dummyNav = rememberNavController()
    MaterialTheme(colorScheme = darkColorScheme()) {
        Settings(dummyNav)
    }
}