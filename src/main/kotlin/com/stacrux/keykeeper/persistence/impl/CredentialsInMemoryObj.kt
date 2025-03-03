package com.stacrux.keykeeper.persistence.impl

import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.persistence.CredentialsManager

object CredentialsInMemoryObj : CredentialsManager {

    private val inMemoryCredentials: MutableList<CredentialEntry> =
        mutableListOf()

    override fun getCredentialsForHost(host: String): List<CredentialEntry> {
        return inMemoryCredentials
            .filter { it.host == host }
    }

    override fun doesHostExist(host: String): Boolean {
        return inMemoryCredentials
            .map { it.host }
            .contains(host)
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