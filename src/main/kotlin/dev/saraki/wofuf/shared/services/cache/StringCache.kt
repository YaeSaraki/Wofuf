package dev.saraki.wofuf.shared.services.cache

import java.time.Duration

interface StringCache {
    fun get(key: String): String?
    fun put(key: String, value: String, ttl: Duration)
    fun delete(key: String)
}
