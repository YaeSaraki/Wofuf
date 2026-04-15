package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.Image
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.ImageJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.ImageEntityMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ImageRepoImpl(
    private val imageJpaRepo: ImageJpaRepo
) : ImageRepo {

    override fun findById(imageId: String): Image? {
        return imageJpaRepo.findById(imageId).orElse(null)?.let(ImageEntityMapper::toDomain)
    }

    override fun findByMd5(md5: String): Image? {
        return imageJpaRepo.findByMd5(md5)?.let(ImageEntityMapper::toDomain)
    }

    override fun findImages(page: Int, size: Int, folder: String?): List<Image> {
        return imageJpaRepo.findImages(folder, PageRequest.of(page, size))
            .content
            .map(ImageEntityMapper::toDomain)
    }

    override fun countImages(folder: String?): Long {
        return imageJpaRepo.countByFolder(folder)
    }

    override fun save(image: Image): Image {
        val entity = ImageEntityMapper.toEntity(image)
        return ImageEntityMapper.toDomain(imageJpaRepo.save(entity))
    }

    @Transactional
    override fun delete(md5: String) {
        imageJpaRepo.deleteByMd5(md5)
    }

    override fun exists(md5: String): Boolean {
        return imageJpaRepo.existsByMd5(md5)
    }
}
