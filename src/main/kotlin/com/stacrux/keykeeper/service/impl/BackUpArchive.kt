package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.service.BackUpService
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class BackUpArchive : BackUpService {

    override fun createBackUpFile(
        credentials: List<CredentialEntry>,
        backUpTag: String,
        encryptionKey: String,
        userId: String
    ): File {
        val csvFile = File.createTempFile("backup", ".csv").apply {
            bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                writer.write("host,username,password\n")
                credentials.forEach { writer.write("${it.host},${it.username},${it.password}\n") }
            }
        }

        val zipFile = File.createTempFile("backup_tag_${backUpTag}__", ".zip")
        val standardizedPassword = encryptionKey.toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8).toCharArray()
        val zip = ZipFile(zipFile, standardizedPassword)
        val params = ZipParameters().apply {
            compressionMethod = CompressionMethod.DEFLATE
            isEncryptFiles = true
            encryptionMethod = EncryptionMethod.AES
            aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
        }

        // Add CSV file
        zip.addFile(csvFile, params)

        // Create and add a metadata file containing backUpTag and userId
        val metadataFile = File.createTempFile("backup_metadata_", ".txt").apply {
            writeText("Backup Tag: $backUpTag\nUser ID: $userId")
        }
        zip.addFile(metadataFile, params)

        // Cleanup temporary files
        csvFile.delete()
        metadataFile.delete()

        return zipFile
    }



    override fun importEncryptedBackUpFile(passwordsBackup: File, encryptionKey: String): List<CredentialEntry> {
        val tempDir = Files.createTempDirectory("backup_extract").toFile()
        return extractAndParseCSV(ZipFile(passwordsBackup, encryptionKey.toCharArray()), tempDir)
    }

    override fun importPlainBackUpFile(passwordsBackup: File): List<CredentialEntry> {
        val tempDir = Files.createTempDirectory("backup_extract").toFile()
        return extractAndParseCSV(ZipFile(passwordsBackup), tempDir)
    }

    override fun isValidBackUpFile(passwordsBackup: File): Boolean {
        return try {
            isZipFile(passwordsBackup)
        } catch (e: Exception) {
            false
        }
    }

    override fun isProtectedBackUpFile(passwordsBackup: File): Boolean {
        return try {
            isZipPasswordProtected(passwordsBackup)
        } catch (e: Exception) {
            false
        }
    }

    private fun isZipFile(file: File): Boolean {
        return try {
            ZipFile(file).file.exists() // zip4j will verify if it's a valid ZIP
        } catch (e: Exception) {
            false
        }
    }

    private fun isZipPasswordProtected(file: File): Boolean {
        return try {
            ZipFile(file).isEncrypted
        } catch (e: Exception) {
            false // Handle corrupt or invalid ZIP files
        }
    }

    private fun extractAndParseCSV(zipFile: ZipFile, tempDir: File): List<CredentialEntry> {
        zipFile.extractAll(tempDir.absolutePath)
        val csvFile = tempDir.listFiles()?.firstOrNull { it.extension == "csv" }
            ?: throw IllegalStateException("CSV file not found in backup.")
        val credentials = csvFile.useLines(StandardCharsets.UTF_8) { lines ->
            lines.drop(1) // Skip header
                .mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size == 3) CredentialEntry(parts[0], parts[1], parts[2]) else null
                }
                .toList()
        }
        csvFile.delete()
        tempDir.delete()
        return credentials
    }
}