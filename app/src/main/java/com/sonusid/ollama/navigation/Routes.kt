package com.sonusid.ollama.navigation

object Routes {
    const val HOME = "home"
    const val CHATS = "chats"
    const val CHAT = "chat"
    const val CHAT_ID_ARG = "chatID"
    const val CHAT_WITH_ID = "$CHAT/{$CHAT_ID_ARG}"
    const val SETTINGS = "setting"
    const val ABOUT = "about"
    const val SPRITE_SETTINGS = "settings/sprite_settings"

    fun chat(chatId: Int): String = "$CHAT/$chatId"
}

sealed interface SettingsRoute {
    val route: String

    data object SpriteSettings : SettingsRoute {
        override val route: String = Routes.SPRITE_SETTINGS
    }
}
