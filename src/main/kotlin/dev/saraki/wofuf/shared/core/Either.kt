package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 20:59
 *   @description: Either 左/右值封装（Left=失败，Right=成功）
 */
sealed class Either<out L, out A> {
    data class Left<out L, out A>(val value: L) : Either<L, A>()
    data class Right<out L, out A>(val value: A) : Either<L, A>()

    // ========== 核心工具方法（补充） ==========
    /** 折叠：同时处理 Left/Right */
    fun <R> fold(onLeft: (L) -> R, onRight: (A) -> R): R = when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

    /** 映射 Right 值 */
    fun <B> map(f: (A) -> B): Either<L, B> = when (this) {
        is Left -> Left(value)
        is Right -> Right(f(value))
    }

    /** 映射 Left 值 */
    fun <B> mapLeft(f: (L) -> B): Either<B, A> = when (this) {
        is Left -> Left(f(value))
        is Right -> Right(value)
    }

    // ========== 伴生对象（工厂函数 + 合并方法） ==========
    companion object {
        /** 创建 Left 实例 */
        fun <L, A> left(value: L): Either<L, A> = Left(value)

        /** 创建 Right 实例 */
        fun <L, A> right(value: A): Either<L, A> = Right(value)

        /** 合并多个 Either，返回第一个 Left 或所有 Right 值列表 */
        fun <L, A> combine(vararg eithers: Either<L, A>): Either<L, List<A>> {
            eithers.forEach { either ->
                if (either is Left) return Left(either.value)
            }
            val rightValues = eithers.map { (it as Right).value }
            return Right(rightValues)
        }
    }
}

// ========== Either 扩展函数（类型检查 + 安全取值） ==========
/** 判断是否为 Left */
fun <L, A> Either<L, A>.isLeft(): Boolean = this is Either.Left

/** 判断是否为 Right */
fun <L, A> Either<L, A>.isRight(): Boolean = this is Either.Right

/** 获取 Left 值（非 Left 则返回 null） */
fun <L, A> Either<L, A>.getLeftOrNull(): L? = when (this) {
    is Either.Left -> this.value
    is Either.Right -> null
}

/** 获取 Right 值（非 Right 则返回 null） */
fun <L, A> Either<L, A>.getRightOrNull(): A? = when (this) {
    is Either.Left -> null
    is Either.Right -> this.value
}

// ========== Either 副作用扩展 ==========
/** Left 时执行副作用 */
inline fun <L, A> Either<L, A>.onLeft(action: (L) -> Unit): Either<L, A> {
    if (this is Either.Left) action(value)
    return this
}

/** Right 时执行副作用 */
inline fun <L, A> Either<L, A>.onRight(action: (A) -> Unit): Either<L, A> {
    if (this is Either.Right) action(value)
    return this
}