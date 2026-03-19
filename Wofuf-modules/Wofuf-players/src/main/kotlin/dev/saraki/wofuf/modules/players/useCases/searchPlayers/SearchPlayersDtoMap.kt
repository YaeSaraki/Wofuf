package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.modules.players.domain.Player

abstract class SearchPlayersDtoMap {
    companion object {
        fun from(player: Player): SearchPlayersDto.PlayerSummary =
            SearchPlayersDto.PlayerSummary(
                id = player.playerId.stringValue,
                name = player.playerName.stringValue,
                lastLogin = player.lastLogin
            )
    }
}
