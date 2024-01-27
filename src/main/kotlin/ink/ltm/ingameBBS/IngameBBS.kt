package ink.ltm.ingameBBS

import ink.ltm.ingameBBS.commands.admin.ReloadConfig
import ink.ltm.ingameBBS.commands.player.PlayerCommandBasic
import ink.ltm.ingameBBS.data.SignInfos
import ink.ltm.ingameBBS.data.SignInteracts
import ink.ltm.ingameBBS.data.SignUsers
import ink.ltm.ingameBBS.listeners.AdvancedSignPlaceListener
import ink.ltm.ingameBBS.utils.GeneralUtils.updateConfigObject
import ink.ltm.ingameBBS.utils.convert
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.ktx.supportSuspendFunctions
import java.sql.Connection
import java.util.logging.Logger

class IngameBBS : JavaPlugin(), Listener {
    companion object {
        lateinit var db: Database
        lateinit var instance: IngameBBS
        lateinit var pluginLogger: Logger

        val offlineMap = mutableMapOf<String, Int>()
        val signBlockList = Material.entries.filter { Tag.ALL_SIGNS.isTagged(it) }.filter { it.isBlock }
        val signList = Material.entries.filter { Tag.ALL_SIGNS.isTagged(it) }.filter { it.isItem }

        object NamespacedKeys {
            val advancedSign = NamespacedKey(instance, "AdvancedSign")
            val advancedSignID = NamespacedKey(instance, "AdvancedSignID")
        }

        object ItemInfo {
            var name = "声光电炫彩酷告示牌"
            var lore = "可以进行互动的神秘牌子"
        }

        object SignInfo {
            var icon = "*"
            var waiting = "blahblah"
            var message = """
            信息： <remark><newline>
            详情：由玩家 <creator> 创建于 <date><newline>
            累计：获赞 <like-count> ，获踩 <dislike-count><newline>
            互动选项： <like-button> <dislike-button>
            """
            var likeButton = "点赞"
            var dislikeButton = "点踩"
        }

        object VoteMessage {
            var like = "点赞!"
            var dislike = "点踩!"
            var getLike = "收到 <player> 点赞"
            var getDislike = "收到 <player> 点踩"
            var offlineMessage = "你收到了 <count> 个离线点赞"
            var likeSound = "entity.experience_orb.pickup"
            var dislikeSound = "entity.player.attack.sweep"
        }

        object InteractMessage {
            var created = "创建可互动告示牌成功"
            var removed = "成功移除告示牌"
        }

        object ErrorMessage {
            var notOwner = "你不是这个告示牌的创建者"
            var notExist = "该告示牌不存在"
        }
    }

    override fun onEnable() {
        instance = this
        pluginLogger = this.logger
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        onConfigLoad()
        onConfigChange()
        onDatabaseInit()
        onRecipesInit()
        onRegisterInit()
        logger.info("Plugin Initialized Successfully.")
    }

    override fun onDisable() {
        onClosed()
    }

    fun onConfigLoad() {
        saveDefaultConfig()
        updateConfigObject(ItemInfo, "item", config)
        updateConfigObject(SignInfo, "sign", config)
        updateConfigObject(InteractMessage, "interact", config)
        updateConfigObject(VoteMessage, "vote", config)
        updateConfigObject(ErrorMessage, "error", config)
        logger.info("Config Loaded Successfully")
    }

    private fun onConfigChange() {
        config.options().copyDefaults(false)
        saveConfig()
        logger.info("Config Updated Successfully.")
    }

    private fun onDatabaseInit() {
        db = Database.connect("jdbc:sqlite:${this.dataFolder}/signData.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        TransactionManager.defaultDatabase = db
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(SignUsers)
            SchemaUtils.create(SignInfos)
            SchemaUtils.create(SignInteracts)
        }
        logger.info("Database Initialized Successfully.")
    }

    private fun onRecipesInit() {
        signList.forEach { i ->
            val recipeName = NamespacedKey(this, "advanced_${i.name}_recipe")

            val advancedSign = ItemStack(i)
            val im = advancedSign.itemMeta
            im.displayName(ItemInfo.name.convert())
            val lore = mutableListOf<Component>()
            ItemInfo.lore.split("<newline>").forEach {
                lore.add(it.convert())
            }
            im.lore(lore)
            im.persistentDataContainer.set(NamespacedKeys.advancedSign, PersistentDataType.BOOLEAN, true)
            advancedSign.itemMeta = im

            val recipe =
                ShapelessRecipe(recipeName, advancedSign).addIngredient(1, Material.REDSTONE).addIngredient(1, i)

            Bukkit.addRecipe(recipe)
            logger.info("Recipe of ${i.name} Initialized Successfully.")
        }
    }

    private fun onRegisterInit() {
        onRegisterCommand()
        Bukkit.getPluginManager().registerEvents(AdvancedSignPlaceListener(), this)

        logger.info("Listeners And Command Initialized Successfully.")
    }

    private fun onRegisterCommand() {
        val commandHandler = BukkitCommandHandler.create(this)
        commandHandler.supportSuspendFunctions()
        commandHandler.register(PlayerCommandBasic())
        commandHandler.register(ReloadConfig())
        commandHandler.registerBrigadier()

        logger.info("Command Initialized Successfully.")
    }

    private fun onClosed() {
        Bukkit.resetRecipes()
    }
}
