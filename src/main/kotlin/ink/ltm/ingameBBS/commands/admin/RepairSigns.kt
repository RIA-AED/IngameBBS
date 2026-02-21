package ink.ltm.ingameBBS.commands.admin

import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.utils.DatabaseUtils
import ink.ltm.ingameBBS.utils.GeneralUtils.applyAdvancedSignText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.bukkit.annotation.CommandPermission

class RepairSigns {
    @Description("Repair existing advanced signs")
    @CommandPermission("ingamebbs.admin.repair")
    @Command("igb repair-signs")
    fun repairSigns(actor: CommandSender) {
        actor.sendMessage("Repair started... This may take a moment.")
        Bukkit.getScheduler().runTaskAsynchronously(IngameBBS.instance, Runnable {
            val records = DatabaseUtils.listSignLocations()
            Bukkit.getScheduler().runTask(IngameBBS.instance, Runnable {
                var repaired = 0
                var missingWorld = 0
                var missingBlock = 0
                var notSign = 0
                var skipped = 0

                records.forEach { record ->
                    val world = Bukkit.getWorld(record.world)
                    if (world == null) {
                        missingWorld++
                        return@forEach
                    }
                    val parts = record.position.split(",")
                    if (parts.size != 3) {
                        skipped++
                        return@forEach
                    }
                    val x = parts[0].trim().toIntOrNull()
                    val y = parts[1].trim().toIntOrNull()
                    val z = parts[2].trim().toIntOrNull()
                    if (x == null || y == null || z == null) {
                        skipped++
                        return@forEach
                    }
                    val block = world.getBlockAt(x, y, z)
                    if (block.type !in IngameBBS.signBlockList) {
                        missingBlock++
                        return@forEach
                    }
                    val sign = block.state as? Sign
                    if (sign == null) {
                        notSign++
                        return@forEach
                    }

                    sign.persistentDataContainer.set(
                        IngameBBS.Companion.NamespacedKeys.advancedSign,
                        PersistentDataType.BOOLEAN,
                        true
                    )
                    sign.persistentDataContainer.set(
                        IngameBBS.Companion.NamespacedKeys.advancedSignID,
                        PersistentDataType.STRING,
                        record.uniqueID
                    )

                    val plain = PlainTextComponentSerializer.plainText()
                    val frontLines = sign.getSide(org.bukkit.block.sign.Side.FRONT).lines()
                    val backLines = sign.getSide(org.bukkit.block.sign.Side.BACK).lines()
                    val frontHasText = frontLines.any { plain.serialize(it).isNotBlank() }
                    val backHasText = backLines.any { plain.serialize(it).isNotBlank() }
                    val sourceLines = when {
                        frontHasText -> frontLines
                        backHasText -> backLines
                        else -> listOf(Component.empty(), Component.empty(), Component.empty(), Component.empty())
                    }

                    val normalizedLines = listOf(
                        sourceLines.getOrNull(0) ?: Component.empty(),
                        sourceLines.getOrNull(1) ?: Component.empty(),
                        sourceLines.getOrNull(2) ?: Component.empty(),
                        sourceLines.getOrNull(3) ?: Component.empty()
                    )

                    applyAdvancedSignText(sign, record.uniqueID, normalizedLines)
                    repaired++
                }

                actor.sendMessage(
                    "Repair finished. repaired=$repaired, missingWorld=$missingWorld, " +
                        "missingBlock=$missingBlock, notSign=$notSign, skipped=$skipped"
                )
            })
        })
    }
}
