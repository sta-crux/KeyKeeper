package com.stacrux.keykeeper.persistence

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

interface SessionManager {

    data class ChatSession @JsonCreator constructor(
        @JsonProperty("boundUserId") val boundUserId: String,
        @JsonProperty("lastBackUpTag") val lastBackUpTag: String?,
        @JsonProperty("localBackUpEnabled") val autoBackUpFeatureEnabled: Boolean = false
    )

    fun storeCredentialsFile(backupFile: File, tag: String)
    fun doesSessionExist(): Boolean
    fun retrieveSession(): ChatSession
    fun storeSessionDetailsUserId(userId: String)
    fun retrieveCredentialsFile(): File
    fun disableLocalBackUp()
    fun enableLocalBackUp()
    fun deleteSession()
}
