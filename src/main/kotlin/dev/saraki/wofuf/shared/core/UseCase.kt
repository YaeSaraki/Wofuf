package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:20
 *   @description:
 */
/**
 * Base UseCase. Implement execute to run use case logic and return Result.
 * Keep non-suspending for compatibility; convert to 'suspend' if you use coroutines across the app.
 */
interface UseCase<Request, Response> {
    fun execute(request: Request): Result<Response>
}