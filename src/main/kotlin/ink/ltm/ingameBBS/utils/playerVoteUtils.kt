package ink.ltm.ingameBBS.utils

/*
fun playerVoteAction(
    sender: CommandSender,
    uid: String,
    like: Boolean,
    message: String,
    messageReceive: String,
    sound: Sound
): Boolean {
    var notify = false
    var cancel = false
    var factor = 1
    val res = when (modifySignLikes(uid, like, sender.name)) {
        LikesResult.NO_SUCH_DATA -> {
            sender.sendMessage("No more information.")
            false
        }

        LikesResult.CANCEL_SAME -> {
            sender.sendMessage(messageConverter(IngameBBS.Companion.VoteInfo.cancel))
            cancel = true
            true
        }

        LikesResult.CANCEL_DIFFERENT -> {
            sender.sendMessage(messageConverter(IngameBBS.Companion.VoteInfo.change))
            notify = true
            true
        }

        LikesResult.SUCCESS -> {
            sender.sendMessage(messageConverter(message))
            notify = true
            true
        }
    }
    if (res) {
        sender.playSound(sound)
        val name = lookupSignInfo(uid)!!.creatorName
        val player = Bukkit.getPlayer(name)
        if (player?.isOnline == true && notify) {
            player.sendMessage(messageConverter(messageReceive.replace("%player", sender.name)))
            player.playSound(sound)
        } else {
            if (cancel) {
                factor = -1
            }
            IngameBBS.offlineMap[name] ?: IngameBBS.offlineMap.put(name, 0)
            IngameBBS.offlineMap[name] = IngameBBS.offlineMap[name]!! + factor * (if (like) 1 else -1)
        }
    }
    return res
}*/