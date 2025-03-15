package com.stacrux.keykeeper.bot.lifestages

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.bot.MessageConverter
import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons
import com.stacrux.keykeeper.model.*
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
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
abstract class AbstractBotLifeStage(private val botToken: String) : BotLifeStage {

    private val logger = LoggerFactory.getLogger(AbstractBotLifeStage::class.java)
    private val telegramClient: TelegramClient = OkHttpTelegramClient(botToken)
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    override fun consume(update: Update) {
        logger.info("Received update: {}", update)

        if (!canAnswerToUser(MessageConverter.getUserIdFromUpdate(update))) return
        // parse only authorized requests
        val request = parseRequest(update) ?: return

        if (request is MonitoringRequestFromTelegram) {
            handleMonitoringRequest(request)
            return
        }

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

    private fun handleMonitoringRequest(request: MonitoringRequestFromTelegram) {
        val defaultMonitoringService = ServiceProvider.getDefaultMonitoringService()
        when (request.monitoringRequest.requestType) {
            MonitoringRequestFromTelegram.MonitoringRequest.MonitoringRequestType.COUNT -> {
                val message = MessageConverter.formatMessageCount(defaultMonitoringService.receivedMessageCount())
                sendMessage(chatId = request.chatId, message)
            }

            MonitoringRequestFromTelegram.MonitoringRequest.MonitoringRequestType.REQUESTS -> {
                val requestsByUserId =
                    defaultMonitoringService.getMessagesByUserId(request.monitoringRequest.requestedUserId)
                sendMessage(
                    chatId = request.chatId,
                    MessageConverter.formatRequests(requestsByUserId)
                )
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
            MessageConverter.convertTelegramReceivedUpdate(update, botToken)
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

    private fun escapeMarkdownV2(text: String): String {
        val specialChars = "_[]()~>#+-=|{}.!".toCharArray()
        return text.map { if (it in specialChars) "\\$it" else it.toString() }.joinToString("")
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


    private fun deleteMessage(chatId: String, messageId: Int) {
        require(chatId.isNotEmpty()) { "Chat Id cannot be empty" }

        val deleteMessage = DeleteMessage.builder()
            .chatId(chatId)
            .messageId(messageId)
            .build()

        telegramClient.execute(deleteMessage)
    }

}
