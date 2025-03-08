package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.persistence.CredentialsManager
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.WebsiteParsingService

class CredentialsServiceImpl(
    private val credentialsManager: CredentialsManager,
    private val webSiteExtractor: WebsiteParsingService
) : CredentialsService {

    private var lastServed: List<CredentialEntry> = emptyList()

    override fun doesEntryExist(url: String): Boolean {
        val host = webSiteExtractor.extractWebsiteIdentifier(url).wholeDomain
        return credentialsManager.doesHostExist(host)
    }

    override fun insertEntry(url: String, userName: String, password: String) {
        val hostFromUrl = webSiteExtractor.extractWebsiteIdentifier(url).wholeDomain
        val credentialEntry = CredentialEntry(hostFromUrl, password, userName)
        insertEntries(listOf(credentialEntry))
    }

    override fun insertEntries(credentials: List<CredentialEntry>) {
        credentials.forEach {
            this.credentialsManager.registerNewValue(it)
        }
    }

    override fun retrieveEntriesAssociatedToUrl(url: String): List<CredentialEntry> {
        if (!doesEntryExist(url)) {
            throw Exception()
        }
        val hostFromUrl = webSiteExtractor.extractWebsiteIdentifier(url)
        val credentialsForHost = credentialsManager.getCredentialsForHost(hostFromUrl.wholeDomain)
        lastServed = credentialsForHost
        return credentialsForHost
    }

    override fun getAllCredentials(): List<CredentialEntry> {
        return credentialsManager.getAll()
    }

    override fun getLastServedCredentials(): List<CredentialEntry> {
        return lastServed
    }
}