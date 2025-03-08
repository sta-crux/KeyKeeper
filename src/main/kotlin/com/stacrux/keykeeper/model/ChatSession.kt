package com.stacrux.keykeeper.model

abstract class ChatSession(
    open val boundUserId: String,
    open val lastBackUpTag: String?,
    open val autoBackUpFeatureEnabled: Boolean = false
)