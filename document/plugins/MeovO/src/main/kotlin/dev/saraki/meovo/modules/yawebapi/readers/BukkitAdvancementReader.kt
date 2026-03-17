package dev.saraki.meovo.modules.yawebapi.readers

import dev.saraki.meovo.modules.yawebapi.domain.AdvancementItem
import dev.saraki.meovo.modules.yawebapi.domain.AdvancementQuery
import dev.saraki.meovo.modules.yawebapi.domain.AdvancementResult
import dev.saraki.meovo.modules.yawebapi.domain.reader.AdvancementReader
import org.bukkit.Bukkit
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:43
 *   @description:
 */

class BukkitAdvancementReader : AdvancementReader {

    override fun read(
        playerId: UUID,
        query: AdvancementQuery
    ): AdvancementResult? {

        val player = Bukkit.getPlayer(playerId) ?: return null

        val advancements = Bukkit.advancementIterator().asSequence()
            .map { adv ->
                val progress = player.getAdvancementProgress(adv)
                AdvancementItem(
                    key = adv.key.key,
                    done = progress.isDone,
                    completed = progress.awardedCriteria.toList(),
                    remaining = progress.remainingCriteria.toList()
                )
            }
            .filter { query.includeDone || !it.done }
            .toList()

        return AdvancementResult(
            uuid = player.uniqueId.toString(),
            name = player.name,
            advancements = advancements
        )
    }

    override fun readAll(query: AdvancementQuery): List<AdvancementResult> {
        return Bukkit.getOnlinePlayers().mapNotNull { player ->
            read(player.uniqueId, query)
        }
    }
}