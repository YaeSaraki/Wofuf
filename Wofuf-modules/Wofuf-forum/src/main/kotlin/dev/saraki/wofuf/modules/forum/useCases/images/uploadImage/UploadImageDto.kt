package dev.saraki.wofuf.modules.forum.useCases.images.uploadImage

/**
 * 图片上传DTO
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
class UploadImageDto {
    /**
     * 上传请求
     * 注意: 文件通过MultipartFile传递，不在此DTO中
     */
    data class Request(
        val folder: String = "posts"  // 存储文件夹，默认为posts
    )

    /**
     * 上传响应
     */
    data class Response(
        val url: String,           // 图片访问URL
        val markdown: String,      // Markdown格式的图片引用
        val md5: String,           // 文件MD5哈希值
        val isDuplicate: Boolean,  // 是否为重复图片
        val success: Boolean = true
    )
}
