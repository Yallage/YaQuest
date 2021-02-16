package com.rabbitown.yaquest

import com.rabbitown.yalib.YaLibCentral
import com.rabbitown.yalib.module.locale.I18NPlugin
import com.rabbitown.yalib.module.locale.PrefixLocale
import com.rabbitown.yalib.module.locale.YLocale.Companion.sendLocale
import com.rabbitown.yaquest.command.CommandMain
import com.rabbitown.yaquest.common.Config
import com.rabbitown.yaquest.common.Logger
import com.rabbitown.yaquest.conversation.ConversationManager
import com.rabbitown.yaquest.conversation.internal.ConversationPluginCanceler
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Yoooooory
 */
class YaQuest : JavaPlugin(), I18NPlugin {

    init {
        instance = this
    }

    override val locale = PrefixLocale(this, "prefix", "zh_CN")

    override fun onLoad() {
        YaLibCentral.registerPlugin(this)
        registerHandlers()
        reload()
    }

    override fun onEnable() {
        Logger.info("我的B站20粉丝啦！")
    }

    override fun onDisable() {
        Logger.info("难道是，天意如此……")
        ConversationManager.runningConversations.removeIf {
            it.abandon(ConversationAbandonedEvent(it, ConversationPluginCanceler(false)))
            (it.forWhom as Player).sendLocale("conversation.cancel-via-disable")
            true
        }
    }

    private fun registerHandlers() {
        CommandMain().register()
    }

    fun reload() {
        locale.load()
        Config.reload()
        ConversationManager.reload()
    }

    companion object {

        lateinit var instance: YaQuest
            private set

    }

}