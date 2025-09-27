package com.stacrux.keykeeper.bot

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.bot.model.ActionsButtons
import com.stacrux.keykeeper.model.*
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File

/**
 * Abstract class representing a bot life stage.
 * Extend this class to create specific life stages.
 */
abstract class AbstractBotLifeStage(
    private val keyKeeperClientHolder: KeyKeeperClientHolder
) : BotLifeStage {

    private val logger = LoggerFactory.getLogger(AbstractBotLifeStage::class.java)

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
                    MessageConverter.formatMessageForAllUserRequests(requestsByUserId)
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
        keyKeeperClientHolder.exposeTelegramClient().execute(answerCallback)
    }

    private fun parseRequest(update: Update): RequestFromTelegram? {
        return try {
            logger.debug("Parsing update to request")
            MessageConverter.convertTelegramReceivedUpdate(update, keyKeeperClientHolder.exposeToken())
        } catch (e: Exception) {
            logger.error("Failed to convert update to request", e)
            null
        }
    }

    override fun sendFile(chatId: String, file: File) {
        keyKeeperClientHolder.sendFile(chatId, file)
    }

    override fun sendMessage(
        chatId: String,
        messageContent: String,
        asSpoiler: Boolean,
        actionButtons: ActionsButtons,
        deleteAfterMinutes: Int
    ): Int {
        return keyKeeperClientHolder.sendMessage(chatId, messageContent, asSpoiler, actionButtons, deleteAfterMinutes)
    }

    override fun editMessage(
        chatId: String,
        messageId: Int,
        newContent: String,
        editAfterMinutes: Int
    ) {
        keyKeeperClientHolder.editMessage(chatId, messageId, newContent, editAfterMinutes)
    }

    private fun canAnswerToUser(userId: String): Boolean {
        val canAnswer = ServiceProvider.getKeyKeeperService().canAnswer(userId)
        logger.info("Checking if bot can answer user {}: {}", userId, canAnswer)
        return canAnswer
    }

}