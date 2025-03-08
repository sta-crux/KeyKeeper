package com.stacrux.keykeeper.service

import com.stacrux.keykeeper.model.RequestFromTelegram
import com.stacrux.keykeeper.model.TelegramUserDetails

/**
 * Used to retrieve recorded events/interactions
 */
interface MonitoringService {

    fun receivedMessageCount(): Map<TelegramUserDetails, Int>
    fun getMessagesByUserId(userId: String): List<RequestFromTelegram>
    fun recordInteraction(user: TelegramUserDetails, request: RequestFromTelegram)
    fun getCommandForUserCount(): String
    fun getCommandForUserMessages(): String

}