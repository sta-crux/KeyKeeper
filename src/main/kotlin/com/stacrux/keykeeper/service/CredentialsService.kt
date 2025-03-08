package com.stacrux.keykeeper.service

import com.stacrux.keykeeper.model.CredentialEntry

/**
 * Service in charge of storing and retrieving credentials, mainly used during the Serving life stage
 */
interface CredentialsService {

    fun doesEntryExist(url: String): Boolean
    fun insertEntry(url: String, userName: String, password: String)
    fun insertEntries(credentials: List<CredentialEntry>)
    fun retrieveEntriesAssociatedToUrl(url: String): List<CredentialEntry>
    fun getAllCredentials(): List<CredentialEntry>
    fun getLastServedCredentials(): List<CredentialEntry>

}