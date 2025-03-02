package com.stacrux.keykeeper.service

interface WebsiteParsingService {

    class WebSiteIdentifier(val wholeDomain: String, val topLevel: String)

    fun extractWebsiteIdentifier(url: String): WebSiteIdentifier

    fun isParseable(url: String): Boolean
}