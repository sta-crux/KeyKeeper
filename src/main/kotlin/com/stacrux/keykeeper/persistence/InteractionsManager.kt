package com.stacrux.keykeeper.persistence

import com.stacrux.keykeeper.model.RequestFromTelegram
import com.stacrux.keykeeper.model.TelegramUserDetails

interface InteractionsManager {

    fun recordNewInteraction(userDetails: TelegramUserDetails, receivedRequest: RequestFromTelegram)
    fun getAllInteractions(): Map<TelegramUserDetails, List<RequestFromTelegram>>

}