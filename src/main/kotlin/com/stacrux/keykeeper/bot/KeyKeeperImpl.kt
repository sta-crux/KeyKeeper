package com.stacrux.keykeeper.bot

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.bot.lifestages.stages.*
import com.stacrux.keykeeper.bot.lifestages.stages.addcredentials.AddNewCredentialsStage
import com.stacrux.keykeeper.bot.lifestages.stages.backupstage.BackUpLifeStage
import com.stacrux.keykeeper.bot.lifestages.stages.credentialsmanagement.CredentialsManagementLifeStage
import com.stacrux.keykeeper.bot.lifestages.stages.restoresession.RestoreSessionLifeStage
import com.stacrux.keykeeper.bot.lifestages.stages.servingpassword.PasswordServingLifeStage
import com.stacrux.keykeeper.bot.model.BotRunningState
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.longpolling.BotSession
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer


object KeyKeeperImpl : KeyKeeper {

    private val logger = LoggerFactory.getLogger(KeyKeeperImpl::class.java)

    private var runningState: BotRunningState = BotRunningState.UNBOUND
    private lateinit var runningBotSession: BotSession
    private lateinit var token: String
    private lateinit var userId: String
    private val application = TelegramBotsLongPollingApplication()

    override fun boundUserId(userId: String) {
        KeyKeeperImpl.userId = userId
    }

    override fun canAnswer(userId: String): Boolean {
        return runningState == BotRunningState.UNBOUND || userId == KeyKeeperImpl.userId
    }

    override fun initializeAndStartBot(token: String): String? {
        KeyKeeperImpl.token = token
        val sessionService = ServiceProvider.getDefaultSessionService()
        if (!sessionService.doesSessionExist()) {
            val bindUserIdLifeStage = BindUserIdLifeStage(token, ServiceProvider.getDefaultSessionService())
            runningBotSession =
                application.registerBot(token, bindUserIdLifeStage)
            return bindUserIdLifeStage.getKeyToMatch()
        }
        this.userId = sessionService.retrieveBoundUserId()
        runningState = BotRunningState.RESTORE_SESSION
        runningBotSession = application.registerBot(
            token, RestoreSessionLifeStage(
                token,
                userId,
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultSessionService(),
                ServiceProvider.getDefaultBackUpService(),
                if (!sessionService.doesBackUpFileExist()) null else sessionService.retrieveBackUpFile()
            )
        )
        return null
    }

    override fun advanceBotLifeStage(chatId: String, nextStage: BotRunningState): BotRunningState {

        val nextLifeStage = when (nextStage) {
            BotRunningState.UNBOUND -> AddNewCredentialsStage(
                token,
                chatId,
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultWebSiteParsingService()
            )

            BotRunningState.ADD_CREDENTIALS -> AddNewCredentialsStage(
                token,
                chatId,
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultWebSiteParsingService()
            )

            BotRunningState.SERVING -> PasswordServingLifeStage(
                token,
                chatId,
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultWebSiteParsingService()
            )

            BotRunningState.BACKUP -> BackUpLifeStage(
                token,
                chatId,
                ServiceProvider.getDefaultBackUpService(),
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultSessionService()
            )

            BotRunningState.RESTORE_SESSION -> throw Exception("Unexpected life stage selected")
            BotRunningState.MANAGE_CREDENTIALS -> CredentialsManagementLifeStage(
                chatId,
                token,
                ServiceProvider.getDefaultCredentialsService()
            )
        }
        startNextState(nextLifeStage)
        this.runningState = nextStage
        return runningState
    }


    override fun shutdown() {
        runningBotSession.stop()
    }

    private fun startNextState(nextPollingBot: LongPollingUpdateConsumer) {
        if (::runningBotSession.isInitialized) {
            val previousBotSession = runningBotSession
            previousBotSession.close()
            while (previousBotSession.isRunning) {
                Thread.sleep(500)
            }
            application.unregisterBot(token)
        }
        runningBotSession = application.registerBot(token, nextPollingBot)
    }

}