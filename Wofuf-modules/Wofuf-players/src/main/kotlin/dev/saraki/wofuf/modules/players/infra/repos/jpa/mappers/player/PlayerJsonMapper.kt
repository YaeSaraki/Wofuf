package dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.player

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerAdvancementProps
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerStatisticProps

object PlayerJsonMapper {
    private val gson: Gson = GsonBuilder()
        .excludeFieldsWithModifiers(java.lang.reflect.Modifier.PRIVATE, java.lang.reflect.Modifier.FINAL)
        .disableHtmlEscaping()
        .serializeNulls()
        .create()


    fun statisticsToJson(stats: Map<String, PlayerStatisticProps>): String {
        val jsonObject = JsonObject()

        stats.forEach { (key, props) ->
            val propsObject = JsonObject().apply {
                addProperty("key", props.key)
                addProperty("category", props.category)
                addProperty("value", props.value)
            }
            jsonObject.add(key, propsObject)
        }

        return gson.toJson(jsonObject)
    }

    fun statisticsFromJson(json: String?): Map<String, PlayerStatisticProps> =
        json?.let {
            try {
                val jsonElement = JsonParser.parseString(it)
                if (!jsonElement.isJsonObject) {
                    return emptyMap()
                }

                val jsonObject = jsonElement.asJsonObject
                val result = mutableMapOf<String, PlayerStatisticProps>()

                jsonObject.entrySet().forEach { entry ->
                    val key = entry.key
                    val propsObject = entry.value.asJsonObject

                    val props = PlayerStatisticProps(
                        key = propsObject.get("key")?.asString ?: key,
                        category = propsObject.get("category")?.asString ?: "",
                        value = propsObject.get("value")?.asLong ?: 0L
                    )
                    result[key] = props
                }

                result
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        } ?: emptyMap()

    fun advancementsToJson(advs: Map<String, PlayerAdvancementProps>): String {
        val jsonObject = JsonObject()

        advs.forEach { (key, props) ->
            val propsObject = JsonObject().apply {
                addProperty("key", props.key)
                addProperty("done", props.done)

                val completedArray = JsonArray()
                props.completed.forEach { completedArray.add(it) }
                add("completed", completedArray)

                val remainingArray = JsonArray()
                props.remaining.forEach { remainingArray.add(it) }
                add("remaining", remainingArray)
            }
            jsonObject.add(key, propsObject)
        }

        return gson.toJson(jsonObject)
    }

    fun advancementsFromJson(json: String?): Map<String, PlayerAdvancementProps> =
        json?.let {
            try {
                val jsonElement = JsonParser.parseString(it)
                if (!jsonElement.isJsonObject) {
                    return emptyMap()
                }

                val jsonObject = jsonElement.asJsonObject
                val result = mutableMapOf<String, PlayerAdvancementProps>()

                jsonObject.entrySet().forEach { entry ->
                    val key = entry.key
                    val propsObject = entry.value.asJsonObject

                    val completed = mutableListOf<String>()
                    propsObject.get("completed")?.asJsonArray?.forEach {
                        completed.add(it.asString)
                    }

                    val remaining = mutableListOf<String>()
                    propsObject.get("remaining")?.asJsonArray?.forEach {
                        remaining.add(it.asString)
                    }

                    val props = PlayerAdvancementProps(
                        key = propsObject.get("key")?.asString ?: key,
                        done = propsObject.get("done")?.asBoolean ?: false,
                        completed = completed,
                        remaining = remaining
                    )
                    result[key] = props
                }

                result
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        } ?: emptyMap()
}