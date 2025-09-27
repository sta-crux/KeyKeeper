package com.stacrux.keykeeper.bot.impl

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.bot.KeyKeeper
import com.stacrux.keykeeper.bot.impl.lifestages.BindUserIdLifeStage
import com.stacrux.keykeeper.bot.impl.lifestages.addcredentials.AddNewCredentialsStage
import com.stacrux.keykeeper.bot.impl.lifestages.backupstage.BackUpLifeStage
import com.stacrux.keykeeper.bot.impl.lifestages.credentialsmanagement.CredentialsManagementLifeStage
import com.stacrux.keykeeper.bot.impl.lifestages.restoresession.RestoreSessionLifeStage
import com.stacrux.keykeeper.bot.impl.lifestages.servingpassword.PasswordServingLifeStage
import com.stacrux.keykeeper.bot.model.BotBindingDetails
import com.stacrux.keykeeper.bot.model.BotRunningState
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.longpolling.BotSession
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import java.time.Duration
import java.time.Instant

object KeyKeeperImpl : KeyKeeper {

    private val logger = LoggerFactory.getLogger(KeyKeeperImpl::class.java)

    private var runningState: BotRunningState = BotRunningState.UNBOUND
    private lateinit var runningBotSession: BotSession
    private lateinit var token: String
    private lateinit var userId: String
    private val application = TelegramBotsLongPollingApplication()
    private lateinit var keyKeeperClientHolder: KeyKeeperClientHolder


    override fun getClientHolder(): com.stacrux.keykeeper.bot.KeyKeeperClientHolder {
        return keyKeeperClientHolder
    }

    override fun boundUserId(userId: String) {
        KeyKeeperImpl.userId = userId
    }

    override fun canAnswer(userId: String): Boolean {
        return runningState == BotRunningState.UNBOUND || userId == KeyKeeperImpl.userId
    }

    override fun initializeAndStartBot(token: String): BotBindingDetails {
        KeyKeeperImpl.token = token
        keyKeeperClientHolder = KeyKeeperClientHolder(token)


        val sessionService = ServiceProvider.getDefaultSessionService()
        if (!sessionService.doesSessionExist()) {
            val bindUserIdLifeStage = BindUserIdLifeStage(keyKeeperClientHolder, ServiceProvider.getDefaultSessionService())
            runningBotSession =
                application.registerBot(token, bindUserIdLifeStage)
            return BotBindingDetails(getBotUserName(), bindUserIdLifeStage.getKeyToMatch())
        }
        this.userId = sessionService.retrieveBoundUserId()
        runningState = BotRunningState.RESTORE_SESSION
        val lifeStage = RestoreSessionLifeStage(
            keyKeeperClientHolder,
            userId,
            ServiceProvider.getDefaultCredentialsService(),
            ServiceProvider.getDefaultSessionService(),
            ServiceProvider.getDefaultBackUpService(),
            if (!sessionService.doesBackUpFileExist()) null else sessionService.retrieveBackUpFile()
        )
        runningBotSession = application.registerBot(
            token, lifeStage
        )
        return BotBindingDetails(getBotUserName(), null)
    }

    override fun advanceBotLifeStage(chatId: String, nextStage: BotRunningState): BotRunningState {

        val nextLifeStage = when (nextStage) {
            BotRunningState.UNBOUND -> AddNewCredentialsStage(
                keyKeeperClientHolder,
                chatId,
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultWebSiteParsingService()
            )

            BotRunningState.ADD_CREDENTIALS -> AddNewCredentialsStage(
                keyKeeperClientHolder,
                chatId,
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultWebSiteParsingService()
            )

            BotRunningState.SERVING -> PasswordServingLifeStage(
                keyKeeperClientHolder,
                chatId,
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultWebSiteParsingService()
            )

            BotRunningState.BACKUP -> BackUpLifeStage(
                keyKeeperClientHolder,
                chatId,
                ServiceProvider.getDefaultBackUpService(),
                ServiceProvider.getDefaultCredentialsService(),
                ServiceProvider.getDefaultSessionService()
            )

            BotRunningState.RESTORE_SESSION -> throw Exception("Unexpected life stage selected")
            BotRunningState.MANAGE_CREDENTIALS -> CredentialsManagementLifeStage(
                keyKeeperClientHolder,
                chatId,
                ServiceProvider.getDefaultCredentialsService()
            )
        }
        startNextState(nextLifeStage)
        this.runningState = nextStage
        return runningState
    }


    override fun shutdown() {
        application.close()
        val timeout = Instant.now().plus(Duration.ofSeconds(15))
        while (application.isRunning && Instant.now().isBefore(timeout)) {
            Thread.sleep(100) // wait until session is really down
        }
    }

    override fun getRunningState(): BotRunningState {
        return runningState
    }

    override fun getBindingKey(): String {
        if (runningState != BotRunningState.UNBOUND) {
            throw Exception("The bot is already bound to a user, no binding key available.")
        }
        return (runningBotSession.updatesConsumer as BindUserIdLifeStage).getKeyToMatch()
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