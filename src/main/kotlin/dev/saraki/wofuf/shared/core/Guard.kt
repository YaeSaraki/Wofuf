package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:42
 *   @description:
 */

object Guard {
    data class GuardArgument(val argument: Any?, val argumentName: String)

    fun combine(guardResults: List<Result<String>>): Result<String> {
        for (result in guardResults) {
            if (result.isFailure) return result
        }
        return Result.success("")
    }

    fun greaterThan(minValue: Int, actualValue: Int): Result<String> {
        return if (actualValue > minValue) {
            Result.success("")
        } else {
            Result.failure(AppError("$actualValue > $minValue", code = "VALUE_TOO_SMALL"))
        }
    }

    fun againstAtLeast(numChars: Int, text: String): Result<String> {
        return if (text.length >= numChars) {
            Result.success(text)
        } else {
            Result.failure(AppError("Text is not at least $numChars chars.", code = "TEXT_TOO_SHORT"))
        }
    }

    fun againstAtMost(numChars: Int, text: String): Result<String> {
        return if (text.length <= numChars) {
            Result.success(text)
        } else {
            Result.failure(AppError("Text is greater than $numChars chars.", code = "TEXT_TOO_LONG"))
        }
    }

    fun againstNullOrUndefined(argument: Any?, argumentName: String): Result<String> {
        return if (argument != null) {
            Result.success("")
        } else {
            Result.failure(AppError("$argumentName is null or undefined", code = "ARGUMENT_IS_NULL"))
        }
    }

    fun againstNullOrUndefinedBulk(args: List<GuardArgument>?): Result<String> {
        if (args == null) return Result.failure(AppError("$args is null or undefined", code = "ARGUMENT_IS_NULL"))
        for (arg in args) {
            val result = againstNullOrUndefined(arg.argument, arg.argumentName)
            if (result.isFailure) return result
        }
        return Result.success("")
    }

    fun isOneOf(value: Any, validValues: List<Any>, argumentName: String): Result<String> {
        val isValid = validValues.contains(value)
        return if (isValid) {
            Result.success("")
        } else {
            Result.failure(AppError("$argumentName isn't oneOf the correct types in ${validValues}. Got \"$value\".", code = "INVALID_ARGUMENT_VALUE"))
        }
    }

    fun inRange(num: Int, min: Int, max: Int, argumentName: String): Result<String> {
        val isInRange = num >= min && num <= max
        return if (isInRange) {
            Result.success("")
        } else {
            Result.failure(AppError("$argumentName is not within range $min to $max.", code = "VALUE_OUT_OF_RANGE"))
        }
    }

    fun allInRange(numbers: List<Int>, min: Int, max: Int, argumentName: String): Result<String> {
        for (num in numbers) {
            val numIsInRangeResult = inRange(num, min, max, argumentName)
            if (numIsInRangeResult.isFailure) return numIsInRangeResult
        }
        return Result.success("")
    }
}