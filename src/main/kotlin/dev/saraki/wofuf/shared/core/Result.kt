package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 21:28
 *   @description: 通用结果封装类（成功/失败模式）
 */

sealed class Result<out T> {
    open class Success<out T>(val value: T) : Result<T>()
    open class Failure<out T>(val exception: AppError) : Result<T>()

    // 状态判断属性
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    // ========== 基础取值方法 ==========
    /** 成功返回值，失败返回null */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    /** 成功返回null，失败返回异常 */
    fun exceptionOrNull(): AppError? = when (this) {
        is Success -> null
        is Failure -> exception
    }

    /** 模拟!!断言：失败返回异常，成功抛NPE */
    fun exceptionOrThrow(): AppError = exceptionOrNull()
        ?: throw AppError("Result is Success, no exception exists")

    /** 成功返回值，失败抛异常 */
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw exception
    }

    /** 失败时返回默认值 */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = getOrNull() ?: defaultValue

    /** 失败时通过函数生成默认值 */
    fun getOrElse(defaultValue: (AppError) -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> defaultValue(exception)
    }

    // ========== 映射/转换方法（贴近原生Result） ==========
    /** 映射成功值 */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> Failure(exception)
    }

    /** 映射失败异常 */
    fun <R> mapFailure(transform: (AppError) -> AppError): Result<R> = when (this) {
        is Success -> Success(value as R) // 成功时类型透传
        is Failure -> Failure(transform(exception))
    }

    /** 折叠：同时处理成功/失败 */
    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (AppError) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(exception)
    }

    // ========== 伴生对象（创建/合并方法） ==========
    companion object {
        /** 创建成功结果 */
        fun <T> success(value: T) = Success(value)

        /** 创建失败结果（带异常） */
        fun <T> failure(exception: AppError) = Failure<T>(exception)

        /** 快速创建失败结果（带消息） */
        fun <T> failure(message: String): Result<T> = failure(AppError(message))

        /** 快速创建失败结果（带消息和代码） */
        fun <T> failure(message: String, code: String): Result<T> = failure(AppError(message, code))




        /**
         * 合并多个Result（通用版）
         * 全部成功返回值列表，失败返回第一个异常
         */
        fun combine(vararg results: Result<*>): Result<List<Any?>> {
            results.forEach { result ->
                if (result is Failure<*>) {
                    return failure<List<Any?>>(result.exception)
                }
            }
            val successValues = results.map { (it as Success<*>).value }
            return success(successValues)
        }

        /** 重载：List入参合并 */
        fun combine(results: List<Result<*>>): Result<List<Any?>> = combine(*results.toTypedArray())

        // ========== 强类型合并（解决Any?类型不安全问题） ==========
        /** 合并2个Result，返回Pair类型 */
        fun <A, B> combine2(
            r1: Result<A>,
            r2: Result<B>
        ): Result<Pair<A, B>> = when {
            r1 is Failure -> failure(r1.exception)
            r2 is Failure -> failure(r2.exception)
            else -> success(Pair(r1.getOrThrow(), r2.getOrThrow()))
        }

        /** 合并3个Result，返回Triple类型（适配表单场景：email+username+password） */
        fun <A, B, C> combine3(
            r1: Result<A>,
            r2: Result<B>,
            r3: Result<C>
        ): Result<Triple<A, B, C>> = when {
            r1 is Failure -> failure(r1.exception)
            r2 is Failure -> failure(r2.exception)
            r3 is Failure -> failure(r3.exception)
            else -> success(Triple(r1.getOrThrow(), r2.getOrThrow(), r3.getOrThrow()))
        }
    }
}

// ========== Result 扩展函数：简化链式调用 ==========
/** 执行副作用：成功时执行 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(value)
    return this
}

/** 执行副作用：失败时执行 */
inline fun <T> Result<T>.onFailure(action: (AppError) -> Unit): Result<T> {
    if (this is Result.Failure) action(exception)
    return this
}

// ========== Result <-> Either 互转扩展（核心新增） ==========
/** Result 转 Either：Failure→Left(exception)，Success→Right(value) */
fun <T> Result<T>.toEither(): Either<AppError, T> = when (this) {
    is Result.Success -> Either.right(value)
    is Result.Failure -> Either.left(exception)
}

/** Either 转 Result：Left→Failure(Left值转为AppError)，Right→Success(value) */
fun <L, A> Either<L, A>.toResult(): Result<A> = when (this) {
    is Either.Left -> {
        val exception = if (value is AppError) value else AppError(value.toString())
        Result.failure(exception)
    }
    is Either.Right -> Result.success(value)
}

/** 简化：直接创建 Left=AppError 的 Either（对齐 Result 失败语义） */
fun <A> Either.Companion.left(exception: AppError): Either<AppError, A> = Either.left(exception)

/** 简化：直接创建 Right 的 Either（对齐 Result 成功语义） */
fun <A> Either.Companion.right(value: A): Either<AppError, A> = Either.right(value)