package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.Image

/**
 * 图片仓储接口
 */
interface ImageRepo {
    /**
     * 根据ID查找图片
     */
    fun findById(imageId: String): Image?

    /**
     * 根据MD5查找图片
     */
    fun findByMd5(md5: String): Image?

    /**
     * 分页获取图片列表
     */
    fun findImages(page: Int, size: Int, folder: String?, uploaderId: String?): List<Image>

    /**
     * 统计图片数量
     */
    fun countImages(folder: String?, uploaderId: String?): Long

    /**
     * 保存图片
     */
    fun save(image: Image): Image

    /**
     * 删除图片
     */
    fun delete(md5: String)

    /**
     * 检查图片是否存在
     */
    fun exists(md5: String): Boolean
}
