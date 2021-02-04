package com.rabbitown.yaquest.conversation

import com.rabbitown.yaquest.common.Config
import com.rabbitown.yaquest.conversation.internal.QuestConversation
import com.rabbitown.yaquest.conversation.internal.QuestConversationFactory

/**
 * @author Yoooooory
 */
object ConversationManager {

    val runningConversations = mutableListOf<QuestConversation>()

    lateinit var factory: QuestConversationFactory
        private set

    fun reload() {
        factory = Config.conversationFactory
    }

}