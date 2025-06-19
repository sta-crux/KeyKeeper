package com.stacrux.keykeeper.persistence

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.stacrux.keykeeper.model.ChatSession
import java.io.File

abstract class SessionManager(val baseDir: File) {

    data class FsChatSession @JsonCreator constructor(
        @JsonProperty("boundUserId") override val boundUserId: String,
        @JsonProperty("lastBackUpTag") override val lastBackUpTag: String?,
        @JsonProperty("localBackUpEnabled") override val autoBackUpFeatureEnabled: Boolean = false
    ) : ChatSession(boundUserId, lastBackUpTag, autoBackUpFeatureEnabled)

    abstract fun storeCredentialsFile(backupFile: File, tag: String)
    abstract fun doesSessionExist(): Boolean
    abstract fun retrieveSession(): ChatSession
    abstract fun storeSessionDetailsUserId(userId: String)
    abstract fun retrieveCredentialsFile(): File
    abstract fun disableLocalBackUp()
    abstract fun enableLocalBackUp()
    abstract fun deleteSession()
}
