package com.stacrux.keykeeper.bot.lifestages.stages.servingpassword

import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons

object AddOrBackUpButtons : ActionsButtons() {

    private val addCredentialsAction =
        ActionButton(buttonText = "\uD83D\uDD0F Add new credentials", actionIdentifier = "add_credential_entry")
    private val backUpAction =
        ActionButton(buttonText = "\uD83D\uDCE6 Backup options", actionIdentifier = "backup_credentials")

    private val primaryActions = listOf(addCredentialsAction, backUpAction)

    override fun asActionButtonsList(): List<ActionButton> {
        return primaryActions
    }


    fun getAddActionIdentifier(): String {
        return this.addCredentialsAction.actionIdentifier
    }


    fun getBackUpActionLabel(): String {
        return this.backUpAction.buttonText
    }

    fun getBackUpActionIdentifier(): String {
        return this.backUpAction.actionIdentifier
    }
}