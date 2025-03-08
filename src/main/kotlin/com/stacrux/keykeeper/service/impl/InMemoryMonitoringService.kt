package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.model.RequestFromTelegram
import com.stacrux.keykeeper.service.MonitoringService

object InMemoryMonitoringService : MonitoringService {

    private val receivedContent: MutableMap<MonitoringService.TelegramUserDetails, MutableList<RequestFromTelegram>> =
        mutableMapOf()

    override fun receivedMessageCount(): Map<MonitoringService.TelegramUserDetails, Int> {
        return receivedContent.mapValues { it.value.size }
    }

    override fun getMessagesByUserId(userId: String): List<RequestFromTelegram> {
        return receivedContent.entries
            .find { it.key.userId == userId }
            ?.value ?: emptyList()
    }

    override fun recordInteraction(user: MonitoringService.TelegramUserDetails, request: RequestFromTelegram) {
        val requests = receivedContent.getOrPut(user) { mutableListOf() }

        // Maintain a size limit for the list (max 1000 requests per user)
        if (requests.size >= 1000) {
            requests.removeAt(0)
        }
        requests.add(request)
    }

    override fun getCommandForUserCount(): String {
        return "monitoring count"
    }

    override fun getCommandForUserMessages(): String {
        return "monitoring messages"
    }
}
