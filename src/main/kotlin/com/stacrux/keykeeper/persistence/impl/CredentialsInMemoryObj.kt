package com.stacrux.keykeeper.persistence.impl

import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.persistence.CredentialsManager

object CredentialsInMemoryObj : CredentialsManager {

    private val inMemoryCredentials: MutableList<CredentialEntry> =
        mutableListOf()

    override fun getCredentialsForHost(host: String): List<CredentialEntry> {
        fun extractLetters(input: String) = input.lowercase().filter { it.isLetter() }

        val lowerHost = host.lowercase()
        val lettersOnlyHost = extractLetters(host)

        return inMemoryCredentials.filter { credential ->
            val storedHost = credential.host.lowercase()
            val lettersOnlyCredentialHost = extractLetters(credential.host)
            val basicMatch =
                storedHost == lowerHost ||
                storedHost.contains(lowerHost) ||
                (storedHost.length > 2 && lowerHost.contains(storedHost))
            val alphabeticMatch =
                lettersOnlyHost.isNotEmpty() &&
                lettersOnlyCredentialHost.isNotEmpty() &&
                (lettersOnlyCredentialHost.contains(lettersOnlyHost) ||
                (storedHost.length > 3 && lettersOnlyHost.contains(lettersOnlyCredentialHost)))

            basicMatch || alphabeticMatch
        }
    }


    override fun doesHostExist(host: String): Boolean {
        val normalizedHost = host.lowercase() // Normalize input to lowercase
        return inMemoryCredentials
            .map { it.host.lowercase() }
            .contains(normalizedHost)
    }

    override fun registerNewValue(credentialEntry: CredentialEntry) {
        inMemoryCredentials.removeIf {
            it.host == credentialEntry.host &&
                    it.username == credentialEntry.username
        }
        inMemoryCredentials.add(credentialEntry)
    }

    override fun getAll(): List<CredentialEntry> {
        return inMemoryCredentials.toList()
    }

    override fun removeCredentials(credentialEntry: CredentialEntry) {
        inMemoryCredentials.removeIf {
            it.host == credentialEntry.host &&
                    it.username == credentialEntry.username
        }
    }

}