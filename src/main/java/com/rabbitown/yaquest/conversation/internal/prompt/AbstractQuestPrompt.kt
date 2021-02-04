package com.rabbitown.yaquest.conversation.internal.prompt

import com.rabbitown.yalib.module.chat.text.JSONText
import com.rabbitown.yalib.module.locale.YLocale.Companion.getLocaleMessage
import com.rabbitown.yaquest.TypedValue
import com.rabbitown.yaquest.conversation.Conversation
import org.bukkit.conversations.Conversable
import org.bukkit.conversations.ConversationContext
import org.bukkit.entity.Player

/**
 * @author Yoooooory
 */
abstract class AbstractQuestPrompt(
    val message: JSONText,
    val variables: Map<String, TypedValue>
) : QuestPrompt {

    override fun blocksForInput(context: ConversationContext) = true

    protected fun resetShowed(context: ConversationContext) {
        context.setSessionData("showed", false)
    }

    protected fun sendInvalidMessage(target: Conversable) = sendLocaleMessage(target, "conversation.invalid-input")

    protected fun sendLocaleMessage(target: Conversable, key: String) {
        target as Player
        target.sendRawMessage(target.getLocaleMessage("prefix") + target.getLocaleMessage(key))
    }

    protected fun getConversation(context: ConversationContext): Conversation {
        return context.getSessionData(Conversation.OBJECT) as Conversation
    }

}