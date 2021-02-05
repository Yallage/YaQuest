package com.rabbitown.yaquest.conversation

import com.rabbitown.yalib.module.locale.YLocale
import com.rabbitown.yaquest.TypedValue.Companion.toTypedValue
import com.rabbitown.yaquest.conversation.internal.QuestConversation
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets

/**
 * @author Yoooooory
 */
class Conversation(
    val owner: String,
    val firstPrompt: String,
    val prompts: Map<String, Prompt>
) {

    fun begin(player: Player) {
        val conversation = ConversationManager.factory
            .withFirstPrompt(getFirstPrompt()).buildConversation(player) as QuestConversation
        conversation.context.setSessionData(OBJECT, this)
        conversation.start()
    }

    fun getFirstPrompt() = prompts[firstPrompt]!!.toQuestPrompt()

    companion object {

        val OBJECT = object {}

        fun parseFile(file: File) = parseFile(file.bufferedReader(StandardCharsets.UTF_8))

        fun parseFile(reader: Reader): Conversation {
            val config = YamlConfiguration.loadConfiguration(reader)
            val owner = config.getString("owner") ?: YLocale.getConsoleMessage("conversation.unknown-owner")!!
            val firstPrompt = config.getString("first")!!

            val fallbackVarSection = config.getConfigurationSection("fallback.variables")
            val fallbackVariables = ConversationSkin.CURRENT.getRequiredVariables()
                .map { it to (fallbackVarSection!!.getString(it)?.toTypedValue("text") ?: ConversationSkin.CURRENT.fallbackVariables[it]!!) }.toMap()
            val promptSection = config.getConfigurationSection("prompts")!!
            val prompts = promptSection.getKeys(false)
                .map { it to Prompt.parseConfig(promptSection.getConfigurationSection(it)!!, owner, fallbackVariables) }.toMap()
            if (firstPrompt !in prompts) error("First prompt $firstPrompt not found.")

            return Conversation(owner, firstPrompt, prompts)
        }

    }

}