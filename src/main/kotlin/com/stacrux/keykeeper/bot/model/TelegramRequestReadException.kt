package com.stacrux.keykeeper.bot.model

class TelegramRequestReadException(message: String, exception: Throwable) :
    Exception(message, exception)