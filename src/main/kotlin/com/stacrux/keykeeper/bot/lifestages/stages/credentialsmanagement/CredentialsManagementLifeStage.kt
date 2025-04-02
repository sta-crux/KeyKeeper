package com.stacrux.keykeeper.bot.lifestages.stages.credentialsmanagement

import com.stacrux.keykeeper.bot.lifestages.AbstractBotLifeStage
import com.stacrux.keykeeper.bot.model.BotRunningState
import com.stacrux.keykeeper.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.model.TextRequestFromTelegram
import com.stacrux.keykeeper.service.CredentialsService
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class CredentialsManagementLifeStage(
    val chatId: String,
    botToken: String,
    private val credentialsService: CredentialsService
) : AbstractBotLifeStage(botToken) {

    private val logger = LoggerFactory.getLogger(CredentialsManagementLifeStage::class.java)


    private var allCredentials = credentialsService.getAllCredentials()
    private val actions = MainActionsCredentialsManagementStage

    init {
        logger.info("Starting credentials manager life stage")
        sendAllCredentialsMessages()
    }

    override fun reactToTextRequest(request: TextRequestFromTelegram) {
        if (request.textContent.contains("delete")) {
            val index = try {
                request.textContent.split(" ")[1].toInt()
            } catch (e: Exception) {
                return
            }
            if (index > allCredentials.lastIndex) {
                return
            }
            credentialsService.removeCredentials(allCredentials[index])
            sendMessage(chatId, "Deleted $index", actionButtons = actions)
        }
    }

    override fun reactToActionRequest(request: ActionRequestFromTelegram) {
        if (request.action == actions.getAbandonActionId()) {
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
        }
        if (request.action == actions.getPrintWholeList()) {
            allCredentials = credentialsService.getAllCredentials()
            sendAllCredentialsMessages()
        }
    }

    override fun reactToReceivedFile(request: FileProvidedByTelegramUser) {
        return
    }

    private fun sendAllCredentialsMessages() {
        if (allCredentials.isEmpty()) {
            sendMessage(chatId, "There are no credentials to manage")
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
        }
        var messageContent = ""

        val indexed = allCredentials.sortedBy { it.host }
            .mapIndexed { index, credentials -> index to credentials }
            .toMap()

        indexed.entries.map {
            messageContent += "" + it.key + " | " + it.value.host + " | " + it.value.username + " | " +
                    "`delete " + "${it.key}`\n"
            if (messageContent.length > 2000) {
                sendMessage(chatId, messageContent, actionButtons = actions)
                messageContent = ""
                Thread.sleep(800)
            }
        }
        if (StringUtils.isNotEmpty(messageContent)) {
            sendMessage(chatId, messageContent, actionButtons = actions)
        }

    }
}