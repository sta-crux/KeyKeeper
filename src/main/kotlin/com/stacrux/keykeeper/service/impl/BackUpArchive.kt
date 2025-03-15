package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.model.CredentialEntry
import com.stacrux.keykeeper.service.BackUpService
import com.stacrux.keykeeper.service.WebsiteParsingService
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class BackUpArchive(
    private val webSiteExtractor: WebsiteParsingService
) : BackUpService {

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

    override fun inspectProvidedImportFile(passwordsBackup: File): BackUpService.SupportedFiles {
        if (isZipFile(passwordsBackup)) {
            if (isProtectedBackUpFile(passwordsBackup)) {
                return BackUpService.SupportedFiles.KEY_KEEPER_PROTECTED
            }
            return BackUpService.SupportedFiles.KEY_KEEPER_CLEAR
        }
        if (passwordsBackup.useLines { lines ->
                lines.firstOrNull()?.contains("\"url\",\"username\",\"password\"") == true
            }) {
            return BackUpService.SupportedFiles.FIREFOX
        }
        throw Exception("Unsupported import file")
    }

    override fun import3rdPartyExport(externalExportFile: File): List<CredentialEntry> {
        val inspectProvidedImportFile = inspectProvidedImportFile(externalExportFile)
        if (inspectProvidedImportFile == BackUpService.SupportedFiles.FIREFOX) {
            val cleanedFile = File(externalExportFile.parent, "cleaned_firefox_export.csv")
            externalExportFile.useLines(StandardCharsets.UTF_8) { lines ->
                cleanedFile.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                    lines.forEachIndexed { index, line ->
                        val parts = line.split(",").map { it.trim('"') }
                        if (index == 0) {
                            // Write new header
                            writer.write("url,username,password\n")
                        } else if (parts.size >= 3) {
                            // Extract only url, username, password
                            writer.write("${parts[0]},${parts[1]},${parts[2]}\n")
                        }
                    }
                }
            }
            val credentials = parseCSV(cleanedFile)
            cleanedFile.delete()
            return credentials
        }
        return listOf()
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
            ZipFile(file).fileHeaders // Triggers zip4j to read the file as a ZIP
            true
        } catch (e: net.lingala.zip4j.exception.ZipException) {
            false
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
        val csvFile = extractCSV(zipFile, tempDir)
        return parseCSV(csvFile)
    }

    private fun extractCSV(zipFile: ZipFile, tempDir: File): File {
        zipFile.extractAll(tempDir.absolutePath)
        return tempDir.listFiles()?.firstOrNull { it.extension == "csv" }
            ?: throw IllegalStateException("CSV file not found in backup.")
    }

    private fun parseCSV(csvFile: File): List<CredentialEntry> {
        val credentials = csvFile.useLines(StandardCharsets.UTF_8) { lines ->
            lines.drop(1) // Skip header
                .mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size == 3) CredentialEntry(
                        host =
                            try {
                                webSiteExtractor.extractWebsiteIdentifier(parts[0]).wholeDomain
                            } catch (e: Exception) {
                                parts[0]
                            },
                        username = parts[1],
                        password = parts[2]
                    ) else null
                }
                .toList()
        }
        csvFile.delete()
        csvFile.parentFile?.delete() // Clean temp dir
        return credentials
    }

}