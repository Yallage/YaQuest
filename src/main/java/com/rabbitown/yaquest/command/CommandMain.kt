package com.rabbitown.yaquest.command

import com.rabbitown.yalib.module.command.CommandSenderType
import com.rabbitown.yalib.module.command.SimpleCommandRemote
import com.rabbitown.yalib.module.command.annotation.Access
import com.rabbitown.yalib.module.command.annotation.Action
import com.rabbitown.yalib.module.locale.YLocale.Companion.sendLocale
import com.rabbitown.yalib.util.FileUtil
import com.rabbitown.yaquest.YaQuest
import com.rabbitown.yaquest.conversation.Conversation
import com.rabbitown.yaquest.conversation.ConversationManager
import org.bukkit.entity.Player

/**
 * @author Yoooooory
 */
class CommandMain : SimpleCommandRemote("YaQuest", YaQuest.instance, listOf("quest")) {

    @Action("reload")
    @Access(["yaquest.admin.reload"])
    fun reload() = YaQuest.instance.reload()

    @Action("test")
    @Access(sender = [CommandSenderType.PLAYER])
    fun test(sender: Player) {
        Conversation.parseFile(FileUtil.getResource(YaQuest.instance, "conversation/example.yml")).begin(sender)
    }

    @Action("input {input}")
    @Access(sender = [CommandSenderType.PLAYER])
    fun input(sender: Player, input: String) {
        if (sender.isConversing) sender.acceptConversationInput(input)
        else sender.sendLocale("conversation.not-in")
    }

}