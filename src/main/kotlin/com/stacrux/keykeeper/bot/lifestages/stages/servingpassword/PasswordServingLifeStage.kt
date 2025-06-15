package com.stacrux.keykeeper.bot.lifestages.stages.servingpassword

import com.stacrux.keykeeper.bot.lifestages.AbstractBotLifeStage
import com.stacrux.keykeeper.bot.model.*
import com.stacrux.keykeeper.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.model.TextRequestFromTelegram
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.WebsiteParsingService
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit


class PasswordServingLifeStage(
    token: String,
    val chatId: String,
    private val credentialsService: CredentialsService,
    private val websiteParsingService: WebsiteParsingService
) :
    AbstractBotLifeStage(token) {

    private val logger = LoggerFactory.getLogger(PasswordServingLifeStage::class.java)
    private val primaryActions = MainActionsServingStage

    private class RecordedSharedCredentials(
        val messageId: Int,
        val proposedActions: SendCopyableOrDeleteButtons,
        val timestamp: Instant
    )

    private val actionsOnSecretMessages: MutableList<RecordedSharedCredentials> = mutableListOf()

    init {
        logger.info("Starting PasswordServingBot")
        val message = if (credentialsService.getAllCredentials().isEmpty()) {
            "📭 *Empty Vault*\n\nNo credentials stored yet! Tap below to add your first one ⬇️"
        } else {
            "🔑 *Password Vault Ready*\n\nPaste any URL and hit send to retrieve your credentials! ⚡"
        }
        sendMessage(chatId, message, actionButtons = primaryActions)
    }


    override fun reactToTextRequest(request: TextRequestFromTelegram) {
        val textContent = request.textContent
        if (credentialsService.doesEntryExist(textContent)) {
            logger.info("Found some matches!")
            val entries = credentialsService.retrieveEntriesAssociatedToUrlFragment(textContent)
            val timeBeforeRedact = 5
            sendMessage(
                chatId, "I've found ${entries.size} match(es) \uD83D\uDE80\n" +
                        "You can find the matching passwords below. For security purposes, the content will redacted " +
                        "in $timeBeforeRedact minutes \uD83D\uDD76\uFE0F", deleteAfterMinutes = timeBeforeRedact
            )
            timeBeforeRedact.sendCredentialsMessage(chatId, entries)
            return
        }
        sendNoCredentialsMessage(chatId, textContent)
    }

    private fun Int.sendCredentialsMessage(chatId: String, entries: List<CredentialEntry>) {

        entries.forEach {
            val formattedMessage = formatCredentialsAsYaml(listOf(it))
            val actionsButtons = SendCopyableOrDeleteButtons(it)
            val messageId = sendMessage(chatId, formattedMessage, asSpoiler = true, actionButtons = actionsButtons)
            recordMessageForFutureInteractions(messageId, actionsButtons)
            editMessage(
                chatId,
                messageId,
                "Nothing to see here \uD83E\uDD77\uD83C\uDFFC",
                this
            )
            Thread.sleep(150) // sleep to avoid hitting the telegram limit
        }
    }

    private fun recordMessageForFutureInteractions(messageId: Int, actionsButtons: SendCopyableOrDeleteButtons) {
        actionsOnSecretMessages.add(RecordedSharedCredentials(messageId, actionsButtons, Instant.now()))
        // perform some cleanup
        actionsOnSecretMessages.removeIf { it.timestamp.isBefore(Instant.now().minus(1, ChronoUnit.DAYS)) }
    }

    private fun sendNoCredentialsMessage(chatId: String, textContent: String) {
        val websiteIdentifier = try {
            websiteParsingService.extractWebsiteIdentifier(textContent).wholeDomain
        } catch (e: Exception) {
            textContent
        }
        sendMessage(
            chatId,
            "I found no stored credentials for [${websiteIdentifier}], " +
                    "do you want to register it? Click below \uD83D\uDC47",
            actionButtons = primaryActions
        )
    }


    override fun reactToActionRequest(request: ActionRequestFromTelegram) {
        logger.info("Received action ${request.action} from userId ${request.userId}")
        if (request.action == primaryActions.getAddActionIdentifier()) {
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.ADD_CREDENTIALS)
            return
        }
        if (request.action == primaryActions.getBackUpActionIdentifier()) {
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.BACKUP)
            return
        }
        if (request.action == primaryActions.getManageCredentialsIdentifier()) {
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.MANAGE_CREDENTIALS)
            return
        }
        if ((request.action == SendCopyableOrDeleteButtons.getCopyModeActionIdentifier())) {
            val retrievedAction = actionsOnSecretMessages.find { it.messageId == request.messageId } ?: return
            val clearContent = formatCredentialsAsCopyableYaml(listOf(retrievedAction.proposedActions.credential))
            editMessage(chatId = chatId, messageId = request.messageId, clearContent)
            return
        }
        if ((request.action == SendCopyableOrDeleteButtons.getDeleteActionIdentifier())) {
            val retrievedAction = actionsOnSecretMessages.find { it.messageId == request.messageId } ?: return
            credentialsService.removeCredentials(retrievedAction.proposedActions.credential)
            this.actionsOnSecretMessages.removeIf { it.messageId == request.messageId }
            editMessage(chatId, request.messageId, "These credentials have been deleted...")
            return
        }
        sendMessage(chatId, "Sorry, that button does not work in this context", actionButtons = primaryActions)
    }

    override fun reactToReceivedFile(request: FileProvidedByTelegramUser) {
        sendMessage(
            chatId,
            "Sorry, I cannot process the file in this context, " +
                    "if you want to restore backup click first on (${primaryActions.getBackUpActionLabel()})",
            actionButtons = primaryActions
        )

    }

    private fun formatCredentials(entries: List<CredentialEntry>, copyable: Boolean = false): String {
        val hostEmoji = "\uD83C\uDFE0"
        val loginEmoji = "\uD83D\uDC65"
        val secretEmoji = "\uD83D\uDD12"

        return buildString {
            entries.forEach { entry ->
                append("$hostEmoji host: ${entry.host}\n")
                append("$loginEmoji login: ${if (copyable) "`${entry.username}`" else entry.username}\n")
                append("$secretEmoji secret: ${if (copyable) "`${entry.password}`" else entry.password}\n")
                append("\n")
            }
        }
    }

    private fun formatCredentialsAsYaml(entries: List<CredentialEntry>): String {
        return formatCredentials(entries)
    }

    private fun formatCredentialsAsCopyableYaml(entries: List<CredentialEntry>): String {
        return formatCredentials(entries, copyable = true)
    }


}