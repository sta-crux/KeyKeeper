package com.stacrux.keykeeper.bot

import com.stacrux.keykeeper.bot.model.ActionsButtons
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.File

interface KeyKeeperClientHolder {

    /**
     * returns the bot username from telegram servers
     */
    fun getBotUserName(): String

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
     * For advanced uses not available here (send message, send file etc), the telegram client can be retrieved calling this
     */
    fun exposeTelegramClient(): TelegramClient

    /**
     * For advanced uses, exposing the token
     */
    fun exposeToken(): String
}