package ink.ltm.ingameBBS.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun messageConverter(text: String): Component {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(text)
}