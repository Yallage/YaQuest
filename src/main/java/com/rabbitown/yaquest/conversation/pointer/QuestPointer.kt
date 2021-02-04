package com.rabbitown.yaquest.conversation.pointer

import com.rabbitown.yaquest.conversation.Conversation
import com.rabbitown.yaquest.conversation.Prompt
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.conversations.ConversationContext

/**
 * @author Yoooooory
 */
// TODO: refactor.
open class QuestPointer(
    val requiredInput: String,
    val next: String,
    val showText: String = requiredInput
) {

    protected fun getConversation(context: ConversationContext) = context.getSessionData(Conversation.OBJECT) as Conversation

    fun isRequiredInput(input: String?): Boolean {
        return requiredInput == input
    }

    fun getNextPrompt(context: ConversationContext): Prompt {
        return getConversation(context).prompts[next]!!
    }

    companion object {
        fun parseObject(obj: Any, root: ConfigurationSection): QuestPointer {
            return when (obj) {
                is String -> {
                    when (val choice = root.getConfigurationSection(obj)!!.get("choice")) {
                        null -> QuestPointer(obj, obj)
                        is String -> QuestPointer(choice, obj)
                        is ConfigurationSection -> QuestPointer(choice.getString("required")!!, obj, choice.getString("text")!!)
                        else -> error("Cannot parse $choice.")
                    }
                }
                is Map<*, *> -> {
                    val pointer = obj["pointer"]!! as String
                    val choice = obj["choice"] as String? ?: pointer
                    val text = obj["text"] as String? ?: choice
                    QuestPointer(choice, pointer, text)
                }
                else -> error("Cannot parse $obj.")
            }
        }
    }

}