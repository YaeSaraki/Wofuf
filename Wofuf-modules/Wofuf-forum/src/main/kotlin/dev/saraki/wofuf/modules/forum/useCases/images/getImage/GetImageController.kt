package dev.saraki.wofuf.modules.forum.useCases.images.getImage

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.storage.ImageStorageService
import dev.saraki.wofuf.modules.forum.infra.storage.ImageUploadException
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Images.ROOT)
class GetImageController(
    private val imageRepo: ImageRepo,
    private val imageStorageService: ImageStorageService
) : BaseController() {

    @GetMapping("/{md5}")
    fun getImage(@PathVariable md5: String): org.springframework.http.ResponseEntity<ByteArray> {
        val image = imageRepo.findByMd5(md5)
            ?: throw ImageUploadException("Image not found: $md5")

        if (image.objectName.isBlank()) {
            throw ImageUploadException("Image objectName is empty")
        }

        val imageBytes = imageStorageService.getImageBytes(image.objectName)

        val contentType = MediaType.parseMediaType(image.contentType)

        return org.springframework.http.ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, image.contentType)
            .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
            .header(HttpHeaders.CONTENT_LENGTH, imageBytes.size.toString())
            .body(imageBytes)
    }
}
