package dev.saraki.wofuf.modules.forum.useCases.images.uploadImage

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * 图片上传错误
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
class UploadImageErrors {

    /** 文件为空 */
    class FileEmptyError : Result.Failure<UploadImageDto.Response>(
        exception = UseCaseError(
            code = "FILE_EMPTY",
            message = "上传文件不能为空"
        )
    )

    /** 文件过大 */
    class FileTooLargeError : Result.Failure<UploadImageDto.Response>(
        exception = UseCaseError(
            code = "FILE_TOO_LARGE",
            message = "文件大小不能超过10MB"
        )
    )

    /** 不支持的文件类型 */
    class UnsupportedFileTypeError(val contentType: String?) : Result.Failure<UploadImageDto.Response>(
        exception = UseCaseError(
            code = "UNSUPPORTED_FILE_TYPE",
            message = "不支持的文件类型: $contentType，仅支持 JPEG, PNG, GIF, WebP"
        )
    )

    /** 上传失败 */
    class UploadFailedError(val reason: String? = null) : Result.Failure<UploadImageDto.Response>(
        exception = UseCaseError(
            code = "UPLOAD_FAILED",
            message = "图片上传失败: ${reason ?: "未知错误"}"
        )
    )
}
