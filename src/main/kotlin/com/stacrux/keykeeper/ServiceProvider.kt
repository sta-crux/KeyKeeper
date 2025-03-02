package com.stacrux.keykeeper

import com.stacrux.keykeeper.bot.KeyKeeper
import com.stacrux.keykeeper.bot.KeyKeeperImpl
import com.stacrux.keykeeper.persistence.CredentialsManager
import com.stacrux.keykeeper.persistence.SessionManager
import com.stacrux.keykeeper.persistence.impl.CredentialsInMemoryObj
import com.stacrux.keykeeper.persistence.impl.SessionManagerFS
import com.stacrux.keykeeper.service.BackUpService
import com.stacrux.keykeeper.service.CredentialsService
import com.stacrux.keykeeper.service.SessionService
import com.stacrux.keykeeper.service.WebsiteParsingService
import com.stacrux.keykeeper.service.impl.BackUpArchive
import com.stacrux.keykeeper.service.impl.CredentialsServiceImpl
import com.stacrux.keykeeper.service.impl.SessionServiceImpl
import com.stacrux.keykeeper.service.impl.WebSiteExtractor

object ServiceProvider {

    fun getKeyKeeperService(): KeyKeeper {
        return KeyKeeperImpl
    }

    fun getDefaultWebSiteParsingService(): WebsiteParsingService {
        return WebSiteExtractor()
    }

    fun getDefaultBackUpService(): BackUpService {
        return BackUpArchive()
    }

    fun getDefaultSessionService(): SessionService {
        return SessionServiceImpl(subServiceProvider.getDefaultSessionManager())
    }

    fun getDefaultCredentialsService(): CredentialsService {
        return CredentialsServiceImpl(
            subServiceProvider.getDefaultCredentialsManager(),
            getDefaultWebSiteParsingService()
        )
    }


    /*
     * Private part
     */
    private val subServiceProvider: SubServicesProvider = SubServicesProvider.createInstance()

    /**
     * These are not accessible outside services
     */
    class SubServicesProvider private constructor() {
        fun getDefaultSessionManager(): SessionManager {
            return SessionManagerFS()
        }

        fun getDefaultCredentialsManager(): CredentialsManager {
            return CredentialsInMemoryObj
        }

        companion object {
            private var subServicesProvider: SubServicesProvider? = null
            internal fun createInstance(): SubServicesProvider {
                if (subServicesProvider == null) {
                    val subs = SubServicesProvider()
                    subServicesProvider = subs
                    return subs
                } else {
                    return subServicesProvider ?: throw Exception()
                }

            }
        }
    }
}