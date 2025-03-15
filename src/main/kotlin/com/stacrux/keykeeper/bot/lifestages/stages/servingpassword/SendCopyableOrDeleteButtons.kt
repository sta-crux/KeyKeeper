package com.stacrux.keykeeper.bot.lifestages.stages.servingpassword

import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons
import com.stacrux.keykeeper.model.CredentialEntry

class SendCopyableOrDeleteButtons(val credential: CredentialEntry) : ActionsButtons() {

    companion object {
        private val sendCopyableAction = ActionButton("Copy mode", "copy_mode")
        private val deleteCredentials = ActionButton("\uD83D\uDDD1\uFE0F Delete credentials", "delete_credentials")
        private val actions = listOf(sendCopyableAction, deleteCredentials)
        fun asActionButtonsList(): List<ActionButton> {
            return actions
        }

        fun getCopyModeActionIdentifier(): String {
            return this.sendCopyableAction.actionIdentifier
        }

        fun getDeleteActionIdentifier(): String {
            return deleteCredentials.actionIdentifier
        }
    }


    override fun asActionButtonsList(): List<ActionButton> {
        return SendCopyableOrDeleteButtons.asActionButtonsList()
    }


}