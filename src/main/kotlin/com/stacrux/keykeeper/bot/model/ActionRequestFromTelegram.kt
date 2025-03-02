package com.stacrux.keykeeper.bot.model

class ActionRequestFromTelegram(
    chatId: String,
    userId: String,
    userName: String,
    val action: String,
    val actionIdentifier: String
) : RequestFromTelegram(chatId, userId, userName) {
}