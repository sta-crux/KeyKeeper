package com.stacrux.keykeeper.persistence

import com.stacrux.keykeeper.TestsSetup
import com.stacrux.keykeeper.model.CredentialEntry
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CredentialsInMemoryObjTest : TestsSetup() {

    private val credentialsManager: CredentialsManager = super.subServiceProvider.getDefaultCredentialsManager()
    private val testLogin = "login"
    private val testSecret = "secret"

    @Test
    fun insertThenGet() {
        val newHost = "some.test.host." + RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertFalse { credentialsManager.doesHostExist(newHost) }
        val credentialEntry = CredentialEntry(newHost, testSecret, testLogin)
        assertDoesNotThrow { credentialsManager.registerNewValue(credentialEntry) }
        val retrievedCredentials = assertDoesNotThrow { credentialsManager.getCredentialsForHost(newHost) }
        assertEquals(1, retrievedCredentials.size)
        assertEquals(newHost, retrievedCredentials[0].host)
        assertEquals(testLogin, retrievedCredentials[0].username)
        assertEquals(testSecret, retrievedCredentials[0].password)
    }

}