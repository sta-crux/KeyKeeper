package com.stacrux.keykeeper.bot.helper

import com.stacrux.keykeeper.bot.model.ActionRequestFromTelegram
import com.stacrux.keykeeper.bot.model.FileProvidedByTelegramUser
import com.stacrux.keykeeper.bot.model.RequestFromTelegram
import com.stacrux.keykeeper.bot.model.TextRequestFromTelegram
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File

class MessageConverter {

    private val logger = LoggerFactory.getLogger(MessageConverter::class.java)

    /**
     * Convert an Update received by the Bot Library into an object of our model: RequestFromTelegram
     * supported updates are:
     * - text messages
     * - actions (button click)
     * - file
     */
    fun convertTelegramReceivedUpdate(update: Update, botToken: String): RequestFromTelegram {
        if (update.hasCallbackQuery()) {
            val callbackQuery = update.callbackQuery
            val chatId = callbackQuery.message.chatId.toString()
            val action = callbackQuery.data
            val userId = callbackQuery.from?.id ?: 0
            val username = callbackQuery.from.userName ?: "Unknown"
            logger.info("Received action from @$username - $userId: $action")
            return ActionRequestFromTelegram(chatId, userId.toString(), username, action, callbackQuery.id)
        }

        if (update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId.toString()
            val username = message.from?.userName ?: "Unknown"
            val userId = message.from?.id ?: 0

            when {
                message.hasText() -> {
                    val userMessage = message.text
                    logger.info("Received message from @$username - $userId: $userMessage")
                    return TextRequestFromTelegram(chatId, userId.toString(), username, userMessage)
                }

                message.hasDocument() -> {
                    val document = message.document
                    val downloadedFile = downloadTelegramFile(botToken, document.fileId, document.fileName)
                    logger.info("Received document from @$username - $userId: ${document.fileName}")
                    return FileProvidedByTelegramUser(chatId, userId.toString(), username, downloadedFile)
                }
            }
        }
        throw Exception("Ops, cannot convert this message")
    }


    private fun downloadTelegramFile(botToken: String, fileId: String, fileName: String): File {
        val client = OkHttpClient()

        // Get file path
        val fileInfoUrl = "https://api.telegram.org/bot$botToken/getFile?file_id=$fileId"
        val fileInfoRequest = Request.Builder().url(fileInfoUrl).build()
        val fileInfoResponse = client.newCall(fileInfoRequest).execute()
        val filePath = JSONObject(fileInfoResponse.body!!.string()).getJSONObject("result").getString("file_path")

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