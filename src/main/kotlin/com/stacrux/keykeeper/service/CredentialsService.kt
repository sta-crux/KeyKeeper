package com.stacrux.keykeeper.service

import com.stacrux.keykeeper.model.CredentialEntry

interface CredentialsService {

    fun doesEntryExist(url: String): Boolean
    fun insertEntry(url: String, userName: String, password: String)
    fun insertEntries(credentials: List<CredentialEntry>)
    fun retrieveEntriesAssociatedToUrl(url: String): List<CredentialEntry>
    fun getAllCredentials(): List<CredentialEntry>

}