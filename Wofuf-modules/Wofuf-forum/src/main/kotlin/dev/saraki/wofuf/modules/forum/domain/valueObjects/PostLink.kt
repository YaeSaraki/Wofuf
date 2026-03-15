package dev.saraki.wofuf.modules.forum.domain.valueObjects

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 16:04
 *   @description:
 */
data class PostLinkProps(
    val value: String
)

class PostLink private constructor(
    props: PostLinkProps
) : ValueObject<PostLinkProps>(props) {
    val value: String
        get() = props.value

    companion object {
        fun create(props: PostLinkProps): Result<PostLink> {
            val guardResult = Guard.againstNullOrUndefined(props.value, "PostLink")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(PostLink(props))
        }
    }
}