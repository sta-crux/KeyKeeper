package com.stacrux.keykeeper.bot.model

abstract class ActionsButtons {

    object EmptyActionButtons : ActionsButtons() {
        override fun asActionButtonsList(): List<ActionButton> {
            return listOf()
        }
    }

    abstract fun asActionButtonsList(): List<ActionButton>

}