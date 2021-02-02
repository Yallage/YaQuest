package com.rabbitown.yaquest

import com.rabbitown.yalib.YaLibCentral
import com.rabbitown.yalib.module.locale.I18NPlugin
import com.rabbitown.yalib.module.locale.Locale
import com.rabbitown.yaquest.util.Logger
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Yoooooory
 */
class YaQuest : JavaPlugin(), I18NPlugin {

    init {
        instance = this
    }

    override val locale = Locale(this, "zh_CN")

    override fun onLoad() {
        YaLibCentral.registerPlugin(this)
        loadConfig()
    }

    override fun onEnable() {
        Logger.info("我已启动，感觉良好！")
    }

    override fun onDisable() {
        Logger.info("难道是，天意如此……")
    }

    fun loadConfig() {
        saveDefaultConfig()
    }

    companion object {

        lateinit var instance: YaQuest
            private set

    }

}