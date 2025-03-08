package com.stacrux.keykeeper.model

class TextRequestFromTelegram(
    chatId: String,
    userId: String,
    userName: String,
    val textContent: String
) : RequestFromTelegram(chatId, userId, userName)