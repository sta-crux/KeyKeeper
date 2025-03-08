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
        try {
            return retrieveEntriesAssociatedToUrl(url).isNotEmpty()
        } catch (e: Exception) {
            return false
        }
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
        val matches: MutableSet<CredentialEntry> = mutableSetOf()
        try {
            val hostFromUrl = webSiteExtractor.extractWebsiteIdentifier(url)
            val credentialsForHost = credentialsManager.getCredentialsForHost(hostFromUrl.wholeDomain)
            matches.addAll(credentialsForHost)
        } catch (e: Exception) {
            val partialHost = url.split(".").filter { it.length > 3 }
            for (part in partialHost) {
                val credentialsForHost = credentialsManager.getCredentialsForHost(part)
                matches.addAll(credentialsForHost)
            }
        }
        lastServed = matches.toList()
        return matches.toList()
    }

    override fun getAllCredentials(): List<CredentialEntry> {
        return credentialsManager.getAll()
    }

    override fun getLastServedCredentials(): List<CredentialEntry> {
        return lastServed
    }
}