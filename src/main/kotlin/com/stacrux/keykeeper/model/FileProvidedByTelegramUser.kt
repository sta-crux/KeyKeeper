package com.stacrux.keykeeper.model

import java.io.File

class FileProvidedByTelegramUser(
    chatId: String,
    userId: String,
    username: String,
    val downloadedFile: File
) : RequestFromTelegram(chatId, userId, username)


