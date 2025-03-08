package com.stacrux.keykeeper.service

import com.stacrux.keykeeper.model.RequestFromTelegram

/**
 * Used to retrieve recorded events/interactions
 */
interface MonitoringService {

    data class TelegramUserDetails(val userId: String, val userName: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TelegramUserDetails) return false
            return userId == other.userId && userName == other.userName
        }

        override fun hashCode(): Int {
            return 31 * userId.hashCode() + userName.hashCode()
        }
    }

    fun receivedMessageCount(): Map<TelegramUserDetails, Int>
    fun getMessagesByUserId(userId: String): List<RequestFromTelegram>
    fun recordInteraction(user: TelegramUserDetails, request: RequestFromTelegram)
    fun getCommandForUserCount(): String
    fun getCommandForUserMessages(): String

}