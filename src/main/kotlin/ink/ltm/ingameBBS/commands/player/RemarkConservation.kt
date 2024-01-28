package ink.ltm.ingameBBS.commands.player

import ink.ltm.ingameBBS.data.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt

class RemarkConservation : ValidatingPrompt() {
    override fun getPromptText(context: ConversationContext): String {
        val message = LegacyComponentSerializer.legacyAmpersand().serialize(
            MiniMessage.miniMessage().deserialize(
                Config.InteractMessage.inputPrompt,
                Placeholder.component("exit", Component.text(Config.InteractMessage.exit))
            )
        )
        return message
    }

    override fun isInputValid(context: ConversationContext, input: String): Boolean {
        if (input.length >= Config.InteractMessage.length) {
            context.forWhom.sendRawMessage(
                LegacyComponentSerializer.legacyAmpersand().serialize(
                    MiniMessage.miniMessage().deserialize(
                        Config.ErrorMessage.tooLong,
                        Placeholder.component("length", Component.text(Config.InteractMessage.length.toString()))
                    )
                )
            )
            return false
        }
        return true
    }

    override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
        if (input == Config.InteractMessage.exit) {
            return Prompt.END_OF_CONVERSATION
        }
        context.setSessionData("remark", input)
        return Prompt.END_OF_CONVERSATION
    }
}