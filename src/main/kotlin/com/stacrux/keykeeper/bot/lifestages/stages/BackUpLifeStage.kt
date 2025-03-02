package com.stacrux.keykeeper.bot.lifestages.stages

import com.stacrux.keykeeper.bot.lifestages.AbstractBotLifeStage
import com.stacrux.keykeeper.bot.model.*
import com.stacrux.keykeeper.service.BackUpService
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.SessionService
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import java.io.File

class BackUpLifeStage(
    token: String,
    val chatId: String,
    private val backUpService: BackUpService,
    private val credentialsService: CredentialsService,
    private val sessionService: SessionService
) :
    AbstractBotLifeStage(token) {

    private val logger = LoggerFactory.getLogger(BackUpLifeStage::class.java)

    private val abandonActionIdentifier = "abandon_action"
    private val backUpActionIdentifier = "backup_credentials"
    private val backUpActionLabel = "\uD83D\uDCBE Perform backup"
    private val toggleStatefulMode = "stateful_toggle"
    private val primaryActions = listOf(
        ActionButton("\uD83D\uDD19 Go back", abandonActionIdentifier),
        ActionButton(buttonText = backUpActionLabel, actionIdentifier = backUpActionIdentifier),
        ActionButton(
            buttonText = "\uD83D\uDDC4\uFE0F Enable/Disable bot local backup",
            actionIdentifier = toggleStatefulMode
        ),
    )

    private var receivedPasswordsBackup: File? = null

    init {
        defaultMessage()
    }

    override fun reactToTextRequest(request: TextRequestFromTelegram) {
        if (receivedPasswordsBackup == null) {
            defaultMessage()
            return
        }
        attemptImportWithReceivedPassword(request.textContent)
    }

    private fun attemptImportWithReceivedPassword(receivedPassword: String) {
        val file = receivedPasswordsBackup ?: return
        val credentials = try {
            backUpService.importEncryptedBackUpFile(file, receivedPassword)
        } catch (e: Exception) {
            sendMessage(
                chatId,
                "The provide password did not work...",
                actionButtons = primaryActions
            )
            return
        }
        credentialsService.insertEntries(credentials)
        sendMessage(
            chatId,
            "Done, imported ${credentials.size} entries.",
            actionButtons = primaryActions
        )
    }

    override fun reactToActionRequest(request: ActionRequestFromTelegram) {
        when (request.action) {
            backUpActionIdentifier -> sendBackup(request)
            abandonActionIdentifier -> abandonProcess()
            toggleStatefulMode -> toggleLocalBackUps()
            else -> sendMessage(
                request.chatId,
                "Sorry, that button does not work in this context",
                actionButtons = primaryActions
            )
        }
    }

    private fun toggleLocalBackUps() {
        if (!sessionService.doesSessionExist()) {
            sessionService.createSession(chatId)
        }

        val isStoring = sessionService.isStoringBackUps()
        val newIsStoring = !isStoring
        sessionService.toggleLocalBackUpStoring(newIsStoring)

        val message = if (newIsStoring) {
            "[\uD83D\uDD34 Local backup disabled] I will only send you the backup files without keeping any copy on my end."
        } else {
            "[\uD83D\uDFE2 Local backup enabled] Every time you create a backup, a copy will be registered on my end as well. " +
                    "If I reboot, you'll have to send the password to unlock it."
        }

        sendMessage(chatId, message, actionButtons = primaryActions)
    }


    override fun reactToReceivedFile(request: FileProvidedByTelegramUser) {
        logger.info("Received a file from the user")
        val downloadedFile = request.downloadedFile
        if (!backUpService.isValidBackUpFile(downloadedFile)) {
            sendMessage(
                request.chatId,
                "Sorry, this file is not valid, send me a zip",
                actionButtons = primaryActions
            )
            return
        }
        if (!backUpService.isProtectedBackUpFile(downloadedFile)) {
            val credentials = backUpService.importPlainBackUpFile(downloadedFile)
            credentialsService.insertEntries(credentials)
            sendMessage(
                chatId,
                "Done, imported ${credentials.size} entries.",
                actionButtons = primaryActions
            )
            return
        }
        this.receivedPasswordsBackup = downloadedFile
        sendMessage(
            request.chatId,
            "Password protected backup received, please send me the password to open it",
            actionButtons = primaryActions
        )
    }

    private fun sendBackup(request: ActionRequestFromTelegram) {
        val encryptionKey = RandomStringUtils.randomAlphanumeric(16)
        val backUpTag = RandomStringUtils.randomAlphanumeric(16)
        sendMessage(
            request.chatId, "Store this secret in your Saved Messages, then delete it from here.\n" +
                    "\uD83C\uDFF7\uFE0F backup file tag: $backUpTag\n" +
                    "\uD83D\uDD11 backup file password: $encryptionKey"
        )
        val backUpFile = backUpService.createBackUpFile(
            credentialsService.getAllCredentials(),
            backUpTag,
            encryptionKey,
            request.userId
        )
        if (!sessionService.isStoringBackUps()) {
            sessionService.storeBackUpFile(backUpFile, backUpTag)
        }
        sendFile(request.chatId, backUpFile)
    }

    private fun abandonProcess() {
        logger.info("Sure! Back to serving known passwords")
        getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
    }

    private fun defaultMessage() {
        sendMessage(
            chatId,
            "When you click [$backUpActionLabel] I will send you a backup file (protected by a password), " +
                    "along with the password I have used to lock it (in a separate message). " +
                    "If the local bot backup feature is active, I'll save the backup on my end as well, in case of " +
                    "problems I might ask you to provide me the password to unlock it." +
                    "\nIf you want to import an old file, you can send me one and I'll import it." +
                    "\nA backup file is a zip wrapping a CSV, containing 3 columns: host, username, password.",
            actionButtons = primaryActions
        )
    }
}