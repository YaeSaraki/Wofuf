package dev.saraki.wofuf.modules.players.useCases.searchPlayers

class SearchPlayersDto {
    data class Request(
        val query: String,
        val limit: Int = 20
    )

    data class Response(
        val players: List<PlayerSummary>
    )

    data class PlayerSummary(
        val id: String,
        val name: String,
        val lastLogin: Long
    )
}
