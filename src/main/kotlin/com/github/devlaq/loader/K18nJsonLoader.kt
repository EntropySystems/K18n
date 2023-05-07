package com.github.devlaq.loader

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream

object K18nJsonLoader: K18nLoader {

    override fun load(stream: InputStream): Map<String, String> {
        val content = stream.reader().readText()

        val jsonElement = Json.parseToJsonElement(content)

        return jsonElement.jsonObject.toMap().mapValues { it.value.jsonPrimitive.content }
    }

}
