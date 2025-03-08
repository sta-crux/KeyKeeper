package com.stacrux.keykeeper.model

import java.time.Instant

abstract class RequestFromTelegram(
    val chatId: String,
    val userId: String,
    val userName: String,
    val dateInstant: Instant = Instant.now()
)