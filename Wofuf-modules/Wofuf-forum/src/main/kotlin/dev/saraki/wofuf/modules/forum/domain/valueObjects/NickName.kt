package dev.saraki.wofuf.modules.forum.domain.valueObjects

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/10 11:18
 *   @description:
 */
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

data class NickNameProps(val value: String)

class NickName private constructor(props: NickNameProps) : ValueObject<NickNameProps>(props) {
    val value: String get() = props.value

    companion object {
        fun create(nickName: String): Result<NickName> {
            val trimmedNickName = nickName.trim()

            val validation = Guard.combine(
                listOf(
                    Guard.againstNullOrUndefined(trimmedNickName, "nickName"),
                    Guard.againstAtLeast(3, trimmedNickName),
                    Guard.againstAtMost(50, trimmedNickName)
                )
            )

            if (validation.isFailure) {
                return Result.failure(validation.exceptionOrThrow())
            }

            // 检查用户名是否只包含允许的字符
            if (!trimmedNickName.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
                return Result.failure(validation.exceptionOrThrow())
            }

            return Result.success(NickName(NickNameProps(trimmedNickName)))
        }

        val UNKNOWN: NickName
            get() = NickName(NickNameProps("UNKNOWN"))
    }
}