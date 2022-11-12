package ink.ltm.ingameBBS.listeners

import de.tr7zw.nbtapi.NBTBlock
import de.tr7zw.nbtapi.NBTItem
import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.data.SignDataInternal
import ink.ltm.ingameBBS.utils.calculateCRC32
import ink.ltm.ingameBBS.utils.insertSignInfo
import ink.ltm.ingameBBS.utils.messageConverter
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayAt
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent

class AdvancedSignPlaceListener : Listener {
    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun onPlaceAdvancedSign(event: BlockPlaceEvent) {
        val block = event.block
        if (block.type in IngameBBS.signBlockList) {
            val player = event.player
            if (NBTItem(event.itemInHand).getBoolean("AdvancedSign")) {
                val name = player.name
                val date = Clock.System.todayAt(TimeZone.currentSystemDefault())
                val pos = "${block.x},${block.y},${block.z},${block.world}"
                val uid = calculateCRC32(name + date + pos)
                val data = SignDataInternal(
                    uid,
                    name,
                    date,
                    pos,
                    0,
                    0,
                    mutableListOf(),
                    "",
                    false
                )
                GlobalScope.launch {
                    insertSignInfo(data)
                }
                player.sendMessage(IngameBBS.Companion.SignItemInfo.signCreateSuccess)

                val nbtb = NBTBlock(block)
                nbtb.data.setBoolean("AdvancedSign", true)
                nbtb.data.setString("AdvancedSignID", uid)
            }
        }
    }

    @EventHandler
    fun onChangeSign(event: SignChangeEvent) {
        val block = event.block
        val nbtb = NBTBlock(block)
        if (nbtb.data.getBoolean("AdvancedSign")) {
            event.line(
                0,
                messageConverter(IngameBBS.Companion.SignItemInfo.signIcon)
                    .append(event.line(0)!!)
            )
            nbtb.data.setBoolean("AdvancedSign", false)
        }
    }

    @EventHandler
    fun onClickSign(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        val action = event.action
        if (block != null) {
            if (action == Action.RIGHT_CLICK_BLOCK && block.type in IngameBBS.signBlockList) {
                val nbtb = NBTBlock(block)
                val uid = nbtb.data.getString("AdvancedSignID")
                if (uid.isNotEmpty()) {
                    event.player.performCommand("igbinfo $uid")
                }
            }
        }
    }
}