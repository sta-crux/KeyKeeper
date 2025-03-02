package com.stacrux.keykeeper.bot.model

/**
 * Represent an action (as a clickable button) that you want to display in the chat after a given message
 * the action identifier is freetext and allows you to identify a button click callback among many
 * and proceed with the proper logic (example: add, remove stuff)
 */
class ActionButton(val buttonText: String, val actionIdentifier: String) {
}