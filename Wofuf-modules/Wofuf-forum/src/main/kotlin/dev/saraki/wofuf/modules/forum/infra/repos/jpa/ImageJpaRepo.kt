package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.ImageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ImageJpaRepo : JpaRepository<ImageEntity, String> {

    fun findByMd5(md5: String): ImageEntity?

    fun existsByMd5(md5: String): Boolean

    fun deleteByMd5(md5: String)

    @Query("SELECT i FROM ImageEntity i WHERE (:folder IS NULL OR i.folder = :folder) AND (:uploaderId IS NULL OR i.uploaderId = :uploaderId) ORDER BY i.uploadedAt DESC")
    fun findImages(@Param("folder") folder: String?, @Param("uploaderId") uploaderId: String?, pageable: Pageable): Page<ImageEntity>

    @Query("SELECT COUNT(i) FROM ImageEntity i WHERE (:folder IS NULL OR i.folder = :folder) AND (:uploaderId IS NULL OR i.uploaderId = :uploaderId)")
    fun countByFolder(@Param("folder") folder: String?, @Param("uploaderId") uploaderId: String?): Long
}
