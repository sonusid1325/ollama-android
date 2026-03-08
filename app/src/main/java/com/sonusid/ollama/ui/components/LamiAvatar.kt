package com.sonusid.ollama.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonusid.ollama.R
import com.sonusid.ollama.api.RetrofitClient
import com.sonusid.ollama.viewmodels.LamiStatus
import com.sonusid.ollama.viewmodels.ModelInfo
import com.sonusid.ollama.viewmodels.mapToLamiState
import com.sonusid.ollama.ui.components.LamiStatusSprite
import com.sonusid.ollama.ui.components.mapToLamiSpriteStatus
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LamiAvatar(
    baseUrl: String,
    selectedModel: String?,
    lastError: String?,
    lamiStatus: LamiStatus = LamiStatus.CONNECTING,
    availableModels: List<ModelInfo> = emptyList(),
    modifier: Modifier = Modifier,
    avatarShape: Shape = RoundedCornerShape(8.dp),
    initialAvatarSize: Dp = 36.dp,
    minAvatarSize: Dp = 32.dp,
    maxAvatarSize: Dp = 64.dp,
    onSelectModel: (String) -> Unit = {},
    onNavigateSettings: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var animationsEnabled by rememberSaveable { mutableStateOf(true) }
    var replacementEnabled by rememberSaveable { mutableStateOf(true) }
    var blinkEffectEnabled by rememberSaveable { mutableStateOf(false) }
    var showStatusDetails by rememberSaveable { mutableStateOf(true) }
    val clampedInitialSize = initialAvatarSize.value
        .roundToInt()
        .coerceIn(minAvatarSize.value.roundToInt(), maxAvatarSize.value.roundToInt())
    var avatarSize by rememberSaveable(
        inputs = arrayOf(minAvatarSize.value, maxAvatarSize.value, clampedInitialSize)
    ) {
        mutableStateOf(clampedInitialSize)
    }
    var lastUpdated by rememberSaveable { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val initializationState = RetrofitClient.getLastInitializationState()
    val fallbackActive = initializationState?.usedFallback == true
    val fallbackMessage = initializationState?.errorMessage
    val avatarSpriteStatus = remember(lamiStatus, selectedModel, lastError) {
        val currentState = mapToLamiState(
            lamiStatus = lamiStatus,
            selectedModel = selectedModel,
            lastError = lastError
        )
        mapToLamiSpriteStatus(
            lamiStatus = lamiStatus,
            lamiState = currentState,
            lastError = lastError
        )
    }
    val statusLabel = remember(lamiStatus) {
        when (lamiStatus) {
            LamiStatus.CONNECTING -> "接続中"
            LamiStatus.READY -> "接続良好"
            LamiStatus.DEGRADED -> "フォールバック中"
            LamiStatus.NO_MODELS -> "モデルなし"
            LamiStatus.OFFLINE -> "オフライン"
            LamiStatus.ERROR -> "エラー"
            LamiStatus.TALKING -> "話し中"
        }
    }

    LaunchedEffect(baseUrl, selectedModel, lastError, fallbackActive) {
        lastUpdated = formatter.format(Date())
    }

    Box(
        modifier = modifier
            .size(avatarSize.dp)
            .clip(avatarShape)
            .combinedClickable(
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    showMenu = true
                },
                onLongClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    showSheet = true
                }
            )
    ) {
        LamiStatusSprite(
            status = avatarSpriteStatus,
            sizeDp = avatarSize.dp,
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent { drawContent() },
            animationsEnabled = animationsEnabled,
            replacementEnabled = replacementEnabled,
            blinkEffectEnabled = blinkEffectEnabled
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("""状態: $statusLabel""") },
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("""接続先: ${baseUrl.ifBlank { "未設定" }}""") },
                onClick = { }
            )
            val canSelectModel = availableModels.size > 1
            DropdownMenuItem(
                text = { Text("""モデル: ${selectedModel ?: "未選択"}""") },
                onClick = {
                    if (canSelectModel) {
                        showSheet = true
                        showMenu = false
                    }
                },
                enabled = canSelectModel
            )
            DropdownMenuItem(
                text = { Text("""フォールバック: ${if (fallbackActive) "ON" else "OFF"}""") },
                onClick = { }
            )
            if (fallbackActive && !fallbackMessage.isNullOrBlank()) {
                DropdownMenuItem(
                    text = { Text("""理由: $fallbackMessage""") },
                    onClick = { }
                )
            }
            DropdownMenuItem(text = { Text("最終更新: $lastUpdated") }, onClick = { })
            if (showStatusDetails) {
                DropdownMenuItem(
                    text = { Text("""エラー概要: ${lastError ?: "なし"}""") },
                    onClick = { }
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showSheet = false }
            ) {
                val sheetMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.7f
                val listState: LazyListState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                var searchQuery by rememberSaveable { mutableStateOf("") }
                val filteredModels by remember(availableModels, searchQuery) {
                    derivedStateOf {
                        availableModels.filter { model ->
                            searchQuery.isBlank() || model.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                val currentState = mapToLamiState(
                    lamiStatus = lamiStatus,
                    selectedModel = selectedModel,
                    lastError = lastError
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = sheetMaxHeight),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                LamiSprite(
                                    state = currentState,
                                    sizeDp = 64.dp,
                                    animationsEnabled = animationsEnabled,
                                    replacementEnabled = replacementEnabled,
                                    blinkEffectEnabled = blinkEffectEnabled
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Lami コントロール", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    if (showStatusDetails) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = statusLabel,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "最終更新: $lastUpdated",
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = {
                                onNavigateSettings?.invoke()
                                showSheet = false
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.settings),
                                    contentDescription = "設定を開く"
                                )
                            }
                        }
                    }
                    item { StatusInfoItem(label = "接続先", value = baseUrl.ifBlank { "未設定" }) }
                    item { StatusInfoItem(label = "選択モデル", value = selectedModel ?: "未選択") }
                    item { StatusInfoItem(label = "フォールバック", value = if (fallbackActive) "ON" else "OFF") }
                    if (fallbackActive && !fallbackMessage.isNullOrBlank()) {
                        item { StatusInfoItem(label = "フォールバック理由", value = fallbackMessage) }
                    }
                    if (showStatusDetails) {
                        item { StatusInfoItem(label = "エラー概要", value = lastError ?: "なし") }
                    }
                    item { Divider() }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("利用可能なモデル", fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = searchQuery,
                                onValueChange = { query -> searchQuery = query },
                                placeholder = { Text("モデルを検索") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "モデル検索"
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "検索文字列をクリア"
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        scope.launch { listState.animateScrollToItem(0) }
                                    }
                                )
                            )
                        }
                    }
                    if (filteredModels.isEmpty()) {
                        item { Text("モデルを取得できませんでした") }
                    } else {
                        items(filteredModels, key = { model -> model.name }) { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .selectable(
                                        selected = selectedModel == model.name,
                                        onClick = {
                                            onSelectModel(model.name)
                                            showSheet = false
                                        },
                                        role = Role.RadioButton
                                    )
                                    .semantics {
                                        contentDescription = "モデル ${model.name} を選択"
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedModel == model.name,
                                        onClick = {
                                            onSelectModel(model.name)
                                            showSheet = false
                                        },
                                        modifier = Modifier.semantics { contentDescription = "モデル ${model.name}" }
                                    )
                                    Text(
                                        text = model.name,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                if (selectedModel == model.name) {
                                    Text("選択中", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    item {
                        ToggleRow(
                            label = "アニメーション",
                            checked = animationsEnabled,
                            onCheckedChange = { animationsEnabled = it }
                        )
                    }
                    item {
                        ToggleRow(
                            label = "置換",
                            checked = replacementEnabled,
                            onCheckedChange = { replacementEnabled = it }
                        )
                    }
                    item {
                        ToggleRow(
                            label = "点滅エフェクト",
                            checked = blinkEffectEnabled,
                            onCheckedChange = { blinkEffectEnabled = it }
                        )
                    }
                    item {
                        ToggleRow(
                            label = "ステータス詳細表示",
                            checked = showStatusDetails,
                            onCheckedChange = { showStatusDetails = it }
                        )
                    }
                    item {
                        Column {
                            Text("表示サイズ (${avatarSize}dp)", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = avatarSize.toFloat(),
                                onValueChange = { value ->
                                    val snapped = (value.roundToInt() / 2) * 2
                                    avatarSize = snapped.coerceIn(
                                        minAvatarSize.value.roundToInt(),
                                        maxAvatarSize.value.roundToInt()
                                    )
                                },
                                valueRange = minAvatarSize.value..maxAvatarSize.value,
                                steps = 0
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onNavigateSettings?.invoke()
                                showSheet = false
                            }
                        ) {
                            Text("設定画面へ移動")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun StatusInfoItem(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp)
    }
}
