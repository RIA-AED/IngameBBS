package ink.ltm.ingameBBS.listeners

import de.tr7zw.nbtapi.NBTItem
import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.data.SignDataInternal
import ink.ltm.ingameBBS.insertSignInfo
import ink.ltm.ingameBBS.utils.calculateCRC32
import kotlinx.datetime.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class AdvancedSignPlaceListener : Listener {
    @EventHandler
    fun onPlaceAdvancedSign(event: BlockPlaceEvent) {
        val block = event.block
        if (block.type in IngameBBS.signList) {
            val player = event.player
            val nbti = NBTItem(event.itemInHand)
            if (nbti.getBoolean("AdvancedSign")) {
                val name = player.name
                val date = Clock.System.todayAt(TimeZone.currentSystemDefault())
                val pos = "${block.x},${block.y},${block.z},${block.world}"
                val uid = calculateCRC32(name+date+pos)
                val data = SignDataInternal(
                    uid,
                    name,
                    date,
                    pos
                )
                IngameBBS.scheduler.runTask(IngameBBS(), kotlinx.coroutines.Runnable { insertSignInfo(data) })
                event.player.sendMessage(IngameBBS.Companion.SignInfo.signCreateSuccess)
            }
        }
    }
}