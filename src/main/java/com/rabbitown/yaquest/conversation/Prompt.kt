package com.rabbitown.yaquest.conversation

import com.rabbitown.yalib.module.chat.text.JSONText
import com.rabbitown.yalib.module.chat.text.impl.PlainTextElement
import com.rabbitown.yaquest.TypedValue
import com.rabbitown.yaquest.TypedValue.Companion.toTypedValue
import com.rabbitown.yaquest.conversation.internal.prompt.QuestPointerPrompt
import com.rabbitown.yaquest.conversation.internal.prompt.QuestMessagePrompt
import com.rabbitown.yaquest.conversation.internal.prompt.QuestPrompt
import com.rabbitown.yaquest.conversation.pointer.QuestPointer
import org.bukkit.configuration.ConfigurationSection

/**
 * A Prompt is a node of conversation, which has a name, choice message, showing message and pointers to the next prompt.
 *
 * @author Yoooooory
 */
class Prompt(
    val name: String,
    val choice: String?,
    val messages: List<String>,
    val pointers: List<QuestPointer>,
    val variables: Map<String, TypedValue>
) {

    fun toQuestPrompt(): QuestPrompt {
        var last: QuestPrompt = QuestPointerPrompt(JSONText(PlainTextElement(messages.last())), pointers, variables)
        for (i in messages.size - 2 downTo 0) {
            val message = messages[i]
            last = QuestMessagePrompt(JSONText(PlainTextElement(message)), last, variables)
        }
        return last
    }

    companion object {
        fun parseConfig(config: ConfigurationSection, fallbackVariables: Map<String, TypedValue>): Prompt {
            val name = config.name
            val choice = config.getString("choice")
            val messages = config.let {
                when {
                    it.contains("message") -> listOf(it.getString("message")!!)
                    it.contains("messages") -> it.getStringList("messages")
                    else -> error("No message of prompt named $name was found.")
                }
            }
            val pointers = config.getList("pointers")?.map { QuestPointer.parseObject(it!!, config.parent!!) } ?: emptyList()
            val variables = ConversationSkin.CURRENT.getRequiredVariables()
                .map { it to (config.getString(it)?.toTypedValue("text") ?: fallbackVariables[it]!!) }.toMap()
            return Prompt(name, choice, messages, pointers, variables)
        }
    }

}