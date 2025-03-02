package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.ServiceProvider
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CredentialsServiceImplTest {

    private val credentialsService = ServiceProvider.getDefaultCredentialsService()
    private val testLogin = "login"
    private val testSecret = "secret"

    @Test
    fun insertThenGet() {
        val newUrl = "https://chatgpt.com/c/67bf991f-cf18-8000-8461-d4dde0cb0d40" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertFalse { credentialsService.doesEntryExist(newUrl) }
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        val retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrl(newUrl) }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
    }
}