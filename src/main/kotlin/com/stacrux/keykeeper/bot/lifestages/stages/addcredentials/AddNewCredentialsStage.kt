package com.stacrux.keykeeper.bot.lifestages.stages.addcredentials

import com.stacrux.keykeeper.bot.lifestages.AbstractBotLifeStage
import com.stacrux.keykeeper.bot.model.*
import com.stacrux.keykeeper.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.model.TextRequestFromTelegram
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.WebsiteParsingService
import org.slf4j.LoggerFactory

/**
 * This life stage is used to group the features around adding new credentials to the vault
 */
class AddNewCredentialsStage(
    token: String,
    val chatId: String,
    private val credentialsService: CredentialsService,
    private val websiteParsingService: WebsiteParsingService
) : AbstractBotLifeStage(token) {

    private val logger = LoggerFactory.getLogger(AddNewCredentialsStage::class.java)

    private var newEntryUrl: String? = null
    private var newEntryUserName: String? = null
    private var newEntryPassword: String? = null

    private val actions = AbandonOrRestart

    init {
        promptForWebsite()
    }

    override fun reactToTextRequest(request: TextRequestFromTelegram) {
        when {
            newEntryUrl == null -> processWebsite(request.textContent)
            newEntryUserName == null -> processUsername(request.textContent)
            newEntryPassword == null -> processPassword(request.textContent)
            else -> restartEntry()
        }
    }

    override fun reactToActionRequest(request: ActionRequestFromTelegram) {
        when (request.action) {
            actions.getRestartActionIdentifier()-> restartEntry()
            actions.getAbandonActionIdentifier() -> abandonProcess()
            else -> sendMessage(
                request.chatId,
                "Sorry, that button does not work in this context",
                actionButtons = actions
            )
        }
    }

    override fun reactToReceivedFile(request: FileProvidedByTelegramUser) {
        return
    }

    private fun promptForWebsite() {
        sendMessage(chatId, "Alright, send me the website to store, the whole URL is fine", actionButtons = actions)
    }

    private fun processWebsite(textContent: String) {
        try {
            websiteParsingService.extractWebsiteIdentifier(textContent)
        } catch (e: Exception) {
            sendMessage(chatId, "I could not understand that URL, can you try another way?", actionButtons = actions)
            return
        }
        // the text content contains a valid URL
        newEntryUrl = textContent
        sendMessage(chatId, "Alright, recognized [$newEntryUrl]. Now enter a username", actionButtons = actions)
    }

    private fun processUsername(textContent: String) {
        newEntryUserName = textContent
        sendMessage(
            chatId,
            "Fine, the username is $newEntryUserName. Now enter the password/secret",
            actionButtons = actions
        )
    }

    private fun processPassword(textContent: String) {
        newEntryPassword = textContent
        sendMessage(chatId, "Done, password received. You can now delete your message.", actionButtons = actions)
        credentialsService.insertEntry(
            newEntryUrl ?: throw Exception(),
            newEntryUserName ?: throw Exception(),
            newEntryPassword ?: throw Exception()
        )
    }

    private fun restartEntry() {
        resetState()
        promptForWebsite()
    }

    private fun abandonProcess() {
        logger.info("Sure! Back to serving known passwords")
        getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
    }

    private fun resetState() {
        newEntryUrl = null
        newEntryUserName = null
        newEntryPassword = null
    }
}
