package ink.ltm.ingameBBS.utils

import ink.ltm.ingameBBS.data.*
import ink.ltm.ingameBBS.data.SignInteracts.player
import ink.ltm.ingameBBS.utils.GeneralUtils.getTime
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseUtils {
    private fun insertSignUser(playerName: String, playerUUID: String) {
        return transaction {
            addLogger(StdOutSqlLogger)
            SignUsers.insert {
                it[this.playerName] = playerName
                it[this.playerUUID] = playerUUID
                it[this.playerJoinTime] = getTime()
                it[this.isBanned] = false
            }
            commit()
        }
    }

    suspend fun checkSign(uniqueID: String): Boolean {
        return newSuspendedTransaction(Dispatchers.IO) {
            addLogger(StdOutSqlLogger)
            val result = SignInfos.selectAll().where { SignInfos.uniqueID eq uniqueID }.singleOrNull()
            return@newSuspendedTransaction result != null
        }
    }

    fun insertSignInfo(data: SignInfoInternal) {
        return transaction {
            addLogger(StdOutSqlLogger)
            val preFound = SignUsers.selectAll().where { SignUsers.playerUUID eq data.creatorUUID }.singleOrNull()
            if (preFound == null) {
                insertSignUser(data.createName, data.creatorUUID)
            }
            val userFound = SignUsers.selectAll().where { SignUsers.playerUUID eq data.creatorUUID }.single()
            SignInfos.insert {
                it[this.uniqueID] = data.uniqueID
                it[this.creator] = userFound[SignUsers.playerUUID]
                it[this.createTime] = getTime()
                it[this.position] = data.position
                it[this.world] = data.world
                it[this.signContent] = ""
                it[this.isDeleted] = false
            }
            commit()
        }
    }

    fun deleteSignInfo(uniqueID: String) {
        return transaction {
            addLogger(StdOutSqlLogger)
            SignInfos.update({ SignInfos.uniqueID eq uniqueID }) {
                it[this.isDeleted] = true
            }
            commit()
        }
    }

    fun updateSignRemark(uniqueID: String, remark: String) {
        return transaction {
            addLogger(StdOutSqlLogger)
            SignInfos.update({ SignInfos.uniqueID eq uniqueID }) {
                it[this.signContent] = remark
            }
            commit()
        }
    }

    suspend fun lookupSignInfo(uniqueID: String): SignInfoInternal? {
        return newSuspendedTransaction(Dispatchers.IO) {
            addLogger(StdOutSqlLogger)
            val result = SignInfos.selectAll().where { SignInfos.uniqueID eq uniqueID }.singleOrNull()
            if (result == null) return@newSuspendedTransaction null
            val userRes = SignUsers.selectAll().where { SignUsers.playerUUID eq result[SignInfos.creator] }.single()
            val res = SignInfoInternal(
                uniqueID = result[SignInfos.uniqueID],
                creatorUUID = userRes[SignUsers.playerUUID],
                createName = userRes[SignUsers.playerName],
                position = result[SignInfos.position],
                world = result[SignInfos.world],
                createTime = result[SignInfos.createTime].toString(),
                signContent = result[SignInfos.signContent]
            )
            commit()
            return@newSuspendedTransaction res
        }
    }

    suspend fun calculateSignInfo(uniqueID: String): List<Pair<String, InteractType>> {
        return newSuspendedTransaction(Dispatchers.IO) {
            addLogger(StdOutSqlLogger)
            val result = SignInteracts.selectAll().where { SignInteracts.sign eq uniqueID }.map {
                Pair(it[player], it[SignInteracts.type])
            }
            return@newSuspendedTransaction result
        }
    }

    suspend fun pushSignInteract(signID: String, playerUUID: String, playerName: String, type: InteractType) {
        return newSuspendedTransaction(Dispatchers.IO) {
            addLogger(StdOutSqlLogger)
            val user = SignUsers.selectAll().where { SignUsers.playerUUID eq playerUUID }.singleOrNull()
            if (user == null) {
                insertSignUser(playerName, playerUUID)
            }
            val res = SignInteracts.selectAll()
                .where { SignInteracts.sign eq signID and (player eq playerUUID) }.singleOrNull()
            if (res != null) {
                val id = res[SignInteracts.id]
                SignInteracts.update({ SignInteracts.id eq id }) {
                    it[this.interactTime] = getTime()
                    it[this.type] = type
                }
            } else {
                SignInteracts.insert {
                    it[this.sign] = signID
                    it[this.player] = playerUUID
                    it[this.interactTime] = getTime()
                    it[this.type] = type
                }
            }
            commit()
        }
    }
}
