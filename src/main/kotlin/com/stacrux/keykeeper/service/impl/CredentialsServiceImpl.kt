package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.persistence.CredentialsManager
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.WebsiteParsingService
import org.slf4j.LoggerFactory

class CredentialsServiceImpl(
    private val credentialsManager: CredentialsManager,
    private val webSiteExtractor: WebsiteParsingService
) : CredentialsService {

    private val logger = LoggerFactory.getLogger(CredentialsServiceImpl::class.java)

    override fun doesEntryExist(url: String): Boolean {
        return try {
            retrieveEntriesAssociatedToUrlFragment(url).isNotEmpty()
        } catch (e: Exception) {
            false
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

    override fun retrieveEntriesAssociatedToUrlFragment(urlFragment: String): List<CredentialEntry> {
        val matches: MutableSet<CredentialEntry> = mutableSetOf()
        try {
            val hostFromUrl =  try { webSiteExtractor.extractWebsiteIdentifier(urlFragment).wholeDomain } catch (e: Exception) { urlFragment}
            val credentialsForHost = credentialsManager.getCredentialsForHost(hostFromUrl)
            matches.addAll(credentialsForHost)
        } catch (e: Exception) {
            val partialHost = urlFragment.split(".")
            for (part in partialHost) {
                val credentialsForHost = credentialsManager.getCredentialsForHost(part)
                matches.addAll(credentialsForHost)
            }
        }
        return matches.toList()
    }

    override fun getAllCredentials(): List<CredentialEntry> {
        return credentialsManager.getAll()
    }

    override fun removeCredentials(credentials: CredentialEntry) {
        try {
            credentialsManager.removeCredentials(credentials)
        } catch (e: Exception) {
            logger.error("I could not delete credentials $credentials", e)
        }
    }
}