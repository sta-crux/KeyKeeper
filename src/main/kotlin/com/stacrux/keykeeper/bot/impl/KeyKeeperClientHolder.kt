package com.stacrux.keykeeper.bot.impl

import com.stacrux.keykeeper.bot.KeyKeeperClientHolder
import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.GetMe
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class KeyKeeperClientHolder(private val botToken: String) : KeyKeeperClientHolder {

    private val logger = LoggerFactory.getLogger(KeyKeeperClientHolder::class.java)
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val telegramClient = OkHttpTelegramClient(botToken)

    override fun getBotUserName(): String {
        val me: User = telegramClient.execute(GetMe())
        return me.userName
    }

    override fun sendFile(chatId: String, file: File) {
        logger.info("Sending file to chat {}: {}", chatId, file.name)
        val sendDocument = SendDocument.builder()
            .chatId(chatId)
            .document(InputFile(file))
            .build()
        telegramClient.execute(sendDocument)
    }

    override fun sendMessage(
        chatId: String,
        messageContent: String,
        asSpoiler: Boolean,
        actionButtons: ActionsButtons,
        deleteAfterMinutes: Int
    ): Int {
        require(chatId.isNotEmpty()) { "Chat Id cannot be empty" }

        val escapedMessage = escapeMarkdownV2(messageContent)
        val message = SendMessage.builder()
            .chatId(chatId)
            .text(if (asSpoiler) "||${escapedMessage}||" else escapedMessage)
            .apply { parseMode("MarkdownV2") }
            .replyMarkup(createInlineKeyboard(actionButtons.asActionButtonsList()))
            .build()

        val messageId = telegramClient.execute(message).messageId
        if (deleteAfterMinutes > 0) {
            scheduler.schedule({
                try {
                    logger.info("Deleting message {} in chat {}", messageId, chatId)
                    deleteMessage(chatId, messageId)
                } catch (t: Throwable) {
                    logger.error("I could not delete message with id {} in chat {}", messageId, chatId)
                }
            }, deleteAfterMinutes.toLong(), TimeUnit.MINUTES)
        }
        return messageId
    }

    override fun editMessage(
        chatId: String,
        messageId: Int,
        newContent: String,
        editAfterMinutes: Int
    ) {
        logger.info("Scheduling message edit in {} minutes for chat {}: {}", editAfterMinutes, chatId, newContent)

        scheduler.schedule({
            try {
                logger.info("Editing message {} in chat {}", messageId, chatId)
                val editMessage = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(escapeMarkdownV2(newContent))
                    .apply { parseMode("MarkdownV2") }
                    .build()
                telegramClient.execute(editMessage)
            } catch (t: Throwable) {
                logger.error("I could not edit message with id {} in chat {}", messageId, chatId)
            }
        }, editAfterMinutes.toLong(), TimeUnit.MINUTES)
    }


    override fun exposeTelegramClient(): TelegramClient {
        return telegramClient
    }

    override fun exposeToken(): String {
        return botToken
    }


    private fun escapeMarkdownV2(text: String): String {
        val specialChars = "_[]()~>#+-=|{}.!".toCharArray()
        return text.map { if (it in specialChars) "\\$it" else it.toString() }.joinToString("")
    }

    private fun createInlineKeyboard(actionButtons: List<ActionButton>): InlineKeyboardMarkup {
        logger.debug("Creating inline keyboard with {} buttons", actionButtons.size)
        val buttonRows = actionButtons.chunked(1).map { rowButtons ->
            InlineKeyboardRow(rowButtons.map {
                InlineKeyboardButton.builder()
                    .text(it.buttonText)
                    .callbackData(it.actionIdentifier)
                    .build()
            })
        }
        return InlineKeyboardMarkup.builder().keyboard(buttonRows).build()
    }

    private fun deleteMessage(chatId: String, messageId: Int) {
        require(chatId.isNotEmpty()) { "Chat Id cannot be empty" }

        val deleteMessage = DeleteMessage.builder()
            .chatId(chatId)
            .messageId(messageId)
            .build()

        telegramClient.execute(deleteMessage)
    }
}