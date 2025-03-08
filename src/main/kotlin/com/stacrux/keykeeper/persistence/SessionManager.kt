package com.stacrux.keykeeper.persistence

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.stacrux.keykeeper.model.ChatSession
import java.io.File

interface SessionManager {

    data class FsChatSession @JsonCreator constructor(
        @JsonProperty("boundUserId") override val boundUserId: String,
        @JsonProperty("lastBackUpTag") override val lastBackUpTag: String?,
        @JsonProperty("localBackUpEnabled") override val autoBackUpFeatureEnabled: Boolean = false
    ) : ChatSession(boundUserId, lastBackUpTag, autoBackUpFeatureEnabled)

    fun storeCredentialsFile(backupFile: File, tag: String)
    fun doesSessionExist(): Boolean
    fun retrieveSession(): ChatSession
    fun storeSessionDetailsUserId(userId: String)
    fun retrieveCredentialsFile(): File
    fun disableLocalBackUp()
    fun enableLocalBackUp()
    fun deleteSession()
}
