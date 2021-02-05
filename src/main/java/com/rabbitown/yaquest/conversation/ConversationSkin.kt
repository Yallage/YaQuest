package com.rabbitown.yaquest.conversation

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.rabbitown.yalib.module.chat.text.IJSONTextElement
import com.rabbitown.yalib.module.chat.text.JSONText
import com.rabbitown.yalib.module.chat.text.JSONTextElement
import com.rabbitown.yalib.module.chat.text.event.ClickEvent
import com.rabbitown.yalib.module.chat.text.event.HoverEvent
import com.rabbitown.yalib.module.chat.text.impl.PlainTextElement
import com.rabbitown.yalib.module.chat.text.impl.TranslateElement
import com.rabbitown.yalib.module.locale.YLocale.Companion.getLocaleMessage
import com.rabbitown.yalib.util.ExtendFunction.Companion.arg
import com.rabbitown.yaquest.TypedValue
import com.rabbitown.yaquest.TypedValue.Companion.toTypedValue
import com.rabbitown.yaquest.conversation.internal.prompt.AbstractQuestPrompt
import com.rabbitown.yaquest.conversation.internal.prompt.QuestMessagePrompt
import com.rabbitown.yaquest.conversation.internal.prompt.QuestPointerPrompt
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

    private fun getText(prompt: AbstractQuestPrompt, context: ConversationContext, choice: (Player, String) -> IJSONTextElement): JSONText {
        val text = cloneText()
        val player = context.forWhom as Player
        baseIndex.forEach { (key, index) ->
            text[index] = when (key.type) {
                "prop" -> {
                    when (key.value) {
                        "owner" -> PlainTextElement(config.getString("owner")!!.arg(prompt.owner))
                        "text" -> {
                            val start = config.getString("text.each-line-start") ?: ""
                            val after = config.getString("text.after")
                            val choices = choice.invoke(player, start)
                            val line = prompt.message.split("\n").size + choices.toString().split("\\n").size
                            val missingLine = config.getInt("text.at-least-line") - line
                            JSONText(PlainTextElement(start + prompt.message + after), choices).apply {
                                for (i in 1..missingLine) add(PlainTextElement("\n"))
                            }
                        }
                        else -> error("Unknown value ${key.value}.")
                    }
                }
                "var" -> getTextElement(player, prompt.variables[key.value]!!, "")
                else -> error("Unknown type ${key.type}.")
            }
        }
        return text
    }

    fun getMessageText(prompt: QuestMessagePrompt, context: ConversationContext): JSONText {
        return getText(prompt, context) { player, start ->
            val button = config.getConfigurationSection("choice.message.button")!!
            val btnText = button.getString("text")!!.toTypedValue("text")
            val btnHover = button.getString("hover-text")!!.toTypedValue("text")
            getTextElement(player, btnText, start).apply {
                clickEvent = ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, "/quest input ${player.getLocaleMessage("conversation.next-message")!!}"
                )
                hoverEvent = HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, getTextElement(player, btnHover, "")
                )
            }
        }
    }

    fun getPointerText(prompt: QuestPointerPrompt, context: ConversationContext): JSONText {
        return getText(prompt, context) { player, start ->
            val button = config.getConfigurationSection("choice.pointer.button")!!
            val btnText = button.getString("text")!!.toTypedValue("text")
            val btnHover = button.getString("hover-text")!!.toTypedValue("text")
            if (prompt.pointers.isNotEmpty()) {
                JSONText().apply {
                    prompt.pointers.forEachIndexed { index, pointer ->
                        add(PlainTextElement(
                            getString(player, btnText).replace("\n", "\n$start").arg(index + 1, pointer.showText)
                        ).apply {
                            clickEvent = ClickEvent(
                                ClickEvent.Action.RUN_COMMAND, "/quest input ${pointer.requiredInput}"
                            )
                            hoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, getTextElement(player, btnHover, "")
                            )
                        })
                    }
                }
            } else PlainTextElement("")
        }
    }

    private fun getTextElement(target: CommandSender, typedValue: TypedValue, start: String): JSONTextElement {
        val value = typedValue.value
        return when (typedValue.type) {
            "text" -> PlainTextElement(value.replace("\n", "\n$start"))
            "locale" -> PlainTextElement(target.getLocaleMessage(value)!!.replace("\n", "\n$start"))
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
            val last = template.substring(end)
            if (last.isNotEmpty()) baseText += PlainTextElement(last)
            return ConversationSkin(config, baseText, baseIndex, fallbackVariables)
        }

    }

}