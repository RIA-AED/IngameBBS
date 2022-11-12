package ink.ltm.ingameBBS.utils

import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.data.InteractType
import ink.ltm.ingameBBS.data.LikesResult
import ink.ltm.ingameBBS.data.SignData
import ink.ltm.ingameBBS.data.SignDataInternal
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun saveAllCache() {
    transaction {
        val snapshot = IngameBBS.cache.asMap()
        for ((_, value) in snapshot) {
            SignData.update({ SignData.uniqueID eq value.uid }) {
                it[signContent] = value.signContent
                it[likeCount] = value.likeCount
                it[dislikeCount] = value.dislikeCount
                it[interactList] = Json.encodeToString(value.interactList)
                it[isDeleted] = value.isDeleted
            }
        }
    }
    Bukkit.getLogger().info("Time-Save Data Successfully.")
}

fun convertQueryToInternal(data: ResultRow): SignDataInternal {
    return SignDataInternal(
        data[SignData.uniqueID],
        data[SignData.creatorName],
        data[SignData.createTime],
        data[SignData.position],
        data[SignData.likeCount],
        data[SignData.dislikeCount],
        Json.decodeFromString(data[SignData.interactList]),
        data[SignData.signContent],
        data[SignData.isDeleted]
    )
}

fun insertSignInfo(data: SignDataInternal) {
    val plainInteractList = Json.encodeToString(data.interactList)
    transaction {
        addLogger(StdOutSqlLogger)
        SignData.insert {
            it[uniqueID] = data.uid
            it[creatorName] = data.creatorName
            it[createTime] = data.createTime
            it[position] = data.position
            it[likeCount] = data.likeCount
            it[dislikeCount] = data.dislikeCount
            it[interactList] = plainInteractList
            it[signContent] = data.signContent
            it[isDeleted] = data.isDeleted
        }
    }
}

fun lookupSignInfo(uid: String): SignDataInternal? {
    var returnValue: SignDataInternal? = null
    if (IngameBBS.cache.get(uid) != null) {
        returnValue = IngameBBS.cache.get(uid)
    } else {
        transaction {
            val data = SignData.select { SignData.uniqueID eq uid }.singleOrNull() ?: return@transaction null
            returnValue = convertQueryToInternal(data)
            IngameBBS.cache.put(uid, returnValue!!)
        }
    }
    return returnValue
}

fun modifySignLikes(uid: String, type: Boolean, player: String): LikesResult {
    var returnValue = LikesResult.NO_SUCH_DATA
    val data = if (IngameBBS.cache.get(uid) != null) {
        IngameBBS.cache.get(uid)
    } else {
        transaction {
            val data = SignData.select { SignData.uniqueID eq uid }.singleOrNull()
            if (data != null) {
                convertQueryToInternal(data)
            } else {
                null
            }
        }
    }
    if (data != null) {
        val interactList = data.interactList
        var likeCount = data.likeCount
        var dislikeCount = data.dislikeCount
        val tmp = interactList.indexOfFirst { it.player == player }
        if (tmp == -1) {
            interactList.add(InteractType(player, type))
            if (type) {
                likeCount += 1
            } else {
                dislikeCount += 1
            }
            returnValue = LikesResult.SUCCESS
        } else {
            val value = interactList[tmp]
            if (value.type == type) {
                interactList.removeAt(tmp)
                if (type) {
                    likeCount -= 1
                } else {
                    dislikeCount -= 1
                }
                returnValue = LikesResult.CANCEL_SAME
            } else {
                interactList[tmp].type = type
                if (type) {
                    likeCount += 1
                    dislikeCount -= 1
                } else {
                    likeCount -= 1
                    dislikeCount += 1
                }
                returnValue = LikesResult.CANCEL_DIFFERENT
            }
        }
        data.likeCount = likeCount
        data.dislikeCount = dislikeCount
        data.interactList = interactList
        IngameBBS.cache.put(uid, data)
    }
    return returnValue
}