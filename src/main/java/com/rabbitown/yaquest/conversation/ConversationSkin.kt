package com.rabbitown.yaquest.conversation

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.rabbitown.yalib.module.chat.text.*
import com.rabbitown.yalib.module.chat.text.event.*
import com.rabbitown.yalib.module.chat.text.impl.*
import com.rabbitown.yalib.module.locale.YLocale.Companion.getLocaleMessage
import com.rabbitown.yalib.util.ExtendFunction.Companion.arg
import com.rabbitown.yaquest.TypedValue
import com.rabbitown.yaquest.TypedValue.Companion.toTypedValue
import com.rabbitown.yaquest.conversation.internal.prompt.*
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.conversations.ConversationContext
import org.bukkit.entity.Player

/**
 * Represents a skin of conversation.
 * Designed For internal JSON parsing, so it's messy.
 *
 * @author Yoooooory
 */
class ConversationSkin private constructor(
    val config: ConfigurationSection,
    private val baseText: JSONText,
    private val baseIndex: Map<TypedValue, Int>,
    val fallbackVariables: Map<String, TypedValue?>
) {

    fun getRequiredVariables() = fallbackVariables.keys

    private fun getText(prompt: AbstractQuestPrompt, context: ConversationContext, choice: (Player) -> IJSONTextElement): JSONText {
        val text = cloneText()
        val player = context.forWhom as Player
        val conversation = context.getSessionData(Conversation.OBJECT) as Conversation
        baseIndex.forEach { (key, index) ->
            text[index] = when (key.type) {
                "prop" -> {
                    when (key.value) {
                        "owner" -> PlainTextElement(conversation.owner)
                        "text" -> prompt.message
                        "choice" -> choice.invoke(player)
                        else -> error("Unknown value ${key.value}.")
                    }
                }
                "var" -> getTextElement(player, prompt.variables[key.value]!!)
                else -> error("Unknown type ${key.type}.")
            }
        }
        println(text)
        return text
    }

    fun getMessageText(prompt: QuestMessagePrompt, context: ConversationContext): JSONText {
        return getText(prompt, context) {
            val button = config.getConfigurationSection("choice.message.button")!!
            val btnText = button.getString("text")!!.toTypedValue("text")
            val btnHover = button.getString("hover-text")!!.toTypedValue("text")
            getTextElement(it, btnText).apply {
                clickEvent = ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, "/quest input ${it.getLocaleMessage("conversation.next-message")!!}"
                )
                hoverEvent = HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, getTextElement(it, btnHover)
                )
            }
        }
    }

    fun getPointerText(prompt: QuestPointerPrompt, context: ConversationContext): JSONText {
        return getText(prompt, context) {
            val button = config.getConfigurationSection("choice.pointer.button")!!
            val btnText = button.getString("text")!!.toTypedValue("text")
            val btnHover = button.getString("hover-text")!!.toTypedValue("text")
            if (prompt.pointers.isNotEmpty()) {
                JSONText().apply {
                    prompt.pointers.forEachIndexed { index, pointer ->
                        add(PlainTextElement(getString(it, btnText).arg(index + 1, pointer.showText)).apply {
                            clickEvent = ClickEvent(
                                ClickEvent.Action.RUN_COMMAND, "/quest input ${pointer.requiredInput}"
                            )
                            hoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, getTextElement(it, btnHover)
                            )
                        })
                    }
                }
            } else PlainTextElement("")
        }
    }

    private fun getTextElement(target: CommandSender, typedValue: TypedValue): JSONTextElement {
        val value = typedValue.value
        return when (typedValue.type) {
            "text" -> PlainTextElement(value)
            "locale" -> PlainTextElement(target.getLocaleMessage(value)!!)
            "json" -> object : JSONTextElement() {
                override fun toJsonTree() = JsonParser().parse(value) as JsonObject
            }
            "translate" -> TranslateElement(value)
            else -> error("Unknown type ${typedValue.type}")
        }
    }

    private fun getString(target: CommandSender, typedValue: TypedValue): String {
        val value = typedValue.value
        return when (typedValue.type) {
            "text" -> value
            "locale" -> target.getLocaleMessage(value)!!
            "translate" -> TODO()
            else -> error("Unknown type ${typedValue.type}")
        }.replace("\\n", "\n")
    }

    private fun cloneText() = baseText.clone() as JSONText

    companion object {

        lateinit var CURRENT: ConversationSkin

        fun parseConfig(config: ConfigurationSection, fallback: ConfigurationSection): ConversationSkin {
            fallback.getKeys(true).forEach { config.addDefault(it, fallback[it]) }
            val template = config.getString("template")!!.replace("\\n", "\n")
            val fallbackSection = config.getConfigurationSection("fallback.variables")
            val baseText = JSONText()
            val baseIndex = mutableMapOf<TypedValue, Int>()
            val fallbackVariables = mutableMapOf<String, TypedValue?>()
            var end = 0
            var index = 0
            Regex("\\{([^{}]+)}").findAll(template).forEach {
                val start = it.range.first
                if (start != 0) {
                    baseText += PlainTextElement(template.substring(end until start))
                    index++
                }
                end = it.range.last + 1
                val group = it.groups[1]!!.value.toTypedValue("prop")
                if (group.type == "var") {
                    fallbackVariables[group.value] = fallbackSection!!.getString(group.value)?.toTypedValue("text")
                }
                baseText += PlainTextElement("")
                baseIndex[group] = index++
            }
            baseText += PlainTextElement(template.substring(end))
            println(baseText)
            return ConversationSkin(config, baseText, baseIndex, fallbackVariables)
        }

    }

}