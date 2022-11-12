package ink.ltm.ingameBBS

import de.tr7zw.nbtapi.NBTItem
import ink.ltm.ingameBBS.commands.PlayerDislikeCommandHandler
import ink.ltm.ingameBBS.commands.PlayerInfoCommandHandler
import ink.ltm.ingameBBS.commands.PlayerLikeCommandHandler
import ink.ltm.ingameBBS.data.SignData
import ink.ltm.ingameBBS.data.SignDataInternal
import ink.ltm.ingameBBS.listeners.AdvancedSignPlaceListener
import ink.ltm.ingameBBS.utils.messageConverter
import ink.ltm.ingameBBS.utils.saveAllCache
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class IngameBBS : JavaPlugin(), Listener {
    companion object {
        lateinit var db: Database
        val signBlockList = Material.values().filter { it.name.contains("SIGN") }
        val signList = signBlockList.filter { it.isItem }
        val cache = Cache.Builder()
            .maximumCacheSize(200)
            .build<String, SignDataInternal>()
        const val oneSec = 20L

        object SignItemInfo {
            var signName = "声光电炫彩酷告示牌"
            var signInfo = "可以进行互动的神秘牌子"
            var signCreateSuccess = "创建可互动告示牌成功"
            var signIcon = "*"
        }

        object SignInfo {
            var signInfoDeco = "blahblah"
            var signContent = "信息：%content"
            var signDetail = "详情：由玩家 %player 创建于 %date"
            var signLikes = "累计：获赞 %like ，获踩 %dislike"
            var signInteract = "互动选项："
            var signLike = "blahblah"
            var signDislike = "blahblah"
        }

        object SignVoteInfo {
            var voteLike = "点赞!"
            var voteDislike = "点踩!"
            var voteChange = "已经更改了原先的评价!"
            var voteCancel = "已经取消了原先的评价!"
        }
    }

    override fun onEnable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        onConfigInit()
        onDatabaseInit()
        onRecipesInit()
        onRegisterInit()
        logger.info("Plugin Initialized Successfully.")
    }

    override fun onDisable() {
        onClosed()
    }

    private fun onConfigInit() {
        saveDefaultConfig()
        SignItemInfo.signName = config.getString("item.name") ?: SignItemInfo.signName
        SignItemInfo.signInfo = config.getString("item.info") ?: SignItemInfo.signInfo
        SignItemInfo.signCreateSuccess = config.getString("item.createSuccess") ?: SignItemInfo.signCreateSuccess
        SignItemInfo.signIcon = config.getString("item.icon") ?: SignItemInfo.signIcon

        SignInfo.signInfoDeco = config.getString("info.infoDeco") ?: SignInfo.signInfoDeco
        SignInfo.signContent = config.getString("info.content") ?: SignInfo.signContent
        SignInfo.signDetail = config.getString("info.detail") ?: SignInfo.signDetail
        SignInfo.signLikes = config.getString("info.likes") ?: SignInfo.signLikes
        SignInfo.signInteract = config.getString("info.interact") ?: SignInfo.signInteract
        SignInfo.signLike = config.getString("info.like") ?: SignInfo.signLike
        SignInfo.signDislike = config.getString("info.dislike") ?: SignInfo.signDislike

        SignVoteInfo.voteLike = config.getString("vote.like") ?: SignVoteInfo.voteLike
        SignVoteInfo.voteDislike = config.getString("vote.dislike") ?: SignVoteInfo.voteDislike
        SignVoteInfo.voteChange = config.getString("vote.change") ?: SignVoteInfo.voteChange
        SignVoteInfo.voteCancel = config.getString("vote.cancel") ?: SignVoteInfo.voteCancel
        logger.info("Config Initialized Successfully")
    }

    private fun onDatabaseInit() {
        db = Database.connect("jdbc:sqlite:${this.dataFolder}/signData.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        TransactionManager.defaultDatabase = db
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(SignData)
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable { saveAllCache() }, oneSec * 60, oneSec * 600)
        logger.info("Database Initialized Successfully.")
    }

    private fun onRecipesInit() {
        for (i in signList) {
            val name = NamespacedKey(this, "advanced_${i.name}_recipe")

            val advancedSign = ItemStack(i)
            val im = advancedSign.itemMeta
            im.displayName(messageConverter(SignItemInfo.signName))
            val lists = mutableListOf<Component>()
            for (j in SignItemInfo.signInfo.split("&n")) {
                lists.add(messageConverter(j))
            }
            im.lore(lists)
            advancedSign.itemMeta = im
            advancedSign.amount = 1
            val nbti = NBTItem(advancedSign)
            nbti.setBoolean("AdvancedSign", true)
            nbti.applyNBT(advancedSign)

            val recipe = ShapelessRecipe(name, advancedSign)
                .addIngredient(1, Material.REDSTONE)
                .addIngredient(1, i)

            Bukkit.addRecipe(recipe)
            logger.info("Recipe of ${i.name} Initialized Successfully.")
        }
    }

    private fun onRegisterInit() {
        Bukkit.getPluginManager().registerEvents(AdvancedSignPlaceListener(), this)

        Bukkit.getPluginCommand("igbinfo")?.setExecutor(PlayerInfoCommandHandler())
        Bukkit.getPluginCommand("igblike")?.setExecutor(PlayerLikeCommandHandler())
        Bukkit.getPluginCommand("igbdislike")?.setExecutor(PlayerDislikeCommandHandler())
        logger.info("Listeners And Command Initialized Successfully.")
    }

    private fun onClosed() {
        saveAllCache()
        Bukkit.resetRecipes()
    }
}
