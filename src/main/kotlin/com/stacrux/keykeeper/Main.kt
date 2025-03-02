package com.stacrux.keykeeper

import org.apache.commons.lang3.StringUtils
import java.io.File
import kotlin.system.exitProcess

fun main() {

    // the first time the token is taken from the standard input,
    // then stored to a file inside home/keyKeeper/botToken
    var botToken: String? = loadBotToken()

    while (StringUtils.isBlank(botToken)) {
        print("Insert bot token: ")
        val input = readlnOrNull()
        if (input.equals("exit", ignoreCase = true)) {
            println("Shutting down bot...")
            exitProcess(0) // Stop the program
        } else {
            botToken = input
        }
    }
    val keyKeeperService = ServiceProvider.getKeyKeeperService()
    keyKeeperService.initializeAndStartBot(botToken ?: "")
    saveBotToken(botToken ?: throw Exception())

    println("KeyKeeper bot is running...")
}

private val keyKeeperDir = File(System.getProperty("user.home"), "keyKeeper").apply {
    mkdirs()
}

fun loadBotToken(): String? {
    val file = File(keyKeeperDir, "botToken")
    return if (file.exists() && file.length() > 0) {
        file.readText().trim()
    } else {
        null
    }
}

fun saveBotToken(token: String) {
    val file = File(keyKeeperDir, "botToken")
    file.writeText(token.trim())
}