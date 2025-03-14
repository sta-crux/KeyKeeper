package com.stacrux.keykeeper.bot.lifestages.stages.addcredentials

import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons

object AbandonOrRestart : ActionsButtons() {

    private val abandonAction = ActionButton("\uD83D\uDD19 Go back", "abandon_action")
    private val restartAction = ActionButton("⏪ Start over", "start_over")

    private val actions = listOf(abandonAction, restartAction)

    override fun asActionButtonsList(): List<ActionButton> {
        return actions
    }

    fun getAbandonActionIdentifier(): String {
        return abandonAction.actionIdentifier
    }

    fun getRestartActionIdentifier(): String {
        return restartAction.actionIdentifier
    }

}