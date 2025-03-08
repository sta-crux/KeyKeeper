package com.stacrux.keykeeper.model

class MonitoringRequestFromTelegram(
    chatId: String,
    userId: String,
    userName: String,
    val monitoringRequest: MonitoringRequest
) : RequestFromTelegram(
    chatId, userId,
    userName
) {
    class MonitoringRequest(val requestedUserId: String, val requestType: MonitoringRequestType) {
        enum class MonitoringRequestType {
            COUNT,
            REQUESTS
        }
    }

}