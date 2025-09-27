package com.stacrux.keykeeper.bot.impl.lifestages.restoresession

import com.stacrux.keykeeper.bot.AbstractBotLifeStage
import com.stacrux.keykeeper.bot.KeyKeeperClientHolder
import com.stacrux.keykeeper.bot.model.*
import com.stacrux.keykeeper.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.model.TextRequestFromTelegram
import com.stacrux.keykeeper.service.BackUpService
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.SessionService
import java.io.File

class RestoreSessionLifeStage(
    keyKeeperClientHolder: KeyKeeperClientHolder,
    val chatId: String,
    private val credentialsService: CredentialsService,
    private val sessionService: SessionService,
    private val backUpService: BackUpService,
    private var backUpFile: File?
) :
    AbstractBotLifeStage(keyKeeperClientHolder) {

    private val primaryActions = ForgetPreviousSession

    init {
        defaultMessage()
    }

    override fun reactToTextRequest(request: TextRequestFromTelegram) {
        if (backUpFile != null) {
            try {
                attemptImportWithReceivedPassword(request.textContent)
                getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
                return
            } catch (e: Exception) {
                return
            }
        } else {
            defaultMessage()
        }
    }

    private fun attemptImportWithReceivedPassword(receivedPassword: String) {
        val file = backUpFile ?: return
        val credentials = try {
            backUpService.importEncryptedBackUpFile(file, receivedPassword)
        } catch (e: Exception) {
            sendMessage(
                chatId,
                "The provide password did not work..."
            )
            throw Exception("Invalid Password")
        }
        credentialsService.insertEntries(credentials)
        sendMessage(
            chatId,
            "The password worked! Now delete it from this chat, store it in a safe space! " +
                    "I have imported ${credentials.size} entries."
        )
    }


    override fun reactToActionRequest(request: ActionRequestFromTelegram) {
        if (request.action == primaryActions.getForgetSessionActionId()) {
            sendMessage(
                chatId,
                "Alright, I'll ignore the previous session and stop storing backups, " +
                        "you can re-enable them in the backup options"
            )
            sessionService.disableSession()
            getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
        }
        return
    }

    override fun reactToReceivedFile(request: FileProvidedByTelegramUser) {
        val downloadedFile = request.downloadedFile
        val supportedFileType = try {
            backUpService.inspectProvidedImportFile(downloadedFile)
        } catch (e: Exception) {
            sendMessage(
                request.chatId,
                "Sorry, this file is not valid, send me a zip or a well formed csv (supported exports: Firefox)",
                actionButtons = primaryActions
            )
        }
        if (!backUpService.isProtectedBackUpFile(downloadedFile)) {
            val credentials = backUpService.importPlainBackUpFile(downloadedFile)
            credentialsService.insertEntries(credentials)
            sendMessage(
                chatId,
                "Done, imported ${credentials.size} entries.",
                actionButtons = primaryActions
            )
            super.getKeyKeeper().advanceBotLifeStage(chatId, BotRunningState.SERVING)
            return
        }
        this.backUpFile = downloadedFile
        sendMessage(
            request.chatId,
            "Password protected backup received, please send me the password to open it",
            actionButtons = primaryActions
        )
    }


    private fun defaultMessage() {
        val message = buildString {
            append("⚡ *KeyKeeper has restarted...*\n\n")
            if (backUpFile == null) {
                append("❌ No backup found on my end.\n")
                append("📤 Please send me a backup file if you’d like me to restore your credentials.")
            } else {
                val backUpTag = sessionService.retrieveLastKnownBackUpTag()
                append("✅ Backup detected! 🏷️ *Tag*: `$backUpTag`\n")
                append("🔑 I’ll need the key to unlock it — could you share it?")
            }
        }
        sendMessage(chatId, message, actionButtons = primaryActions)
    }

}