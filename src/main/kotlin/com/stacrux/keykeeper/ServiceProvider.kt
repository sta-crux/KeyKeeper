package com.stacrux.keykeeper

import com.stacrux.keykeeper.bot.KeyKeeper
import com.stacrux.keykeeper.bot.KeyKeeperImpl
import com.stacrux.keykeeper.persistence.CredentialsManager
import com.stacrux.keykeeper.persistence.InteractionsManager
import com.stacrux.keykeeper.persistence.SessionManager
import com.stacrux.keykeeper.persistence.impl.CredentialsInMemoryObj
import com.stacrux.keykeeper.persistence.impl.InMemoryInteractionManager
import com.stacrux.keykeeper.persistence.impl.SessionManagerFS
import com.stacrux.keykeeper.service.*
import com.stacrux.keykeeper.service.impl.*
import java.io.File

/**
 * Use this object to retrieve the services wherever they are needed
 */
object ServiceProvider {

    fun getKeyKeeperService(): KeyKeeper {
        return KeyKeeperImpl
    }

    fun getDefaultWebSiteParsingService(): WebsiteParsingService {
        return WebSiteExtractor()
    }

    fun getDefaultBackUpService(): BackUpService {
        return BackUpArchive(getDefaultWebSiteParsingService())
    }

    var baseDir: File = File(System.getProperty("user.home"))

    /**
     * Call this method to provide a base directory where the bots will save its file
     */
    fun customizeBaseDir(baseDir: File) {
        ServiceProvider.baseDir = baseDir
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

    fun getDefaultMonitoringService(): MonitoringService {
        return MonitoringServiceImpl(subServiceProvider.getDefaultInteractionsManager())
    }

    /*
     * Private part
     */
    private val subServiceProvider: SubServicesProvider = SubServicesProvider.createInstance()

    /**
     * These are not to be accessed directly
     */
    class SubServicesProvider private constructor() {
        fun getDefaultSessionManager(): SessionManager {
            return SessionManagerFS(baseDir)
        }

        fun getDefaultCredentialsManager(): CredentialsManager {
            return CredentialsInMemoryObj
        }

        fun getDefaultInteractionsManager(): InteractionsManager {
            return InMemoryInteractionManager
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