package dev.saraki.wofuf.modules.players.domain

import dev.saraki.wofuf.modules.players.domain.valueObjects.ServerTps
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Server status aggregate root representing Minecraft server status
 */
data class ServerStatusProps(
    val onlinePlayers: Int,
    val maxPlayers: Int,
    val tps: ServerTps,
    val heartbeatStatus: Boolean,
    val updateTime: Long
)

class ServerStatus private constructor(
    props: ServerStatusProps,
    id: UniqueEntityId?
) : AggregateRoot<ServerStatusProps>(props, id) {

    val onlinePlayers: Int get() = props.onlinePlayers
    val maxPlayers: Int get() = props.maxPlayers
    val tps: ServerTps get() = props.tps
    val heartbeatStatus: Boolean get() = props.heartbeatStatus
    val updateTime: Long get() = props.updateTime

    companion object {
        fun create(
            onlinePlayers: Int,
            maxPlayers: Int,
            tps: Double,
            heartbeatStatus: Boolean,
            updateTime: Long
        ): Result<ServerStatus> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(onlinePlayers, "Online players cannot be null"),
                    Guard.GuardArgument(maxPlayers, "Max players cannot be null")
                )
            )
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            val tpsVO = ServerTps.create(tps).getOrThrow()

            return Result.success(
                ServerStatus(
                    ServerStatusProps(
                        onlinePlayers = onlinePlayers,
                        maxPlayers = maxPlayers,
                        tps = tpsVO,
                        heartbeatStatus = heartbeatStatus,
                        updateTime = updateTime
                    ),
                    UniqueEntityId()
                )
            )
        }
    }
}
