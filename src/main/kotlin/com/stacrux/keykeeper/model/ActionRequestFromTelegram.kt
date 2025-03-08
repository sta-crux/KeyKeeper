package com.stacrux.keykeeper.model

class ActionRequestFromTelegram(
    chatId: String,
    userId: String,
    userName: String,
    val action: String,
    val actionIdentifier: String,
    val messageId: Int // id of the message linked to the action
) : RequestFromTelegram(chatId, userId, userName)