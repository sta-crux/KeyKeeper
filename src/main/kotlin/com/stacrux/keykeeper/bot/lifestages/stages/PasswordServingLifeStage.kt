package com.stacrux.keykeeper.bot.lifestages.stages

import com.stacrux.keykeeper.bot.lifestages.AbstractBotLifeStage
import com.stacrux.keykeeper.bot.model.*
import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.WebsiteParsingService
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors


class PasswordServingLifeStage(
    token: String,
    val chatId: String,
    private val credentialsService: CredentialsService,
    private val websiteParsingService: WebsiteParsingService
) :
    AbstractBotLifeStage(token) {

    private val logger = LoggerFactory.getLogger(PasswordServingLifeStage::class.java)
    private val addActionIdentifier = "add_credential_entry"
    private val backUpActionIdentifier = "backup_credentials"
    private val backUpOptionsLabel = "\uD83D\uDCE6 Backup options"
    private val primaryActions = listOf(
        ActionButton(buttonText = "\uD83D\uDD0F Add new credentials", actionIdentifier = addActionIdentifier),
        ActionButton(buttonText = backUpOptionsLabel, actionIdentifier = backUpActionIdentifier)
    )

    init {
        logger.info("Starting PasswordServingBot")
        if (credentialsService.getAllCredentials().isEmpty()) {
            val message = "[\uD83D\uDECE\uFE0F Serving mode]\n You have zero credentials stored, you can add some clicking below \uD83D\uDC47"
            sendMessage(chatId, message, actionButtons = primaryActions)
        } else {
            val message = "[\uD83D\uDECE\uFE0F Serving mode]\nDirectly paste the websites and hit send to get your credentials!"
            sendMessage(chatId, message, actionButtons = primaryActions)
        }
    }

    override fun reactToTextRequest(request: TextRequestFromTelegram) {
        val textContent = request.textContent

        when {
            !websiteParsingService.isParseable(textContent) -> sendParsingErrorMessage(chatId)
            credentialsService.doesEntryExist(textContent) -> sendCredentialsMessage(chatId, textContent)
            else -> sendNoCredentialsMessage(chatId, textContent)
        }
    }

    private fun sendParsingErrorMessage(chatId: String) {
        sendMessage(
            chatId,
            "I cannot process this... try another URL?",
            actionButtons = primaryActions
        )
    }

    private fun sendCredentialsMessage(chatId: String, textContent: String) {
        logger.info("Found some matches!")
        val entries = credentialsService.retrieveEntriesAssociatedToUrl(textContent)
        if (entries.isEmpty()) {
            return
        }
        val timeIntervalToModify = 15;
        var formattedMessage = "Your saved passwords for this website are below, the content will disappear " +
                "in $timeIntervalToModify minutes\n"
        formattedMessage += formatCredentialsAsYaml(entries)
        val messageId = sendMessage(chatId, formattedMessage, asSpoiler = true)
        editMessage(chatId, messageId, "Nothing to see here \uD83E\uDD77\uD83C\uDFFC", timeIntervalToModify)
    }

    private fun sendNoCredentialsMessage(chatId: String, textContent: String) {
        val websiteIdentifier = websiteParsingService.extractWebsiteIdentifier(textContent)

        sendMessage(
            chatId,
            "I found no passwords for ${websiteIdentifier.wholeDomain}, " +
                    "do you want to register it? Click below \uD83D\uDC47",
            actionButtons = primaryActions
        )
    }


    override fun reactToActionRequest(request: ActionRequestFromTelegram) {
        logger.info("Received action ${request.action} from userId ${request.userId}")
        if (request.action == addActionIdentifier) {
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.ADD_CREDENTIALS)
            return
        }
        if (request.action == backUpActionIdentifier) {
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.BACKUP)
            return
        }
        sendMessage(chatId, "Sorry, that button does not work in this context", actionButtons = primaryActions)
    }

    override fun reactToReceivedFile(request: FileProvidedByTelegramUser) {
        sendMessage(
            chatId,
            "Sorry, I cannot process the file, if you want to restore backup click first on ($backUpOptionsLabel)",
            actionButtons = primaryActions
        )

    }

    private fun formatCredentialsAsYaml(entries: List<CredentialEntry>): String {
        return buildString {
            entries.forEach { entry ->
                val host = entry.host
                val username = entry.username ?: "n/a"
                val password = entry.password
                append("\uD83C\uDFE0 host: $host\n")
                append("\uD83D\uDC65 login: $username\n")
                append("\uD83D\uDD12 secret: $password\n")
                append("\n")
            }
        }
    }


}