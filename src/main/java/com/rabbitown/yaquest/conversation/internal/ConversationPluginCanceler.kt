package com.rabbitown.yaquest.conversation.internal

import org.bukkit.conversations.ManuallyAbandonedConversationCanceller

/**
 * @author Yoooooory
 */
class ConversationPluginCanceler(val removeFromList: Boolean = true) : ManuallyAbandonedConversationCanceller()