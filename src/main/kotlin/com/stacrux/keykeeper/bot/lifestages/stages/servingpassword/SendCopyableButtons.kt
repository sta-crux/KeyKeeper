package com.stacrux.keykeeper.bot.lifestages.stages.servingpassword

import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons

object SendCopyableButtons : ActionsButtons() {

    private val sendCopyableAction = ActionButton("Copy mode", "copy_mode")

    private val actions = listOf(sendCopyableAction)

    override fun asActionButtonsList(): List<ActionButton> {
        return actions
    }

    fun getCopyModeActionIdentifier(): String {
        return this.sendCopyableAction.actionIdentifier
    }
}