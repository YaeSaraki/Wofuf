package dev.saraki.wofuf.modules.players.domain.valueObjects

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Value object representing server TPS (Ticks Per Second)
 */
data class ServerTpsProps(val value: Double)

class ServerTps private constructor(props: ServerTpsProps) : ValueObject<ServerTpsProps>(props) {
    val stringValue: String get() = String.format("%.2f", props.value)
    val doubleValue: Double get() = props.value

    companion object {
        fun create(value: Double): Result<ServerTps> {
            if (value < 0.0) {
                return Result.failure("TPS cannot be negative")
            }
            return Result.success(ServerTps(ServerTpsProps(value)))
        }
    }
}
