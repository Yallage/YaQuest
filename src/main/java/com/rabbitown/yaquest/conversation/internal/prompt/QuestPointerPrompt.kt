package com.rabbitown.yaquest.conversation.internal.prompt

import com.rabbitown.yalib.module.nms.base.entity.EntityPlayer.Companion.asNMS
import com.rabbitown.yaquest.TypedValue
import com.rabbitown.yaquest.conversation.ConversationSkin
import com.rabbitown.yaquest.conversation.pointer.QuestPointer
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.entity.Player

/**
 * @author Yoooooory
 */
open class QuestPointerPrompt(
    owner: String,
    message: String,
    val pointers: List<QuestPointer>,
    variables: Map<String, TypedValue>
) : AbstractQuestPrompt(owner, message, variables) {

    override fun blocksForInput(context: ConversationContext) = pointers.isNotEmpty()

    override fun showPrompt(context: ConversationContext) {
        val player = context.forWhom as Player
        player.asNMS().sendMessage(ConversationSkin.CURRENT.getPointerText(this, context))
        if (pointers.isEmpty()) {
            sendLocaleMessage(player, "conversation.end")
        }
    }

    override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
        if (pointers.isEmpty()) return Prompt.END_OF_CONVERSATION
        pointers.forEach {
            if (it.isRequiredInput(input)) {
                resetShowed(context)
                return it.getNextPrompt(context).toQuestPrompt()
            }
        }

        // If no matches, then return this prompt again.
        sendInvalidMessage(context.forWhom)
        return this
    }

}