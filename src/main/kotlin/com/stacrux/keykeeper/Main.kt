package com.stacrux.keykeeper

import java.io.File
import kotlin.system.exitProcess

fun main() {
    val botToken = getBotToken() ?: return

    val keyKeeperService = ServiceProvider.getKeyKeeperService()
    keyKeeperService.initializeAndStartBot(botToken)
    saveBotToken(botToken)

    println("KeyKeeper bot is running...")
}

private val keyKeeperDir = File(System.getProperty("user.home"), "keyKeeper").apply { mkdirs() }

private fun getBotToken(): String? {
    loadBotToken()?.let { return it }

    while (true) {
        print("Insert bot token: ")
        val input = readlnOrNull()?.trim()

        if (input.equals("exit", ignoreCase = true)) {
            println("Shutting down bot...")
            exitProcess(0)
        }

        if (!input.isNullOrBlank()) {
            return input
        }
    }
}

private fun loadBotToken(): String? {
    val file = File(keyKeeperDir, "botToken")
    return file.takeIf { it.exists() && it.length() > 0 }?.readText()?.trim()
}

private fun saveBotToken(token: String) {
    File(keyKeeperDir, "botToken").writeText(token.trim())
}