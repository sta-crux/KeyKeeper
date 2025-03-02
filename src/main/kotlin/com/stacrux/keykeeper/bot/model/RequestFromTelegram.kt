package com.stacrux.keykeeper.bot.model

abstract class RequestFromTelegram(
    val chatId: String,
    val userId: String,
    val userName: String
)