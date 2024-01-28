package ink.ltm.ingameBBS.commands.player

import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.data.Config
import ink.ltm.ingameBBS.data.InteractType
import ink.ltm.ingameBBS.utils.DatabaseUtils.checkSign
import ink.ltm.ingameBBS.utils.DatabaseUtils.lookupSignInfo
import ink.ltm.ingameBBS.utils.DatabaseUtils.pushSignInteract
import ink.ltm.ingameBBS.utils.DatabaseUtils.updateSignRemark
import ink.ltm.ingameBBS.utils.GeneralUtils
import ink.ltm.ingameBBS.utils.GeneralUtils.soundBuilder
import ink.ltm.ingameBBS.utils.convert
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.conversations.ConversationFactory
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.util.*

class PlayerCommandBasic {
    private suspend fun interactSign(
        actor: Player,
        signID: String,
        interactType: InteractType,
        actorMessage: Component,
        sound: Sound,
        creatorMessage: String
    ) {
        if (checkSign(signID)) {
            pushSignInteract(signID, actor.uniqueId.toString(), actor.name, interactType)
            actor.sendMessage(actorMessage)
            actor.playSound(sound)
            val value = lookupSignInfo(signID)
            val player = Bukkit.getPlayer(UUID.fromString(value!!.creatorUUID))
            if (player != null) {
                player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        creatorMessage, Placeholder.component("player", Component.text(actor.name))
                    )
                )
            } else {
                IngameBBS.offlineMap[value.creatorUUID] = IngameBBS.offlineMap.getOrDefault(
                    value.creatorUUID, 0
                ) + if (interactType == InteractType.LIKE) 1 else -1
            }

        } else {
            actor.sendMessage(Config.ErrorMessage.notExist.convert())
        }
    }

    @Description("Get a Sign Info")
    @CommandPermission("ingamebbs.player.info")
    @Command("igb info")
    suspend fun getSignInfo(actor: Player, signID: String) {
        actor.sendMessage(Config.SignInfo.waiting.convert())
        val value = lookupSignInfo(signID)
        val signInfo = value?.let { GeneralUtils.buildSignInfo(it) }
        if (signInfo != null) {
            actor.sendMessage(signInfo)
            if (UUID.fromString(value.creatorUUID) == actor.uniqueId) {
                val editButton = Config.SignInfo.editButton.convert().clickEvent(
                    ClickEvent.clickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/igb remark $signID"
                    )
                )
                actor.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        Config.SignInfo.ownerMessage,
                        Placeholder.component("edit-button", editButton)
                    )
                )
            }

        } else {
            actor.sendMessage(Config.ErrorMessage.notExist.convert())
        }
    }

    @Description("Like a sign")
    @CommandPermission("ingamebbs.player.vote")
    @Command("igb like")
    suspend fun likeSign(actor: Player, signID: String) {
        interactSign(
            actor,
            signID,
            InteractType.LIKE,
            Config.VoteMessage.like.convert(),
            soundBuilder(Config.VoteMessage.likeSound),
            Config.VoteMessage.getLike
        )
    }

    @Description("Dislike a sign")
    @CommandPermission("ingamebbs.player.vote")
    @Command("igb dislike")
    suspend fun dislikeSign(actor: Player, signID: String) {
        interactSign(
            actor,
            signID,
            InteractType.DISLIKE,
            Config.VoteMessage.dislike.convert(),
            soundBuilder(Config.VoteMessage.dislikeSound),
            Config.VoteMessage.getDislike
        )
    }

    @Description("Add a remark for sign")
    @CommandPermission("ingamebbs.player.vote")
    @Command("igb remark")
    suspend fun remarkSign(actor: Player, signID: String) {
        if (!checkSign(signID)) {
            actor.sendMessage(Config.ErrorMessage.notExist.convert())
            return
        }
        if (actor.uniqueId.toString() != lookupSignInfo(signID)!!.creatorUUID) {
            actor.sendMessage(Config.ErrorMessage.notOwner.convert())
            return
        }
        val conv = ConversationFactory(IngameBBS.instance)
            .withModality(false)
            .withFirstPrompt(RemarkConservation())
            .withTimeout(60)
            .thatExcludesNonPlayersWithMessage(
                LegacyComponentSerializer.legacyAmpersand().serialize(
                    Config.ErrorMessage.notPlayer.convert()
                )
            )
            .buildConversation(actor)
        conv.addConversationAbandonedListener {
            val remark = conv.context.getSessionData("remark") ?: return@addConversationAbandonedListener
            Bukkit.getScheduler().runTaskAsynchronously(IngameBBS.instance, Runnable {
                updateSignRemark(signID, remark.toString())
            })
            actor.sendMessage(Config.InteractMessage.updatedRemark.convert())
            return@addConversationAbandonedListener
        }
        conv.begin()
    }
}


