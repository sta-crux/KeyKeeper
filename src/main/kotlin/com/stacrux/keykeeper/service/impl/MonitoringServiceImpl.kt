package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.model.RequestFromTelegram
import com.stacrux.keykeeper.model.TelegramUserDetails
import com.stacrux.keykeeper.persistence.InteractionsManager
import com.stacrux.keykeeper.service.MonitoringService

class MonitoringServiceImpl(private val interactionsManager: InteractionsManager) : MonitoringService {


    override fun receivedMessageCount(): Map<TelegramUserDetails, Int> {
        return interactionsManager.getAllInteractions().mapValues { it.value.size }
    }

    override fun getMessagesByUserId(userId: String): List<RequestFromTelegram> {
        return interactionsManager.getAllInteractions().entries
            .find { it.key.userId == userId }
            ?.value ?: emptyList()
    }

    override fun recordInteraction(user: TelegramUserDetails, request: RequestFromTelegram) {
        interactionsManager.recordNewInteraction(user, request)
    }

    override fun getCommandForUserCount(): String {
        return "monitoring count"
    }

    override fun getCommandForUserMessages(): String {
        return "monitoring messages"
    }
}
