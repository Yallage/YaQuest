package com.rabbitown.yaquest.common

import com.rabbitown.yalib.module.locale.YLocale
import com.rabbitown.yaquest.YaQuest
import org.bukkit.Bukkit

/**
 * @author Yoooooory
 */
object Logger {

    private val prefix = YLocale.getConsoleMessage("prefix")

    fun info(message: String) = Bukkit.getConsoleSender().sendMessage(prefix + message)
    fun severe(message: String?) = YaQuest.instance.logger.severe(message)
    fun warning(message: String?) = YaQuest.instance.logger.warning(message)

}