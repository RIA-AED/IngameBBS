package ink.ltm.ingameBBS.utils

import ink.ltm.ingameBBS.data.Config
import ink.ltm.ingameBBS.data.InteractType
import ink.ltm.ingameBBS.data.SignInfoInternal
import ink.ltm.ingameBBS.utils.DatabaseUtils.calculateSignInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

object GeneralUtils {
    fun updateConfigObject(obj: Any, prefix: String, config: FileConfiguration) {
        obj::class.declaredMemberProperties.forEach {
            val mutableProperty = it as KMutableProperty1<*, *>
            mutableProperty.apply {
                isAccessible = true
                val configValue = config.get("$prefix.${mutableProperty.name}")
                if (configValue != null) {
                    println("$name has changed to $configValue")
                    setter.call(obj, configValue)
                } else {
                    println("No config value found for property $name")
                }
            }
        }
    }

    fun checkItemPDCTrue(item: ItemStack, key: NamespacedKey): Boolean {
        return item.itemMeta.persistentDataContainer.has(
            key,
            PersistentDataType.BOOLEAN
        ) && item.itemMeta.persistentDataContainer.get(key, PersistentDataType.BOOLEAN) == true
    }

    fun checkSignPDCTrue(sign: Sign, key: NamespacedKey): Boolean {
        return sign.persistentDataContainer.has(
            key,
            PersistentDataType.BOOLEAN
        ) && sign.persistentDataContainer.get(key, PersistentDataType.BOOLEAN) == true
    }

    fun getSignPDCValue(sign: Sign, key: NamespacedKey): String? {
        return sign.persistentDataContainer.get(key, PersistentDataType.STRING)
    }

    fun getTime(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    fun soundBuilder(key: String): Sound {
        val sound = Sound.sound(Key.key(key), Sound.Source.MASTER, 1f, 1f)
        return sound
    }

    suspend fun buildSignInfo(sign: SignInfoInternal): Component {
        val creator = sign.createName
        val time = sign.createTime
        val content = sign.signContent
        val interactList = calculateSignInfo(sign.uniqueID)
        val likeCount = interactList.count { it.second == InteractType.LIKE }
        val dislikeCount = interactList.count { it.second == InteractType.DISLIKE }
        val uniqueID = sign.uniqueID
        val likeButton = Config.SignInfo.likeButton.convert().clickEvent(ClickEvent.runCommand("/igb like $uniqueID"))
        val dislikeButton =
            Config.SignInfo.dislikeButton.convert().clickEvent(ClickEvent.runCommand("/igb dislike $uniqueID"))
        val component = MiniMessage.miniMessage().deserialize(
            Config.SignInfo.message,
            Placeholder.component("content", Component.text(content)),
            Placeholder.component("creator", Component.text(creator)),
            Placeholder.component("date", Component.text(time)),
            Placeholder.component("like-count", Component.text(likeCount.toString())),
            Placeholder.component("dislike-count", Component.text(dislikeCount.toString())),
            Placeholder.component("like-button", likeButton),
            Placeholder.component("dislike-button", dislikeButton)
        )
        return component
    }
}

fun String.convert(): Component {
    return MiniMessage.miniMessage().deserialize(this)
}