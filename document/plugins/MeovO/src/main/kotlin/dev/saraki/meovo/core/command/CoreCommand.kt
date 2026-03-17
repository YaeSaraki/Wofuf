package dev.saraki.meovo.core.command

import dev.saraki.meovo.modules.yawebapi.utils.HashVerifyUtil
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.warning
import taboolib.expansion.createHelper


/**
 * Core 核心命令：插件全局管理入口
 * 支持重载配置、查看状态、控制模块等核心操作
 */
@CommandHeader(
    name = "meovo",
    aliases = ["meo"],
    description = "Meoveo 管理命令",
)
object CoreCommand {
    /**
     * 主命令：显示帮助信息
     */
    @CommandBody
    val main = mainCommand {
            createHelper()
    }

    @CommandBody
    val code = subCommand {
        execute<Player> { sender, context, argument ->
            try {
                val code = HashVerifyUtil.generateCode((sender.uniqueId.toString() + sender.lastPlayed.toString()))
                val message =
                    "您的验证码为：$code, 验证码在进入游戏时更新，验证时请不要退出游戏，验证后请重新进入游戏, 防止账号被他人盗用。"
                sender.sendMessage(message)
            } catch (ex: Exception) {
                warning(ex.localizedMessage)
            }
        }
    }
}