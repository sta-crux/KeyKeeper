package com.stacrux.keykeeper.bot.lifestages.stages

import com.stacrux.keykeeper.bot.lifestages.AbstractBotLifeStage
import com.stacrux.keykeeper.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.bot.model.BotRunningState
import com.stacrux.keykeeper.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.model.TextRequestFromTelegram
import com.stacrux.keykeeper.service.SessionService
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory

class BindUserIdLifeStage(token: String, private val sessionService: SessionService) : AbstractBotLifeStage(token) {

    private val logger = LoggerFactory.getLogger(BindUserIdLifeStage::class.java)
    private val messagesHitCounter: MutableMap<String, Int> = mutableMapOf()
    private val keyToMatch = RandomStringUtils.randomAlphabetic(32)

    init {
        logger.info("Starting BoundUserIdToBot")
        logger.info("Please contact me with the following message to bind your user id")
        logger.info(keyToMatch)
    }

    override fun reactToTextRequest(request: TextRequestFromTelegram) {
        if (keyToMatch.equals(request.textContent)) {
            sendMessage(
                request.chatId, "Hello ${request.userName}, " +
                        "I am now bound to your account n ${request.userId}"
            )
            getKeyKeeper().boundUserId(request.userId)
            sessionService.createSession(request.userId)
            getKeyKeeper().advanceBotLifeStage(request.chatId, BotRunningState.SERVING)
            return
        }
        if ((messagesHitCounter[request.userId] ?: 0) > 30) {
            logger.warn(
                "Brute forcing attempt done by id ${request.userId} " +
                        "with username ${request.userName}"
            )
            return
        }
        if (messagesHitCounter[request.userId] == 0) {
            sendMessage(
                request.chatId,
                "Hey, it seems like I went to sleep and I just woke up!" +
                        "please send me again the password that I display (wherever I am running) " +
                        "to link your user"
            )
            return
        }
        sendMessage(request.chatId, "Sorry, that did not work... please send the binding key")
    }

    override fun reactToActionRequest(request: ActionRequestFromTelegram) {
        sendMessage(request.chatId, "No button is working here; please send the binding key")
    }

    override fun reactToReceivedFile(request: FileProvidedByTelegramUser) {
        return
    }

}