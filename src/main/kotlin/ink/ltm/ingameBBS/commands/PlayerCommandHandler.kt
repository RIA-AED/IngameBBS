package ink.ltm.ingameBBS.commands

import ink.ltm.ingameBBS.IngameBBS
import ink.ltm.ingameBBS.data.LikesResult
import ink.ltm.ingameBBS.data.SignDataInternal
import ink.ltm.ingameBBS.utils.lookupSignInfo
import ink.ltm.ingameBBS.utils.messageConverter
import ink.ltm.ingameBBS.utils.modifySignLikes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class PlayerInfoCommandHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args.isNullOrEmpty()) {
            return false
        }
        val uid = args.first()
        val data: SignDataInternal? = lookupSignInfo(uid)
        if (data == null) {
            sender.sendMessage("No more information.")
            return false
        }
        val signInfoDeco = IngameBBS.Companion.SignInfo.signInfoDeco
        val signContent = IngameBBS.Companion.SignInfo.signContent.replace("%content", data.signContent)
        val signDetail = IngameBBS.Companion.SignInfo.signDetail.replace("%player", data.creatorName)
            .replace("%date", data.createTime.toString())
        val signLikes = IngameBBS.Companion.SignInfo.signLikes.replace("%like", data.likeCount.toString())
            .replace("%dislike", data.dislikeCount.toString())
        val signInteract = IngameBBS.Companion.SignInfo.signInteract
        val signLike = IngameBBS.Companion.SignInfo.signLike
        val signDislike = IngameBBS.Companion.SignInfo.signDislike
        val component = Component.empty()
            .append(messageConverter(signInfoDeco))
            .append(Component.newline())
            .append(messageConverter(signContent))
            .append(Component.newline())
            .append(messageConverter(signDetail))
            .append(Component.newline())
            .append(messageConverter(signLikes))
            .append(Component.newline())
            .append(messageConverter(signInteract))
            .append(Component.newline())
            .append(
                messageConverter(signLike).clickEvent(ClickEvent.runCommand("/igblike $uid"))
            )
            .append(
                messageConverter(signDislike).clickEvent(ClickEvent.runCommand("/igbdislike $uid"))
            )
        sender.sendMessage(component)
        return true
    }
}

class PlayerLikeCommandHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args != null) {
            val uid = args.first()
            return when (modifySignLikes(uid, true, sender.name)) {
                LikesResult.NO_SUCH_DATA -> {
                    sender.sendMessage("No more information.")
                    false
                }

                LikesResult.CANCEL_SAME -> {
                    sender.sendMessage(messageConverter(IngameBBS.Companion.SignVoteInfo.voteCancel))
                    true
                }

                LikesResult.CANCEL_DIFFERENT -> {
                    sender.sendMessage(messageConverter(IngameBBS.Companion.SignVoteInfo.voteChange))
                    true
                }

                LikesResult.SUCCESS -> {
                    sender.sendMessage(messageConverter(IngameBBS.Companion.SignVoteInfo.voteLike))
                    true
                }
            }
        }
        return false
    }
}

class PlayerDislikeCommandHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args != null) {
            val uid = args.first()
            return when (modifySignLikes(uid, false, sender.name)) {
                LikesResult.NO_SUCH_DATA -> {
                    sender.sendMessage("No more information.")
                    false
                }

                LikesResult.CANCEL_SAME -> {
                    sender.sendMessage(messageConverter(IngameBBS.Companion.SignVoteInfo.voteCancel))
                    true
                }

                LikesResult.CANCEL_DIFFERENT -> {
                    sender.sendMessage(messageConverter(IngameBBS.Companion.SignVoteInfo.voteChange))
                    true
                }

                LikesResult.SUCCESS -> {
                    sender.sendMessage(messageConverter(IngameBBS.Companion.SignVoteInfo.voteDislike))
                    true
                }
            }
        }
        return false
    }
}

class PlayerAddContentCommandHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        TODO("Not yet implemented")
    }
}