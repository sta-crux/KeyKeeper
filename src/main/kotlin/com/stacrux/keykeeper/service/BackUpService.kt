package com.stacrux.keykeeper.service

import com.stacrux.keykeeper.model.CredentialEntry
import java.io.File

/**
 * Service to create or import backup files
 */
interface BackUpService {

    fun createBackUpFile(
        credentials: List<CredentialEntry>,
        backUpTag: String,
        encryptionKey: String,
        userId: String
    ): File

    fun importEncryptedBackUpFile(passwordsBackup: File, encryptionKey: String): List<CredentialEntry>
    fun importPlainBackUpFile(passwordsBackup: File): List<CredentialEntry>
    fun isProtectedBackUpFile(passwordsBackup: File): Boolean
    enum class SupportedFiles {
        KEY_KEEPER_PROTECTED,
        KEY_KEEPER_CLEAR,
        FIREFOX
    }

    fun inspectProvidedImportFile(passwordsBackup: File): SupportedFiles

    fun import3rdPartyExport(externalExportFile: File): List<CredentialEntry>
}