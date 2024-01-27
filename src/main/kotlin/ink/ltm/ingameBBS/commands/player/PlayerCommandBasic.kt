package ink.ltm.ingameBBS.commands.player

import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.data.InteractType
import ink.ltm.ingameBBS.utils.DatabaseUtils
import ink.ltm.ingameBBS.utils.DatabaseUtils.checkSign
import ink.ltm.ingameBBS.utils.DatabaseUtils.pushSignInteract
import ink.ltm.ingameBBS.utils.GeneralUtils
import ink.ltm.ingameBBS.utils.GeneralUtils.soundBuilder
import ink.ltm.ingameBBS.utils.convert
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.bukkit.annotation.CommandPermission

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
            val value = DatabaseUtils.lookupSignInfo(signID)
            val player = Bukkit.getPlayer(value!!.creatorUUID)
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
            actor.sendMessage(IngameBBS.Companion.ErrorMessage.notExist.convert())
        }
    }

    @Description("Get a Sign Info")
    @CommandPermission("ingamebbs.player.info")
    @Command("igb info")
    suspend fun getSignInfo(actor: Player, signID: String) {
        actor.sendMessage(IngameBBS.Companion.SignInfo.waiting.convert())
        val value = DatabaseUtils.lookupSignInfo(signID)
        val signInfo = value?.let { GeneralUtils.buildSignInfo(it) }
        val result = signInfo ?: (IngameBBS.Companion.ErrorMessage.notExist).convert()
        actor.sendMessage(result)
    }

    @Description("Like a sign")
    @CommandPermission("ingamebbs.player.vote")
    @Command("igb like")
    suspend fun likeSign(actor: Player, signID: String) {
        interactSign(
            actor,
            signID,
            InteractType.LIKE,
            IngameBBS.Companion.VoteMessage.like.convert(),
            soundBuilder(IngameBBS.Companion.VoteMessage.likeSound),
            IngameBBS.Companion.VoteMessage.getLike
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
            IngameBBS.Companion.VoteMessage.dislike.convert(),
            soundBuilder(IngameBBS.Companion.VoteMessage.dislikeSound),
            IngameBBS.Companion.VoteMessage.getDislike
        )
    }


    /*
    @Description("Add a remark for sign")
    @CommandPermission("ingamebbs.player.vote")
    @Command("igb remark")
    suspend fun remarkSign(actor: BukkitCommandActor, signID: String, remark: String) {
        checkSign(signID)
    }*/
}