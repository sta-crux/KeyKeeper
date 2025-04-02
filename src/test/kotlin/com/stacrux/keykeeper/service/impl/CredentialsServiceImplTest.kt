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
        val newUrl = "https://chatgpt.com/c/67bf991f-cf18" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertFalse { credentialsService.doesEntryExist(newUrl) }
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        val retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment(newUrl) }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
    }

    @Test
    fun insertThenGetShortName() {
        val newUrl = "https://www.aaa.pingpong/produits/5906669038574/some_cool_catalog_stuff.html"
        assertFalse { credentialsService.doesEntryExist(newUrl) }
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        var retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("a") }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
        retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("Aa") }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
        retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("aaA") }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
    }

    @Test
    fun insertThenGetSpecialChars() {
        val newUrl = "https://www.z-library.anycom/book/5906669038574/some_royalty_free_book.html"
        assertFalse { credentialsService.doesEntryExist(newUrl) }
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        var retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("z-library") }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
        retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("zlib") }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
        retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("lib") }
        assertEquals(1, retrievedCredentials.size)
        assertTrue { newUrl.contains(retrievedCredentials[0].host) }
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
    }

    @Test
    fun testPartialRetrieval() {
        var newUrl = "https://copilot.microsoft.com/chats/QxX94ZNz6WwHsA" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        newUrl = "https://github.com/sta-crux/KeyKeeper/" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        newUrl = "https://www.abri-plus.com/stationnement" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        newUrl = "https://www.paypal.com/fr/home" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        var retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("github") }
        assertEquals(1, retrievedCredentials.size)
        retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("abri-plus") }
        assertEquals(1, retrievedCredentials.size)
        retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("paypal") }
        assertEquals(1, retrievedCredentials.size)
        retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("PAYpAL") }
        assertEquals(1, retrievedCredentials.size)
    }

    @Test
    fun testMultipleCredentialsSameHost() {
        var newUrl = "https://copilot.microsoft.com/chats/QxX94ZNz6WwHsA" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        newUrl = "https://github.com/sta-crux/KeyKeeper/" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        newUrl = "https://www.paypal.com/fr/home" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, testLogin, testSecret) }
        newUrl = "https://www.paypal.it/fr/home" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, RandomStringUtils.randomAlphabetic(8), testSecret) }
        newUrl = "https://www.paypal.net/fr/home" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertDoesNotThrow { credentialsService.insertEntry(newUrl, RandomStringUtils.randomAlphabetic(8), testSecret) }
        val retrievedCredentials = assertDoesNotThrow { credentialsService.retrieveEntriesAssociatedToUrlFragment("paypal") }
        assertEquals(3, retrievedCredentials.size)
    }
}