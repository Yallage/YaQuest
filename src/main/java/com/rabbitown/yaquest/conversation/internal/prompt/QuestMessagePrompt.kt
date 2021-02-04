package com.rabbitown.yaquest.conversation.internal.prompt

import com.rabbitown.yalib.module.chat.text.JSONText
import com.rabbitown.yalib.module.locale.YLocale.Companion.getLocaleMessage
import com.rabbitown.yalib.module.nms.base.entity.EntityPlayer.Companion.asNMS
import com.rabbitown.yaquest.TypedValue
import com.rabbitown.yaquest.conversation.ConversationSkin
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.entity.Player

/**
 * @author Yoooooory
 */
class QuestMessagePrompt(
    message: JSONText,
    val next: QuestPrompt,
    variables: Map<String, TypedValue>
) : AbstractQuestPrompt(message, variables) {

    override fun showPrompt(context: ConversationContext) {
        val player = context.forWhom as Player
        player.asNMS().sendMessage(ConversationSkin.CURRENT.getMessageText(this, context))
    }

    override fun acceptInput(context: ConversationContext, input: String?): Prompt {
        return if (input ?: "" == (context.forWhom as Player).getLocaleMessage("conversation.next-message")) {
            resetShowed(context)
            next
        } else {
            sendInvalidMessage(context.forWhom)
            this
        }
    }

}