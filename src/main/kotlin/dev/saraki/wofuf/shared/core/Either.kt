package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 20:59
 *   @description:
 */
sealed class Either<out L, out A> {
    data class Left<out L, out A>(val value: L) : Either<L, A>()
    data class Right<out L, out A>(val value: A) : Either<L, A>()

    // 扩展函数用于类型检查
    fun <L, A> Either<L, A>.isLeft(): Boolean = this is Either.Left
    fun <L, A> Either<L, A>.isRight(): Boolean = this is Either.Right

    // 工厂函数
    fun <L, A> left(value: L): Either<L, A> = Either.Left(value)
    fun <L, A> right(value: A): Either<L, A> = Either.Right(value)

    // 扩展函数用于安全获取值
    fun <L, A> Either<L, A>.getLeftOrNull(): L? = when (this) {
        is Either.Left -> this.value
        is Either.Right -> null
    }

    fun <L, A> Either<L, A>.getRightOrNull(): A? = when (this) {
        is Either.Left -> null
        is Either.Right -> this.value
    }
}
