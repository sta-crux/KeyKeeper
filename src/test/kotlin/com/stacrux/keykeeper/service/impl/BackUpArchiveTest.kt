package com.stacrux.keykeeper.service.impl

import com.stacrux.keykeeper.ServiceProvider
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackUpArchiveTest {

    private val backUpService = ServiceProvider.getDefaultBackUpService()

    private fun createTestCsvZip(encryptionKey: String? = null): File {
        // Create a temporary CSV file
        val tempCsv = File.createTempFile("test_logins", ".csv").apply {
            writeText(
                """
            host,username,password
            gmail.com,john.doe,P@ssw0rd123
            outlook.com,jane_smith,Secure!456
            yahoo.com,mike.adams,Qwerty789!
            github.com,dev_user,GitHubRocks42
            amazon.com,buyer2024,Shopping@321
            facebook.com,social_butterfly,FbPass99!
            linkedin.com,pro_networker,JobHunt2025
            twitter.com,tweety_bird,TweetTweet77
            netflix.com,streamlover,MovieTime2023
            apple.com,icloud_user,MacBookPro99
            """.trimIndent()
            )
            deleteOnExit()
        }
        // Create a temporary ZIP file
        val tempZip = File.createTempFile("test_logins", ".zip").apply { deleteOnExit() }
        val zipFile = ZipFile(tempZip)
        val zipParams = ZipParameters().apply {
            compressionMethod = CompressionMethod.DEFLATE
            if (!encryptionKey.isNullOrEmpty()) {
                isEncryptFiles = true
                encryptionMethod = EncryptionMethod.AES
                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
            }
        }
        // Apply password if provided
        if (!encryptionKey.isNullOrEmpty()) {
            zipFile.setPassword(encryptionKey.toCharArray())
        }
        // Add the CSV file to the ZIP
        zipFile.addFile(tempCsv, zipParams)
        return tempZip
    }


    @Test
    fun testImportUnprotectedFile() {
        val unprotectedFile = createTestCsvZip()
        assertTrue { backUpService.isValidBackUpFile(unprotectedFile) }
        assertFalse { backUpService.isProtectedBackUpFile(unprotectedFile) }
        val credentialEntries = assertDoesNotThrow { backUpService.importPlainBackUpFile(unprotectedFile) }
        assertEquals(10, credentialEntries.size)
    }

    @Test
    fun testImportProtectedFile() {
        val protectedFile = createTestCsvZip("password")
        assertTrue { backUpService.isValidBackUpFile(protectedFile) }
        assertTrue { backUpService.isProtectedBackUpFile(protectedFile) }
        assertThrows<Exception> { backUpService.importPlainBackUpFile(protectedFile) }
        assertThrows<Exception> { backUpService.importEncryptedBackUpFile(protectedFile, "wrong_password") }
        val credentialEntries =
            assertDoesNotThrow { backUpService.importEncryptedBackUpFile(protectedFile, "password") }
        assertEquals(10, credentialEntries.size)
    }

    @Test
    fun testCreateBackUpFile() {
        val unprotectedFile = createTestCsvZip()
        val credentials = backUpService.importPlainBackUpFile(unprotectedFile)
        val createBackUpFile = backUpService.createBackUpFile(credentials, "tag", "password", "userid")
        assertTrue { backUpService.isValidBackUpFile(createBackUpFile) }
        assertTrue { backUpService.isProtectedBackUpFile(createBackUpFile) }
    }

    @Test
    fun testCreateBackUpFileAndImportIt() {
        val unprotectedFile = createTestCsvZip()
        val credentials = backUpService.importPlainBackUpFile(unprotectedFile)
        val createBackUpFile = backUpService.createBackUpFile(credentials, "tag", "password", "userid")
        val creds = assertDoesNotThrow { backUpService.importEncryptedBackUpFile(createBackUpFile, "password") }
        assertEquals(10, creds.size)
    }

}