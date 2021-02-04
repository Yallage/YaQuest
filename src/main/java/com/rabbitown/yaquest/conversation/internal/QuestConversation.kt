package com.rabbitown.yaquest.conversation.internal

import com.rabbitown.yaquest.YaQuest
import com.rabbitown.yaquest.conversation.ConversationManager
import com.rabbitown.yaquest.conversation.internal.prompt.QuestPrompt
import org.bukkit.conversations.*

/**
 * @author Yoooooory
 */
class QuestConversation(
    forWhom: Conversable,
    firstPrompt: QuestPrompt,
    initialSessionData: Map<Any, Any?> = mapOf()
) : Conversation(YaQuest.instance, forWhom, firstPrompt, initialSessionData) {

    fun start() {
        super.begin()
        ConversationManager.runningConversations += this
    }

    override fun abandon() {
        ConversationManager.runningConversations -= this
    }

    override fun outputNextPrompt() {
        if (currentPrompt == null) {
            abandon(ConversationAbandonedEvent(this))
        } else {
            (currentPrompt as QuestPrompt).getPromptText(context)
            if (!currentPrompt.blocksForInput(context)) {
                currentPrompt = currentPrompt.acceptInput(context, null)
                outputNextPrompt()
            }
        }
    }

    companion object {
        val ABANDONED_LISTENER = ConversationAbandonedListener {
            val canceller = it.canceller
            if (canceller !is ConversationPluginCanceler || canceller.removeFromList) {
                ConversationManager.runningConversations -= (it.source as QuestConversation)
            }
        }
    }

}