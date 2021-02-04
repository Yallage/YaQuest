package com.rabbitown.yaquest.conversation.internal.prompt

import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt

/**
 * @author Yoooooory
 */
interface QuestPrompt : Prompt {

    override fun getPromptText(context: ConversationContext): String {
        if (context.getSessionData("showed") != true) {
            showPrompt(context)
            context.setSessionData("showed", true)
        }
        return "" // This was ignored in QuestConversation.
    }

    fun showPrompt(context: ConversationContext)

}