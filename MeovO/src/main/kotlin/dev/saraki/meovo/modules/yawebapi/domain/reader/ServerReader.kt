package dev.saraki.meovo.modules.yawebapi.domain.reader

import dev.saraki.meovo.modules.yawebapi.domain.ServerStatusResult

interface ServerReader {
    fun readStatus(): ServerStatusResult
}