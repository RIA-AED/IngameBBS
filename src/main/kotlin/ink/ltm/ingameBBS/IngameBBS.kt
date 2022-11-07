package ink.ltm.ingameBBS

import de.tr7zw.nbtapi.NBTItem
import ink.ltm.ingameBBS.data.SignData
import ink.ltm.ingameBBS.listeners.AdvancedSignPlaceListener
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
        val signList = Material.values().filter { it.name.contains("SIGN") }
        val scheduler = Bukkit.getScheduler()
        object SignInfo{
            var signName = "声光电炫彩酷告示牌"
            var signInfo = "可以进行互动的神秘牌子"
            var signCreateSuccess = "创建可互动告示牌成功"
        }
    }

    override fun onEnable() {
        if(!dataFolder.exists()){
            dataFolder.mkdirs()
        }
        onDatabaseInit()
        onRecipesInit()
        onEventInit()

    }

    override fun onDisable() {
        onClosed()
    }

    private fun onDatabaseInit() {
        db = Database.connect("jdbc:sqlite:${this.dataFolder}/signData.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        TransactionManager.defaultDatabase = db
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(SignData)
        }
    }

    private fun onRecipesInit() {
        for(i in signList) {
            val name = NamespacedKey(this,"advanced_${i.name}_recipe")

            var advancedSign = ItemStack(i).asOne()
            val im = advancedSign.itemMeta
            im.displayName(Component.text(SignInfo.signName))
            im.lore(listOf(Component.text(SignInfo.signInfo)))
            advancedSign.itemMeta = im
            val nbti = NBTItem(advancedSign)
            nbti.setBoolean("AdvancedSign",true)
            advancedSign = nbti.item

            val recipe = ShapelessRecipe(name,advancedSign)
                .addIngredient(1,Material.REDSTONE)
                .addIngredient(1,i)

            Bukkit.addRecipe(recipe)
        }
    }

    private fun onEventInit(){
        Bukkit.getPluginManager().registerEvents(AdvancedSignPlaceListener(),this)
    }
    private fun onClosed() {
        Bukkit.resetRecipes()
    }
}
