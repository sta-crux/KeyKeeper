package com.stacrux.keykeeper.persistence

import com.stacrux.keykeeper.model.CredentialEntry

interface CredentialsManager {

    fun getCredentialsForHost(host: String): List<CredentialEntry>
    fun doesHostExist(host: String): Boolean
    fun registerNewValue(credentialEntry: CredentialEntry)
    fun getAll(): List<CredentialEntry>
}