package com.stacrux.keykeeper.persistence.impl

import com.stacrux.keykeeper.model.RequestFromTelegram
import com.stacrux.keykeeper.model.TelegramUserDetails
import com.stacrux.keykeeper.persistence.InteractionsManager

object InMemoryInteractionManager : InteractionsManager {

    private val receivedContent: MutableMap<TelegramUserDetails, MutableList<RequestFromTelegram>> =
        mutableMapOf()

    override fun recordNewInteraction(userDetails: TelegramUserDetails, receivedRequest: RequestFromTelegram) {
        val requests = receivedContent.getOrPut(userDetails) { mutableListOf() }

        // Maintain a size limit for the list (max 1000 requests per user)
        if (requests.size >= 1000) {
            requests.removeAt(0)
        }
        requests.add(receivedRequest)
    }

    override fun getAllInteractions(): Map<TelegramUserDetails, List<RequestFromTelegram>> {
        return receivedContent
    }
}