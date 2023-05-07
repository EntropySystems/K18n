package com.github.devlaq.loader

import java.io.InputStream
import java.util.Properties

object K18nPropertiesLoader: K18nLoader {

    override fun load(stream: InputStream): Map<String, String> {
        val properties = Properties()

        properties.load(stream)

        return properties.toMap().mapKeys { it.key.toString() }.mapValues { it.value.toString() }
    }

}
