package com.stacrux.keykeeper.scheduled

import com.stacrux.keykeeper.ServiceProvider
import org.apache.commons.lang3.RandomStringUtils
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object ScheduledBackUpService {

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    init {
        val now = LocalDateTime.now()
        val targetTime = now.with(LocalTime.of(21, 0))
        val firstRun = if (now.isAfter(targetTime)) targetTime.plusDays(1) else targetTime
        val initialDelay = Duration.between(now, firstRun).seconds

        scheduler.scheduleAtFixedRate(
            { runBackUp() },
            initialDelay,
            TimeUnit.DAYS.toSeconds(1),
            TimeUnit.SECONDS
        )
    }

    private fun runBackUp() {
        val sessionService = ServiceProvider.getDefaultSessionService()
        val credentialsService = ServiceProvider.getDefaultCredentialsService()
        val backUpService = ServiceProvider.getDefaultBackUpService()
        val keyKeeperClientHolder = ServiceProvider.getKeyKeeperService().getClientHolder()

        val userId = sessionService.getBoundUserId()
        val allCredentials = credentialsService.getAllCredentials()
        val encryptionKey = RandomStringUtils.randomAlphanumeric(16)
        val backUpTag = "auto_backup_" + RandomStringUtils.randomAlphanumeric(16)
        val timeBeforeDelete = 120

        keyKeeperClientHolder.sendMessage(
            userId,
            "Performing daily backup...\nStore this secret in your Saved Messages, " +
                    "it is required to open the backup file, I'll delete it from here in $timeBeforeDelete " +
                    "minutes.\n\uD83C\uDFF7\uFE0F backup file tag: `$backUpTag`\n" +
                    "\uD83D\uDD11 backup file password: `$encryptionKey`",
            deleteAfterMinutes = timeBeforeDelete
        )

        val backUpFile = backUpService.createBackUpFile(
            credentials = allCredentials,
            backUpTag = backUpTag,
            encryptionKey = encryptionKey,
            userId = userId
        )
        if (sessionService.isStoringBackUps()) {
            sessionService.storeBackUpFile(backUpFile, backUpTag)
        }
        keyKeeperClientHolder.sendFile(userId, backUpFile)
    }
}
