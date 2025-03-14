package com.stacrux.keykeeper.bot

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File

object MessageConverter {

    private val logger = LoggerFactory.getLogger(MessageConverter::class.java)

    fun formatMessageCount(map: Map<TelegramUserDetails, Int>): String {
        return buildString {
            appendLine("```")
            appendLine("Count\t|\tUserId\t|\tUserName\t|\tget_messages")
            map.entries
                .sortedByDescending { it.value }
                .forEach { (userDetails, count) ->
                    appendLine(
                        "$count\t|\t${userDetails.userId}\t|\t${userDetails.userName}\t|\t" +
                                ServiceProvider.getDefaultMonitoringService().getCommandForUserMessages() +
                                " ${userDetails.userId}"
                    )
                }
            appendLine("```")
        }
    }

    fun formatRequests(requestsByUserId: List<RequestFromTelegram>): String {
        return buildString {
            appendLine("```")
            requestsByUserId
                .forEach {
                    appendLine(
                        when (it) {
                            is TextRequestFromTelegram -> "received_request: message\ncontent: ${it.textContent}"
                            is FileProvidedByTelegramUser -> "received_request: file\nfile_name: ${it.downloadedFile.name}"
                            is ActionRequestFromTelegram -> "received_request: action\naction_name: ${it.action}"
                            else -> "received_request: unknown" // fallback in case of unexpected request types
                        }
                    )
                    appendLine()
                }
            appendLine("```")
        }
    }


    /**
     * Convert an Update received by the Bot Library into an object of our model: RequestFromTelegram
     * supported updates are:
     * - text messages
     * - actions (button click)
     * - file
     */
    fun convertTelegramReceivedUpdate(update: Update, botToken: String): RequestFromTelegram {

        val defaultMonitoringService = ServiceProvider.getDefaultMonitoringService()
        fun recordInteraction(userId: String, username: String, request: RequestFromTelegram) {
            defaultMonitoringService.recordInteraction(TelegramUserDetails(userId, username), request)
        }

        // Handle callback query
        if (update.hasCallbackQuery()) {
            val callbackQuery = update.callbackQuery
            val userId = callbackQuery.from?.id?.toString() ?: "0"
            val username = callbackQuery.from?.userName ?: "Unknown"
            val chatId = callbackQuery.message.chatId.toString()
            val action = callbackQuery.data
            val messageId = callbackQuery.message.messageId
            logger.info("Received action from @$username - $userId: $action")

            val actionRequest = ActionRequestFromTelegram(
                chatId, userId, username, action, callbackQuery.id, messageId
            )
            recordInteraction(userId, username, actionRequest)
            return actionRequest
        }

        // Handle regular message
        if (update.hasMessage()) {
            val message = update.message
            val userId = message.from?.id?.toString() ?: "0"
            val username = message.from?.userName ?: "Unknown"
            val chatId = message.chatId.toString()

            when {
                message.hasDocument() -> {
                    val document = message.document
                    val downloadedFile = downloadTelegramFile(botToken, document.fileId, document.fileName)
                    logger.info("Received document from @$username - $userId: ${document.fileName}")
                    val fileRequest = FileProvidedByTelegramUser(chatId, userId, username, downloadedFile)
                    recordInteraction(userId, username, fileRequest)
                    return fileRequest
                }

                message.hasText() -> {
                    val userMessage = message.text
                    logger.info("Received message from @$username - $userId: $userMessage")
                    if (userMessage.contains(defaultMonitoringService.getCommandForUserCount())) {
                        return buildCountMonitoringRequest(chatId, userId, username)
                    }
                    if (userMessage.contains(defaultMonitoringService.getCommandForUserMessages())) {
                        val split = userMessage.split(" ")
                        if (split.size < 3) {
                            throw Exception("Ops, cannot convert this message")
                        }
                        val targetUserId = split[2]
                        return buildGetMessagesMonitoringRequest(chatId, userId, username, targetUserId)
                    }
                    val textRequest = TextRequestFromTelegram(chatId, userId, username, userMessage)
                    recordInteraction(userId, username, textRequest)
                    return textRequest
                }
            }
        }

        throw Exception("Ops, cannot convert this message")
    }

    fun getUserIdFromUpdate(update: Update): String {
        if (update.hasCallbackQuery()) {
            val callbackQuery = update.callbackQuery
            return callbackQuery.from?.id?.toString() ?: "0"
        }
        if (update.hasMessage()) {
            val message = update.message
            return message.from?.id?.toString() ?: "0"
        }
        throw Exception("Cannot extract user id from request")
    }

    private fun buildGetMessagesMonitoringRequest(
        chatId: String,
        userId: String,
        username: String,
        targetUserId: String
    ): RequestFromTelegram {
        val requestType = MonitoringRequestFromTelegram.MonitoringRequest.MonitoringRequestType.REQUESTS
        val request = MonitoringRequestFromTelegram.MonitoringRequest(targetUserId, requestType)
        return MonitoringRequestFromTelegram(
            chatId = chatId,
            userId = userId,
            userName = username,
            monitoringRequest = request
        )
    }

    private fun buildCountMonitoringRequest(
        chatId: String,
        userId: String,
        username: String
    ): MonitoringRequestFromTelegram {
        val requestType = MonitoringRequestFromTelegram.MonitoringRequest.MonitoringRequestType.COUNT
        val request = MonitoringRequestFromTelegram.MonitoringRequest("n/a", requestType)
        return MonitoringRequestFromTelegram(
            chatId = chatId,
            userId = userId,
            userName = username,
            monitoringRequest = request
        )
    }


    private fun downloadTelegramFile(botToken: String, fileId: String, fileName: String): File {
        val client = OkHttpClient()

        // Get file path and metadata
        val fileInfoUrl = "https://api.telegram.org/bot$botToken/getFile?file_id=$fileId"
        val fileInfoRequest = Request.Builder().url(fileInfoUrl).build()
        val fileInfoResponse = client.newCall(fileInfoRequest).execute()
        val fileInfoJson = JSONObject(fileInfoResponse.body!!.string())
        val result = fileInfoJson.getJSONObject("result")
        val filePath = result.getString("file_path")
        val fileSize = result.optLong("file_size", -1L) // Use `-1L` if `file_size` is not available

        // Check file size limit (100 MB = 100 * 1024 * 1024 bytes)
        val maxFileSize = 100 * 1024 * 1024
        if (fileSize > maxFileSize) {
            throw IllegalStateException("File size exceeds 100 MB limit: ${fileSize / (1024 * 1024)} MB")
        }

        // Download file
        val fileUrl = "https://api.telegram.org/file/bot$botToken/$filePath"
        val fileRequest = Request.Builder().url(fileUrl).build()
        val fileResponse = client.newCall(fileRequest).execute()
        val fileBytes = fileResponse.body!!.bytes()

        // Save file locally
        val file = File(fileName)
        file.writeBytes(fileBytes)
        return file
    }


}