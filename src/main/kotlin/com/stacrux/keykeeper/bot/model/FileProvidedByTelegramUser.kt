package com.stacrux.keykeeper.bot.model

import java.io.File

class FileProvidedByTelegramUser(
    chatId: String,
    userId: String,
    username: String,
    val downloadedFile: File
) : RequestFromTelegram(chatId, userId, username)


