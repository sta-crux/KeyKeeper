package com.stacrux.keykeeper.service

import java.io.File

/**
 * Service to define and get session properties
 */
interface SessionService {

    fun isStoringBackUps(): Boolean
    fun createSession(userId: String)
    fun disableSession()
    fun doesSessionExist(): Boolean
    fun retrieveBoundUserId(): String
    fun retrieveLastKnownBackUpTag(): String?
    fun doesBackUpFileExist(): Boolean
    fun retrieveBackUpFile(): File
    fun storeBackUpFile(backUpFile: File, backUpTag: String)
    fun toggleLocalBackUpStoring(isActive: Boolean)

}