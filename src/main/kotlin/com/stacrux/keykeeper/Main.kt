package com.stacrux.keykeeper

import com.stacrux.keykeeper.bot.model.BotRunningState
import io.ktor.server.application.call
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.get

import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File

private val keyKeeperDir = File(System.getProperty("user.home"), "keyKeeper").apply { mkdirs() }
private val botTokenFile = File(keyKeeperDir, "botToken")

private fun loadBotToken(): String? {
    val file = File(keyKeeperDir, "botToken")
    return file.takeIf { it.exists() && it.length() > 0}?.readText()?.trim()
}

/**
 * The main starts a small server to store the bot token and expose a binding key, this key is used to bind a user to
 * the bot, read it from the GET call and send the key to the bot via telegram itself, the bot will then react only
 * to the first user that sent the key.
 */
fun main() {

    embeddedServer(Netty, port = 8080) {

        val botToken = loadBotToken() ?: ""

        if (!botToken.isEmpty()) {
            val keyKeeperService = ServiceProvider.getKeyKeeperService()
            keyKeeperService.initializeAndStartBot(botToken)
        }

        routing {
            // POST /botToken
            post("/botToken") {
                val params = call.receiveParameters()
                val token = params["token"]?.trim()
                if (token.isNullOrBlank()) {
                    call.respond("Error: token missing\n")
                    return@post
                }
                if (botTokenFile.exists()) {
                    call.respond("Error: token already stored\n")
                    return@post
                }
                try {
                    botTokenFile.writeText(token)
                    call.respond("Token stored successfully, starting Bot\n")
                    val keyKeeperService = ServiceProvider.getKeyKeeperService()
                    keyKeeperService.initializeAndStartBot(token)
                    println("KeyKeeper bot is running...")
                } catch (e: Exception) {
                    println(e.message)
                    call.respond("Error storing the token\n")
                }
            }

            // GET /bindingKey?token=...
            get("/bindingKey") {
                val tokenParam = call.request.queryParameters["token"]?.trim()
                if (!botTokenFile.exists()) {
                    call.respond("Error: no token stored yet\n")
                    return@get
                }

                if (ServiceProvider.getKeyKeeperService().getRunningState() != BotRunningState.UNBOUND) {
                    call.respond("The bot is already bound to a user!\n")
                }

                val storedToken = loadBotToken()
                println("Here the stored token: $storedToken")
                println("Here the receiv token: $tokenParam")
                if (tokenParam == storedToken) {
                    val bindingKey = ServiceProvider.getKeyKeeperService().getBindingKey()
                    call.respond("Here is your binding key: $bindingKey\n")
                } else {
                    call.respond("Error: token mismatch, cannot give you the binding key...\n")
                }
            }
        }
    }.start(wait = true)

}
