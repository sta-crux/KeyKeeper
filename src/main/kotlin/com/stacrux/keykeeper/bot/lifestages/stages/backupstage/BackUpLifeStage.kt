package com.stacrux.keykeeper.bot.lifestages.stages.backupstage

import com.stacrux.keykeeper.bot.lifestages.AbstractBotLifeStage
import com.stacrux.keykeeper.bot.model.*
import com.stacrux.keykeeper.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.model.TextRequestFromTelegram
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
    private val primaryActions = AbandonOrBackUpOrToggleAutoBackUp

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
            primaryActions.getBackUpActionIdentifier() -> sendBackup(request)
            primaryActions.getAbandonActionIdentifier() -> abandonProcess()
            primaryActions.getToggleStatefulModeActionIdentifier() -> toggleLocalBackUps()
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

        val newIsStoring = !sessionService.isStoringBackUps()
        sessionService.toggleLocalBackUpStoring(newIsStoring)

        val message = if (newIsStoring) {
            "🟢 *Local Backup Enabled*\n\nEvery time you create a backup, a copy will be stored securely on my side. If I reboot, you'll need to provide the password to unlock it."
        } else {
            "🔴 *Local Backup Disabled*\n\nFrom now on, I'll only send you the backup files — no copies will be kept on my end."
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
        val timeBeforeDelete = 10
        sendMessage(
            request.chatId,
            "Store this secret in your Saved Messages, I'll delete it from here in ${timeBeforeDelete}.\n" +
                    "\uD83C\uDFF7\uFE0F backup file tag: `$backUpTag`\n" +
                    "\uD83D\uDD11 backup file password: `$encryptionKey`",
            deleteAfterMinutes = timeBeforeDelete
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
        getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
    }

    private fun defaultMessage() {
        val isLocalBackupEnabled = if (sessionService.isStoringBackUps()) "🟢 *Enabled*" else "🔴 *Disabled*"

        sendMessage(
            chatId,
            """
        📦 *Backup & Import Guide*  
        
        🛡️ Click *[${primaryActions.getBackUpActionLabel()}]* to receive a backup file (password-protected), along with the password in a separate message.  
        
        🔐 *Local Backup:* $isLocalBackupEnabled  
        If enabled, I’ll securely store a copy of each backup on my side too. In case of issues, I might ask you for the password to unlock it.  
        
        📥 Want to import an old backup? Just send me the file, and I’ll take care of it!  
        
        📋 *Backup Structure*:  
        Each backup is a ZIP containing a CSV with 3 columns: `host`, `username`, `password`.
        """.trimIndent(),
            actionButtons = primaryActions
        )
    }



}