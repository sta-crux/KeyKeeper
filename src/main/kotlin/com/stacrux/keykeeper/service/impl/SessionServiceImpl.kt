package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.persistence.SessionManager
import com.stacrux.keykeeper.service.SessionService
import java.io.File

class SessionServiceImpl(private val sessionManager: SessionManager) : SessionService {

    override fun isStoringBackUps(): Boolean {
        return sessionManager.retrieveSession().autoBackUpFeatureEnabled
    }

    override fun createSession(userId: String) {
        sessionManager.storeSessionDetailsUserId(userId)
    }

    override fun disableSession() {
        sessionManager.disableLocalBackUp()
    }

    override fun doesSessionExist(): Boolean {
        return sessionManager.doesSessionExist()
    }

    override fun retrieveBoundUserId(): String {
        return sessionManager.retrieveSession().boundUserId
    }

    override fun retrieveLastKnownBackUpTag(): String? {
        return sessionManager.retrieveSession().lastBackUpTag
    }

    override fun doesBackUpFileExist(): Boolean {
        try {
            retrieveBackUpFile()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun retrieveBackUpFile(): File {
        return sessionManager.retrieveCredentialsFile()
    }

    override fun storeBackUpFile(backUpFile: File, backUpTag: String) {
        sessionManager.storeCredentialsFile(backUpFile, backUpTag)
    }

    override fun toggleLocalBackUpStoring(isActive: Boolean) {
        if (isActive) {
            sessionManager.enableLocalBackUp()
        } else {
            sessionManager.disableLocalBackUp()
        }
    }
}