package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject
import dev.saraki.wofuf.shared.utils.TextUtil

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 22:49
 *   @description:
 */

data class PostSlugProps(
    val value: String
)

private data class SlugConfig(
    val replacement: String = "-",
    val symbols: Boolean = false,
    val lower: Boolean = true
)

class PostSlug private constructor(
    props: PostSlugProps
) : ValueObject<PostSlugProps>(props) {
    val value: String
        get() = props.value

    companion object {
        private val slugConfig = SlugConfig()

        fun create(postTitle: PostTitle): Result<PostSlug> {
            val guardResult = Guard.againstNullOrUndefined(postTitle.value, "PostSlug")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            val randomNumeric = TextUtil.createRandomNumericString(7)
            val titleSlug = postTitle.value.toSlug(slugConfig)
            val resultSlug = "$titleSlug-$randomNumeric"
            return Result.success(PostSlug(PostSlugProps(resultSlug)))
        }

        fun createFromExisting(slugName: String): Result<PostSlug> {
            val guardResult = Guard.againstNullOrUndefined(slugName, "PostSlug")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(PostSlug(PostSlugProps(slugName)))
        }

        private fun String.toSlug(config: SlugConfig): String {
            var processed = this
            // 1. 移除/替换特殊字符
            if (!config.symbols) {
                processed = processed.replace(Regex("[^a-zA-Z0-9\\s]"), "")
            }
            // 2. 替换非单词字符/下划线为空格
            processed = processed.replace(Regex("[\\W_]+"), " ")
            // 3. 空格替换为指定字符
            processed = processed.replace(Regex("\\s+"), config.replacement)
            // 4. 转小写
            if (config.lower) {
                processed = processed.lowercase()
            }
            // 5. 去除首尾的替换字符
            return processed.trim(config.replacement[0])
        }
    }


}
