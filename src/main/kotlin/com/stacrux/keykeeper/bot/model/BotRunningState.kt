package com.stacrux.keykeeper.bot.model

enum class BotRunningState {
    UNBOUND, // the bot needs to be bound to a user id, nothing else can be performed
    ADD_CREDENTIALS, // new entries can be added to the passwords
    SERVING, // the bot is operative and serves credentials
    BACKUP, // the bot offers options to forward the backup to the user or to import a new file
    RESTORE_SESSION, // the bot restarted and found an active session to reuse
    MANAGE_CREDENTIALS // stage to delete or update credentials
}


