package ink.ltm.ingameBBS.data

object Config {
    object ItemInfo {
        var name = "声光电炫彩酷告示牌"
        var lore = "可以进行互动的神秘牌子"
    }

    object SignInfo {
        var icon = "*"
        var waiting = "blahblah"
        var message = """
        信息： <remark><newline>
        详情：由玩家 <creator> 创建于 <date><newline>
        累计：获赞 <like-count> ，获踩 <dislike-count><newline>
        互动选项： <like-button> <dislike-button>
        """
        var likeButton = "点赞"
        var dislikeButton = "点踩"
        var ownerMessage = "编辑选项：<edit-button>"
        var editButton = "编辑备注"
    }

    object VoteMessage {
        var like = "点赞!"
        var dislike = "点踩!"
        var getLike = "收到 <player> 点赞"
        var getDislike = "收到 <player> 点踩"
        var offlineMessage = "你收到了 <count> 个离线点赞"
        var likeSound = "entity.experience_orb.pickup"
        var dislikeSound = "entity.player.attack.sweep"
    }

    object InteractMessage {
        var created = "创建可互动告示牌成功"
        var removed = "成功移除告示牌"
        var updatedRemark = "成功更新备注"
        var length = 64
        var exit = "exit"
        var inputPrompt = "请输入备注信息，输入 <exit> 退出"
    }

    object ErrorMessage {
        var notOwner = "你不是这个告示牌的创建者"
        var notExist = "该告示牌不存在"
        var tooLong = "备注信息过长，最多 <length> 个字符"
        var notPlayer = "你必须是一名玩家"
    }
}