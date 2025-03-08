package com.stacrux.keykeeper.persistence.impl

import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.persistence.CredentialsManager

object CredentialsInMemoryObj : CredentialsManager {

    private val inMemoryCredentials: MutableList<CredentialEntry> =
        mutableListOf()

    override fun getCredentialsForHost(host: String): List<CredentialEntry> {
        val normalizedHost = host.lowercase() // Normalize input to lowercase
        return inMemoryCredentials.filter { credential ->
            credential.host.lowercase().contains(normalizedHost) // Perform a case-insensitive substring match
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

}