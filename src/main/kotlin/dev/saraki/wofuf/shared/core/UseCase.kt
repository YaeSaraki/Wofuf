package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:20
 *   @description:
 */
abstract class UseCase<in Request, out Response> {
    abstract fun execute(request: Request): Response
}