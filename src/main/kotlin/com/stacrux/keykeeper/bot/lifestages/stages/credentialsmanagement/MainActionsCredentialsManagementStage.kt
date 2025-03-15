package com.stacrux.keykeeper.bot.lifestages.stages.credentialsmanagement

import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons

object MainActionsCredentialsManagementStage : ActionsButtons() {

    private val abandonButton = ActionButton("\uD83D\uDD19 Go back", "abandon_action")
    private val printWholeList = ActionButton("\uD83D\uDCBE Print all", "print_all_credentials")

    private val actions = listOf(abandonButton, printWholeList)

    override fun asActionButtonsList(): List<ActionButton> {
        return actions
    }

    fun getAbandonActionId(): String {
        return abandonButton.actionIdentifier
    }

    fun getPrintWholeList(): String {
        return printWholeList.actionIdentifier
    }

}