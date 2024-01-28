package ink.ltm.ingameBBS

import ink.ltm.ingameBBS.commands.admin.ReloadConfig
import ink.ltm.ingameBBS.commands.player.PlayerCommandBasic
import ink.ltm.ingameBBS.data.Config
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
    }

    override fun onEnable() {
        instance = this
        pluginLogger = this.logger
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        onConfigLoad()
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
        config.options().copyDefaults(true)
        saveConfig()
        updateConfigObject(Config.ItemInfo, "item", config)
        updateConfigObject(Config.SignInfo, "sign", config)
        updateConfigObject(Config.InteractMessage, "interact", config)
        updateConfigObject(Config.VoteMessage, "vote", config)
        updateConfigObject(Config.ErrorMessage, "error", config)
        logger.info("Config Loaded Successfully")
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
            SchemaUtils.createMissingTablesAndColumns(SignUsers, SignInfos, SignInteracts)
        }
        logger.info("Database Initialized Successfully.")
    }

    private fun onRecipesInit() {
        signList.forEach { i ->
            val recipeName = NamespacedKey(this, "advanced_${i.name}_recipe")

            val advancedSign = ItemStack(i)
            val im = advancedSign.itemMeta
            im.displayName(Config.ItemInfo.name.convert())
            val lore = mutableListOf<Component>()
            Config.ItemInfo.lore.split("<newline>").forEach {
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
