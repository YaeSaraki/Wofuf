package dev.saraki.wofuf.modules.players.infra.yawebapi

import dev.saraki.wofuf.modules.players.services.yawebapi.PluginApiClient
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.AdvancementResult
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.PlayerResult
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.PlayerSkinResult
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.SkinUrlResult
import dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc.StatisticResult
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.util.Base64
import java.util.UUID

@Component
class PluginApiClientImpl(
    private val restTemplate: RestTemplate,
    @Value("\${plugin.api.base-url}")
    private val baseUrl: String,
    @Value("\${plugin.api.backup-skin-url-with-extension-png}")
    private val backupSkinUrl: String,
) : PluginApiClient {

    private val log = KotlinLogging.logger {}

    override fun fetchOnlinePlayers(): List<PlayerResult>? = try {
        val url = "$baseUrl/api/v1/players"
        restTemplate.getForObject<Array<PlayerResult>>(url)?.toList()
    } catch (e: Exception) {
        log.error(e) { "获取在线玩家列表失败: ${e.message}" }
        null
    }

    override fun fetchPlayerStatistics(uuid: UUID): StatisticResult? = try {
        val url = "$baseUrl/api/v1/statistics/$uuid"
        restTemplate.getForObject<StatisticResult>(url)
    } catch (e: Exception) {
        log.error(e) { "获取玩家 $uuid 的统计数据失败: ${e.message}" }
        null
    }

    override fun fetchPlayerAdvancements(uuid: UUID): AdvancementResult? = try {
        val url = "$baseUrl/api/v1/advancements/$uuid"
        restTemplate.getForObject<AdvancementResult>(url)
    } catch (e: Exception) {
        log.error(e) { "获取玩家 $uuid 的进度数据失败: ${e.message}" }
        null
    }

    override fun fetchPlayerSkin(uuid: UUID): PlayerSkinResult? = try {
        val skinUrlResult = fetchSkinUrl(uuid) ?: return null
        val (skinBytes, capeBytes) = fetchSkinImages(uuid, skinUrlResult)

        PlayerSkinResult(
            type = skinUrlResult.type,
            skin = encodeToBase64(skinBytes),
            cape = encodeToBase64(capeBytes)
        )
    } catch (e: Exception) {
        log.error(e) { "获取玩家 $uuid 的皮肤数据失败: ${e.message}" }
        null
    }

    private fun fetchSkinUrl(uuid: UUID): SkinUrlResult? = try {
        val url = "$baseUrl/api/v1/skins/$uuid"
        restTemplate.getForObject<SkinUrlResult>(url)
    } catch (e: Exception) {
        log.error(e) { "获取玩家 $uuid 的皮肤URL失败: ${e.message}" }
        null
    }

    private fun fetchSkinImages(uuid: UUID, skinUrlResult: SkinUrlResult): Pair<ByteArray, ByteArray> {
        val skinBytes = fetchImage(skinUrlResult.skin, "皮肤")
            ?: fetchBackupImage("皮肤")
            ?: ByteArray(0)

        val capeBytes = if (skinUrlResult.cape.isNotBlank()) {
            fetchImage(skinUrlResult.cape, "披风")
                ?: fetchBackupImage("披风")
                ?: ByteArray(0)
        } else {
            ByteArray(0)
        }

        return Pair(skinBytes, capeBytes)
    }

    private fun fetchImage(url: String, imageType: String): ByteArray? = try {
        restTemplate.getForObject<ByteArray>(url)
    } catch (e: Exception) {
        log.warn(e) { "获取玩家 $imageType 图片失败: $url - ${e.message}" }
        null
    }

    private fun fetchBackupImage(imageType: String): ByteArray? = try {
        log.info { "尝试使用备用URL获取 $imageType 图片: $backupSkinUrl" }
        restTemplate.getForObject<ByteArray>(backupSkinUrl)
    } catch (e: Exception) {
        log.warn(e) { "获取备用 $imageType 图片失败: ${e.message}" }
        null
    }

    private fun encodeToBase64(bytes: ByteArray): String =
        Base64.getEncoder().encodeToString(bytes)
}