package ink.ltm.ingameBBS.data

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object SignInfos : IntIdTable() {
    val uniqueID = varchar("uniqueID", 36).uniqueIndex()
    val creator = reference("creator", SignUsers.playerUUID)

    val createTime = date("createTime") //创建时间，为2022/11/07 11:29这种
    val position = text("position") //放置位置
    val world = text("world")
    val signRemark = text("signContent")

    val isDeleted: Column<Boolean> = bool("isDeleted")
}

data class SignInfoInternal(
    val uniqueID: String,
    val creatorUUID: String,
    val createName: String,
    val position: String,
    val world: String,

    val createTime: String = "",
    val signRemark: String = "null",
    val likeCount: Int = 0,
    val dislikeCount: Int = 0
)

object SignUsers : Table() {
    val playerUUID = varchar("playerUUID", 36).uniqueIndex()
    val playerName = varchar("playerName", 32)
    val playerJoinTime = date("playerJoinTime")

    val isBanned = bool("isBanned")
    override val primaryKey = PrimaryKey(playerUUID, name = "PK_playerUUID")
}

object SignInteracts : IntIdTable() {
    val sign = reference("sign", SignInfos.uniqueID)
    val player = reference("player", SignUsers.playerUUID)

    val interactTime: Column<LocalDate> = date("createTime")
    val type = enumeration("type", InteractType::class)
}