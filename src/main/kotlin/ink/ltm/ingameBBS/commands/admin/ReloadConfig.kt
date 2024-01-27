package ink.ltm.ingameBBS.commands.admin

import ink.ltm.ingameBBS.IngameBBS
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

class ReloadConfig {
    @Description("Reload the config")
    @CommandPermission("ingamebbs.admin.reload")
    @Command("igb reload")
    fun reloadConfig(actor: BukkitCommandActor) {
        IngameBBS.instance.reloadConfig()
        IngameBBS.instance.onConfigLoad()
        actor.reply("Reload config successfully")
    }
}