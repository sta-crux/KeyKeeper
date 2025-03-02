package com.stacrux.keykeeper.persistence.impl

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.stacrux.keykeeper.persistence.SessionManager
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.File

class SessionManagerFS : SessionManager {

    private val logger = LoggerFactory.getLogger(SessionManagerFS::class.java)

    private val keyKeeperDir = File(System.getProperty("user.home"), "keyKeeper").apply {
        mkdirs()
        logger.info("KeyKeeper directory initialized at: $absolutePath")
    }
    private val sessionFile = File(keyKeeperDir, "session.yaml")
    private val credentialsFile = File(keyKeeperDir, "credentials.zip")

    private val yamlMapper = YAMLMapper()

    override fun storeCredentialsFile(backupFile: File, tag: String) {
        logger.info("Storing credentials file with backup tag: $tag")
        backupFile.copyTo(credentialsFile, overwrite = true)
        val retrieveSession = retrieveSession()
        val session = retrieveSession.copy(lastBackUpTag = tag)
        yamlMapper.writeValue(sessionFile, session)
        logger.info("Credentials file stored successfully.")
    }

    override fun doesSessionExist(): Boolean {
        if (!sessionFile.exists() || sessionFile.readText().isBlank()) {
            logger.warn("Session file does not exist or is empty.")
            return false
        }
        return try {
            val session = yamlMapper.readValue<SessionManager.ChatSession>(sessionFile)
            val exists = StringUtils.isNotEmpty(session.boundUserId)
            logger.info("Session exists: $exists")
            exists
        } catch (e: Exception) {
            logger.error("Failed to read session file: ${e.message}", e)
            false
        }
    }

    override fun retrieveSession(): SessionManager.ChatSession {
        if (!doesSessionExist()) {
            logger.error("Attempted to retrieve session, but none exists.")
            throw IllegalStateException("No active session found")
        }
        logger.info("Retrieving session from file.")
        return yamlMapper.readValue(sessionFile)
    }

    override fun storeSessionDetailsUserId(userId: String) {
        logger.info("Storing session details for userId: $userId")
        val session = SessionManager.ChatSession(userId, lastBackUpTag = null, autoBackUpFeatureEnabled = false)
        yamlMapper.writeValue(sessionFile, session)
        logger.info("Session details stored successfully.")
    }

    override fun retrieveCredentialsFile(): File {
        if (!credentialsFile.exists()) {
            logger.error("Attempted to retrieve credentials file, but none exists.")
            throw IllegalStateException("No credentials file found.")
        }
        logger.info("Retrieving credentials file from: ${credentialsFile.absolutePath}")
        return credentialsFile
    }

    override fun disableLocalBackUp() {
        logger.info("Disabling local backup storage.")
        val retrieveSession = retrieveSession()
        val session = retrieveSession.copy(lastBackUpTag = "", autoBackUpFeatureEnabled = false)
        yamlMapper.writeValue(sessionFile, session)
        credentialsFile.delete()
        logger.info("Local backup disabled and credentials file deleted.")
    }

    override fun enableLocalBackUp() {
        logger.info("Enabling local backup storage.")
        val retrieveSession = retrieveSession()
        val session = retrieveSession.copy(lastBackUpTag = "", autoBackUpFeatureEnabled = true)
        yamlMapper.writeValue(sessionFile, session)
        logger.info("Local backup enabled.")
    }

    override fun deleteSession() {
        logger.info("Deleting session and associated files.")
        credentialsFile.delete()
        sessionFile.delete()
        logger.info("Session and credentials file deleted successfully.")
    }
}
