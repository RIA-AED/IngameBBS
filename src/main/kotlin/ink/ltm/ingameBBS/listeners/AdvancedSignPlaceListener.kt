package ink.ltm.ingameBBS.listeners

import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.data.Config
import ink.ltm.ingameBBS.data.SignInfoInternal
import ink.ltm.ingameBBS.utils.DatabaseUtils
import ink.ltm.ingameBBS.utils.DatabaseUtils.insertSignInfo
import ink.ltm.ingameBBS.utils.GeneralUtils
import ink.ltm.ingameBBS.utils.GeneralUtils.checkSignPDCTrue
import ink.ltm.ingameBBS.utils.GeneralUtils.getSignPDCValue
import ink.ltm.ingameBBS.utils.convert
import kotlinx.coroutines.Runnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.ClickEvent.clickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*


class AdvancedSignPlaceListener : Listener {
    @EventHandler
    fun onPlaceAdvancedSign(event: BlockPlaceEvent) {
        val block = event.block
        if (block.type in IngameBBS.signBlockList) {
            val player = event.player
            val item = event.itemInHand
            if (GeneralUtils.checkItemPDCTrue(item, IngameBBS.Companion.NamespacedKeys.advancedSign)) {
                val signUUID = UUID.randomUUID().toString()
                val playerUUID = player.uniqueId.toString()
                val playerName = player.name
                val pos = "${block.x},${block.y},${block.z}"
                val world = block.world.name
                val data = SignInfoInternal(
                    signUUID, playerUUID, playerName, pos, world
                )
                Bukkit.getScheduler().runTaskAsynchronously(IngameBBS.instance, Runnable {
                    insertSignInfo(data)
                })

                if (block.state is Sign) {
                    val sign = block.state as Sign
                    sign.persistentDataContainer.set(
                        IngameBBS.Companion.NamespacedKeys.advancedSign, PersistentDataType.BOOLEAN, true
                    )
                    sign.persistentDataContainer.set(
                        IngameBBS.Companion.NamespacedKeys.advancedSignID, PersistentDataType.STRING, signUUID
                    )
                    sign.update()
                }
                player.sendMessage(Config.InteractMessage.created.convert())
            }
        }
    }

    @EventHandler
    fun onChangeSign(event: SignChangeEvent) {
        val block = event.block.state as Sign
        val currentSide = event.side.name
        if (checkSignPDCTrue(block, IngameBBS.Companion.NamespacedKeys.advancedSign)) {
            val icon = Config.SignInfo.icon.convert()
            val commandEvent1 = clickEvent(
                ClickEvent.Action.RUN_COMMAND, "igb info ${
                    block.persistentDataContainer.get(
                        IngameBBS.Companion.NamespacedKeys.advancedSignID, PersistentDataType.STRING
                    )
                }"
            )
            val tmp = event.line(0)!!
            event.line(
                0, icon.clickEvent(commandEvent1).append(tmp)
            )




            val commandEvent2 = clickEvent(
                ClickEvent.Action.RUN_COMMAND, "igb info ${
                    block.persistentDataContainer.get(
                        IngameBBS.Companion.NamespacedKeys.advancedSignID, PersistentDataType.STRING
                    )
                }"
            )
            Side.entries.single { it.name != currentSide }.let { side ->
                val signSide = block.getSide(side)
                signSide.line(0, icon.clickEvent(commandEvent2).append(tmp))
                event.lines().forEachIndexed { index, line ->
                    if (index != 0) {
                        signSide.line(index, line)
                    }
                }
            }
            block.update()
            block.isWaxed = true
            block.update()
        }
    }

    @EventHandler
    fun onRemoveSign(event: BlockBreakEvent) {
        val block = event.block
        if (block.type in IngameBBS.signBlockList && checkSignPDCTrue(
                block.state as Sign,
                IngameBBS.Companion.NamespacedKeys.advancedSign
            )
        ) {
            getSignPDCValue(block.state as Sign, IngameBBS.Companion.NamespacedKeys.advancedSignID)?.let {
                Bukkit.getScheduler().runTaskAsynchronously(IngameBBS.instance, Runnable {
                    DatabaseUtils.deleteSignInfo(it)
                })
            }

            val sign = (block.state as Sign)
            sign.persistentDataContainer.remove(IngameBBS.Companion.NamespacedKeys.advancedSign)
            sign.persistentDataContainer.remove(IngameBBS.Companion.NamespacedKeys.advancedSignID)
            sign.update()
            event.player.sendMessage(Config.InteractMessage.removed.convert())
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId.toString()
        if (IngameBBS.offlineMap[uuid] != null) {
            player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    Config.VoteMessage.offlineMessage,
                    Placeholder.component("count", Component.text(IngameBBS.offlineMap[uuid].toString()))
                )
            )
            IngameBBS.offlineMap.remove(uuid)
        }
    }
}