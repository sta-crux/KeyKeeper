package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.ServiceProvider
import com.stacrux.keykeeper.TestsSetup
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import java.util.*

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebSiteExtractorTest : TestsSetup() {

    val webSiteExtractor = ServiceProvider.getDefaultWebSiteParsingService()

    @Test
    fun testParseability() {
        var newUrl = "https://start.duckduckgo.com/" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertTrue { webSiteExtractor.isParseable(newUrl) }
        newUrl = "start.duckduckgo.com/" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertTrue { webSiteExtractor.isParseable(newUrl) }
        newUrl = "https://www.youtube.com/?gl=IT&hl=it" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertTrue { webSiteExtractor.isParseable(newUrl) }
        newUrl = "https://youtube.com/?gl=IT&hl=it" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertTrue { webSiteExtractor.isParseable(newUrl) }
        newUrl = "chatgpt/c/67bf991f-cf18-8000-8461-d4dde0cb0d40" +
                RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertFalse { webSiteExtractor.isParseable(newUrl) }
    }

    @Test
    fun testExtractDomain() {
        var newUrl =
            "https://start.duckduckgo.com/" + RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertEquals("start.duckduckgo", webSiteExtractor.extractWebsiteIdentifier(newUrl).wholeDomain)
        newUrl = "start.duckduckgo.com/" + RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertEquals("start.duckduckgo", webSiteExtractor.extractWebsiteIdentifier(newUrl).wholeDomain)
        newUrl = "https://www.youtube.com/?gl=IT&hl=it" + RandomStringUtils.randomAlphabetic(10)
            .lowercase(Locale.getDefault())
        assertEquals("youtube", webSiteExtractor.extractWebsiteIdentifier(newUrl).wholeDomain)
        newUrl =
            "https://youtube.com/?gl=IT&hl=it" + RandomStringUtils.randomAlphabetic(10).lowercase(Locale.getDefault())
        assertEquals("youtube", webSiteExtractor.extractWebsiteIdentifier(newUrl).wholeDomain)
        newUrl = "https://cfspart-idp.impots.gouv.fr/oauth2" + RandomStringUtils.randomAlphabetic(10)
            .lowercase(Locale.getDefault())
        assertEquals("cfspart-idp.impots.gouv", webSiteExtractor.extractWebsiteIdentifier(newUrl).wholeDomain)
    }

}