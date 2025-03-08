package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.service.WebsiteParsingService
import org.slf4j.LoggerFactory
import java.net.URI

class WebSiteExtractor : WebsiteParsingService {

    private val logger = LoggerFactory.getLogger(WebSiteExtractor::class.java)


    override fun extractWebsiteIdentifier(url: String): WebsiteParsingService.WebSiteIdentifier {
        try {
            val formattedUrl = if (!url.startsWith("http")) "https://$url" else url
            val uri = URI(formattedUrl)
            val host = uri.host ?: throw Exception("Invalid host")

            val parts = host.split(".").toMutableList()
            parts.removeIf { it == "www" }
            if (parts.size < 2) {
                throw Exception("no top level domain")
            }
            val topLevel = parts.lastOrNull() ?: throw Exception("Invalid TLD")
            val wholeDomain = parts.dropLast(1).joinToString(".")

            return WebsiteParsingService.WebSiteIdentifier(wholeDomain, topLevel)
        } catch (e: Exception) {
            throw Exception("I can't parse this website...", e)
        }
    }


    override fun isParseable(url: String): Boolean {
        try {
            this.extractWebsiteIdentifier(url)
            return true
        } catch (e: Exception) {
            return false
        }
    }

}