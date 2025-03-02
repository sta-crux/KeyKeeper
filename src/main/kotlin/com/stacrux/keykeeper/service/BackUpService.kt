package com.stacrux.keykeeper.service

import com.stacrux.keykeeper.model.CredentialEntry
import java.io.File

interface BackUpService {

    fun createBackUpFile(credentials: List<CredentialEntry>, backUpTag: String, encryptionKey: String, userId: String): File
    fun importEncryptedBackUpFile(passwordsBackup: File, encryptionKey: String): List<CredentialEntry>
    fun importPlainBackUpFile(passwordsBackup: File): List<CredentialEntry>
    fun isValidBackUpFile(passwordsBackup: File): Boolean
    fun isProtectedBackUpFile(passwordsBackup: File): Boolean
}