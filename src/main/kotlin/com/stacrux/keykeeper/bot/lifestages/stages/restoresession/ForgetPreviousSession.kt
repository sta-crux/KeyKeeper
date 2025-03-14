package com.stacrux.keykeeper.bot.lifestages.stages.restoresession

import com.stacrux.keykeeper.bot.model.ActionButton
import com.stacrux.keykeeper.bot.model.ActionsButtons

object ForgetPreviousSession : ActionsButtons() {

    private val forgetSessionAction =
        ActionButton(buttonText = "\uD83D\uDDD1\uFE0F Ignore previous data", actionIdentifier = "forget_prev_session")

    private val primaryActions = listOf(forgetSessionAction)

    override fun asActionButtonsList(): List<ActionButton> {
        return primaryActions
    }

    fun getForgetSessionActionId(): String {
        return forgetSessionAction.actionIdentifier
    }
}