package com.stacrux.keykeeper.bot.lifestages

import ch.qos.logback.classic.pattern.MessageConverter
import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.bot.model.*
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Abstract class representing a bot life stage.
 * Extend this class to create specific life stages.
 */
abstract class AbstractBotLifeStage(val botToken: String) : BotLifeStage {

    private val logger = LoggerFactory.getLogger(AbstractBotLifeStage::class.java)
    private val telegramClient: TelegramClient = OkHttpTelegramClient(botToken)
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    override fun consume(update: Update) {
        logger.info("Received update: {}", update)
        val request = parseRequest(update) ?: return
        if (!canAnswerToUser(request.userId)) return

        when (request) {
            is ActionRequestFromTelegram -> {
                logger.info("Processing action request: {}", request)
                reactToActionRequest(request)
                acknowledgeClick(request)
            }
            is FileProvidedByTelegramUser -> {
                logger.info("Processing file request: {}", request)
                reactToReceivedFile(request)
            }
            is TextRequestFromTelegram -> {
                logger.info("Processing text request: {}", request)
                reactToTextRequest(request)
            }
        }
    }

    /**
     * Acknowledge the interaction to make the animation stop in telegram
     */
    private fun acknowledgeClick(request: ActionRequestFromTelegram) {
        logger.info("Acknowledging click for action: {}", request.actionIdentifier)
        val answerCallback = AnswerCallbackQuery.builder()
            .callbackQueryId(request.actionIdentifier)
            .showAlert(false)
            .build()
        telegramClient.execute(answerCallback)
    }

    private fun parseRequest(update: Update): RequestFromTelegram? {
        return try {
            logger.debug("Parsing update to request")
            MessageConverter().convertTelegramReceivedUpdate(update, botToken)
        } catch (e: Exception) {
            logger.error("Failed to convert update to request", e)
            null
        }
    }

    override fun sendFile(chatId: String, file: File) {
        logger.info("Sending file to chat {}: {}", chatId, file.name)
        val sendDocument = SendDocument.builder()
            .chatId(chatId)
            .document(InputFile(file))
            .build()
        telegramClient.execute(sendDocument)
    }

    override fun sendMessage(chatId: String, messageContent: String, asSpoiler: Boolean, actionButtons: List<ActionButton>): Int {
        require(chatId.isNotEmpty()) { "Chat Id cannot be empty" }

        val message = SendMessage.builder()
            .chatId(chatId)
            .text(if (asSpoiler) "||${escapeMarkdownV2(messageContent)}||" else messageContent)
            .apply { if (asSpoiler) parseMode("MarkdownV2") }
            .replyMarkup(createInlineKeyboard(actionButtons))
            .build()

        return telegramClient.execute(message).messageId
    }

    override fun editMessage(chatId: String, messageId: Int, newContent: String, editAfterMinutes: Int) {
        logger.info("Scheduling message edit in {} minutes for chat {}: {}", editAfterMinutes, chatId, newContent)
        scheduler.schedule({
            logger.info("Editing message {} in chat {}: {}", messageId, chatId, newContent)
            val editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(newContent)
                .build()
            telegramClient.execute(editMessage)
        }, editAfterMinutes.toLong(), TimeUnit.MINUTES)
    }

    private fun createInlineKeyboard(actionButtons: List<ActionButton>): InlineKeyboardMarkup {
        logger.debug("Creating inline keyboard with {} buttons", actionButtons.size)
        val buttonRows = actionButtons.chunked(2).map { rowButtons ->
            InlineKeyboardRow(rowButtons.map {
                InlineKeyboardButton.builder()
                    .text(it.buttonText)
                    .callbackData(it.actionIdentifier)
                    .build()
            })
        }
        return InlineKeyboardMarkup.builder().keyboard(buttonRows).build()
    }

    private fun canAnswerToUser(userId: String): Boolean {
        val canAnswer = ServiceProvider.getKeyKeeperService().canAnswer(userId)
        logger.info("Checking if bot can answer user {}: {}", userId, canAnswer)
        return canAnswer
    }

    private fun escapeMarkdownV2(text: String): String {
        val specialChars = "_*[]()~`>#+-=|{}.!".toCharArray()
        return text.map { if (it in specialChars) "\\$it" else it.toString() }.joinToString("")
    }
}
