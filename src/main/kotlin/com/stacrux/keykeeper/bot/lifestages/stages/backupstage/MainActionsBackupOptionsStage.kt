package com.stacrux.keykeeper.bot.lifestages.stages.backupstage

import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons

object MainActionsBackupOptionsStage : ActionsButtons() {

    private val abandonButton = ActionButton("\uD83D\uDD19 Go back", "abandon_action")
    private val backUpButton = ActionButton("\uD83D\uDCBE Perform backup", "backup_credentials")
    private val toggleStatefulButton =
        ActionButton("\uD83D\uDDC4\uFE0F Enable/Disable local backup", "stateful_toggle")

    private val actions = listOf(abandonButton, backUpButton, toggleStatefulButton)

    override fun asActionButtonsList(): List<ActionButton> {
        return actions
    }

    fun getBackUpActionIdentifier(): String {
        return this.backUpButton.actionIdentifier
    }

    fun getBackUpActionLabel(): String {
        return this.backUpButton.buttonText
    }

    fun getAbandonActionIdentifier(): String {
        return this.abandonButton.actionIdentifier
    }

    fun getToggleStatefulModeActionIdentifier(): String {
        return this.toggleStatefulButton.actionIdentifier
    }
}