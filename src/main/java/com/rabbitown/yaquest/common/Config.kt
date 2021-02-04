package com.rabbitown.yaquest.common

import com.rabbitown.yalib.util.ExtendFunction.Companion.arg
import com.rabbitown.yalib.util.FileUtil
import com.rabbitown.yaquest.YaQuest
import com.rabbitown.yaquest.conversation.ConversationSkin
import com.rabbitown.yaquest.conversation.internal.QuestConversation
import com.rabbitown.yaquest.conversation.internal.QuestConversationFactory
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration

/**
 * @author Yoooooory
 */
object Config {

    private val plugin = YaQuest.instance
    private lateinit var config: FileConfiguration

    lateinit var conversationFactory: QuestConversationFactory
        private set

    fun reload() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        config = plugin.config

        conversationFactory = QuestConversationFactory().withTimeout(config.getInt("conversation.timeout"))
            .withLocalEcho(false)
            .addConversationAbandonedListener(QuestConversation.ABANDONED_LISTENER) as QuestConversationFactory

        FileUtil.saveResource(plugin, "skins.yml")
        val skins = YamlConfiguration.loadConfiguration(plugin.dataFolder.resolve("skins.yml")).apply {
            addDefaults(YamlConfiguration.loadConfiguration(FileUtil.getResource(plugin, "skins.yml")))
        }
        ConversationSkin.CURRENT = ConversationSkin.parseConfig(
            skins.getConfigurationSection("skins.${config.getString("conversation.skin")!!}")!!,
            skins.getConfigurationSection("skins.${skins.getString("fallback") ?: "default"}")!!
        )
    }


    fun getConversationPrefix(owner: String) = config.getString("conversation.prefix")!!.arg(owner)

    fun getSelectedSkin(): ConversationSkin {
        TODO()
    }

}