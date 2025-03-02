package com.stacrux.keykeeper.bot

import com.stacrux.keykeeper.bot.model.BotRunningState

/**
 * This is the main interface representing a long-lasting bot, the bot is born with the app boot
 * and passes through different life stages
 * life-stage: Unbound user id; in this stage the bot can only wait for a binding
 * life-stage: Serving the passwords, in this stage the bot serves the password or store new ones if needed
 */
interface KeyKeeper {


    /**
     * used to set the user id that the bot will reply to with the passwords
     */
    fun boundUserId(userId: String)

    /**
     * The bot will respond only to the user id that share the secret generated at startup,
     * it is printed in the console, send it to the bot via telegram to bind your user.
     */
    fun canAnswer(userId: String): Boolean

    /**
     * Bot startup and init routine
     */
    fun initializeAndStartBot(token: String)

    /**
     * Start the new life-stage of the bot
     */
    fun advanceBotLifeStage(chatId: String, nextStage: BotRunningState): BotRunningState

    /**
     * Closes the bot
     */
    fun shutdown()

}