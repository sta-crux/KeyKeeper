package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.model.TelegramUserDetails
import com.stacrux.keykeeper.model.TextRequestFromTelegram
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MonitoringServiceImplTest {

    private val monitoringService = ServiceProvider.getDefaultMonitoringService()

    @Test
    fun testGetWithEmptyMap() {
        val interactions = assertDoesNotThrow { monitoringService.getMessagesByUserId("anyId") }
        assertTrue { interactions.isEmpty() }
        val receivedCounts = assertDoesNotThrow { monitoringService.receivedMessageCount() }
        assertTrue { receivedCounts.isEmpty() }
    }

    @Test
    fun testInsertInteraction() {
        val userId = RandomStringUtils.randomAlphabetic(16)
        val telegramUserDetails = TelegramUserDetails(userId, "someUserName")
        assertDoesNotThrow { monitoringService.recordInteraction(telegramUserDetails, createRandomTextRequest(userId)) }
        val messages = assertDoesNotThrow { monitoringService.getMessagesByUserId(userId) }
        assertEquals(1, messages.size)
    }

    @Test
    fun testMultipleInsertInteraction() {
        val userIds: MutableList<String> = mutableListOf()
        for (i in 1..50) {
            val userId = RandomStringUtils.randomAlphabetic(16)
            userIds.add(userId)
            val telegramUserDetails = TelegramUserDetails(userId, "someUserName")
            assertDoesNotThrow {
                monitoringService.recordInteraction(
                    telegramUserDetails,
                    createRandomTextRequest(userId)
                )
            }
        }
        val messages = assertDoesNotThrow { monitoringService.getMessagesByUserId(userIds[33]) }
        assertEquals(1, messages.size)
        val messagesCount = assertDoesNotThrow { monitoringService.receivedMessageCount() }
        assertTrue { messagesCount.isNotEmpty() }
        assertEquals(50, messagesCount.values.sum(), "We have 50 interactions in total")
    }


    private fun createRandomTextRequest(userId: String): TextRequestFromTelegram {
        val chatId = RandomStringUtils.randomAlphabetic(8)
        val message = RandomStringUtils.randomAlphabetic(256)
        return TextRequestFromTelegram(chatId, userId, "Pippo999", message)
    }
}