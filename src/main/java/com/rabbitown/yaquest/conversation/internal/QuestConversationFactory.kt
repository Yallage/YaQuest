package com.rabbitown.yaquest.conversation.internal

import com.rabbitown.yaquest.YaQuest
import com.rabbitown.yaquest.conversation.internal.prompt.QuestPrompt
import org.bukkit.conversations.*

class QuestConversationFactory : ConversationFactory(YaQuest.instance) {

    override fun withFirstPrompt(firstPrompt: Prompt?): ConversationFactory {
        if (firstPrompt !is QuestPrompt) throw UnsupportedOperationException()
        return super.withFirstPrompt(firstPrompt)
    }

    override fun buildConversation(forWhom: Conversable): QuestConversation {
        val copiedInitialSessionData: MutableMap<Any, Any> = HashMap()
        copiedInitialSessionData.putAll(initialSessionData)

        return QuestConversation(forWhom, firstPrompt as QuestPrompt, copiedInitialSessionData).apply {
//            if (!this@QuestConversationFactory.isModal) invokeMethod(this, "setModal", false)
            isLocalEchoEnabled = localEchoEnabled
            if (prefix !is NullConversationPrefix) invokeMethod(this, "setPrefix", prefix)
            cancellers.forEach { invokeMethod(this, "addConversationCanceller", it.clone()) }
            abandonedListeners.forEach { addConversationAbandonedListener(it) }
        }
    }

    private fun invokeMethod(conversation: QuestConversation, name: String, value: Any) {
        val method = conversation::class.java.getDeclaredMethod(name, value::class.java)
        method.isAccessible = true
        method.invoke(conversation, value)
    }

}