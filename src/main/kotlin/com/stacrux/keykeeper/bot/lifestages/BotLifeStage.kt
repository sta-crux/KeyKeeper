package com.stacrux.keykeeper.bot.lifestages

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.bot.KeyKeeper
import com.stacrux.keykeeper.bot.model.ActionsButtons
import com.stacrux.keykeeper.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.model.TextRequestFromTelegram
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import java.io.File

interface BotLifeStage : LongPollingSingleThreadUpdateConsumer {

    /**
     * Override this into your implementations to perform actions after having received some text
     */
    fun reactToTextRequest(request: TextRequestFromTelegram)

    /**
     * Override this into your implementations to perform actions after having received an action
     * (button click)
     */
    fun reactToActionRequest(request: ActionRequestFromTelegram)

    /**
     * Override this into your implementations to perform actions after having received a file
     */
    fun reactToReceivedFile(request: FileProvidedByTelegramUser)

    /**
     * By calling this method, a message is sent to a chat identified by chatId
     * @param chatId identifies the chat that will receive the message
     * @param asSpoiler when true, send a spoiler message (concealed unless clicked)
     * @param actionButtons list of buttons that are displayed after the message is sent
     * @param deleteAfterMinutes only when greater than 0, the message will be deleted after the provided amount of time
     * @returns the message id of the sent message
     */
    fun sendMessage(
        chatId: String,
        messageContent: String,
        asSpoiler: Boolean = false,
        actionButtons: ActionsButtons = ActionsButtons.EmptyActionButtons,
        deleteAfterMinutes: Int = 0
    ): Int

    /**
     * By calling this method, an existing message is edited
     * @param chatId identifies the chat that will receive the message
     * @param messageId id of the message ti edit
     * @param newContent new text content that replaces the previous one
     * @param editAfterMinutes delay to perform the edit
     */
    fun editMessage(
        chatId: String,
        messageId: Int,
        newContent: String,
        editAfterMinutes: Int = 0 // by default instantly edit){}
    )

    /**
     * By calling this method, a file is sent to the chat (determined by the chatId)
     * @param chatId identifies the chat that will receive the message
     * @param file the file to share
     */
    fun sendFile(chatId: String, file: File)

    /**
     * Return the main context, where the user id (which correspond to the chat id) is stored.
     */
    fun getKeyKeeper(): KeyKeeper {
        return ServiceProvider.getKeyKeeperService()
    }
}