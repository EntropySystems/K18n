package com.github.devlaq

import com.github.devlaq.loader.K18nLoader
import com.github.devlaq.loader.K18nPropertiesLoader
import com.github.devlaq.type.K18nExtractResult
import com.github.devlaq.type.K18nExtractResultType
import com.sun.org.apache.bcel.internal.generic.LCONST
import java.io.File
import java.io.InputStream
import java.util.Locale

object K18n {

    internal val fallbackLocale = mutableMapOf<String, Locale>()

    private val registry = mutableMapOf<Locale, MutableMap<String, String>>()
    private val externalRegistry = mutableMapOf<Locale, MutableMap<String, String>>()

    init {
        fallbackLocale["global"] = Locale.ENGLISH
    }

    fun loadInternal(namespace: String, clazz: Class<*>, base: String = "/locale-{tag}.properties", loader: K18nLoader = K18nPropertiesLoader, vararg locales: Locale) {
        if(fallbackLocale[namespace] == null) fallbackLocale[namespace] = Locale.ENGLISH

        var useGlobal = false
        if(namespace == "global") useGlobal = true

        locales.forEach {
            val path = base.replace("{tag}", it.toLanguageTag())

            val stream = clazz.getResourceAsStream(path) ?: return@forEach

            var loadedMap = loader.load(stream)
            if(!useGlobal) loadedMap = loadedMap.mapKeys { e -> "$namespace:${e.key}" }

            val targetMap = registry.getOrPut(it, ::mutableMapOf)

            targetMap.putAll(loadedMap)
        }
    }

    inline fun <reified T> loadInternal(namespace: String, base: String = "/locale-{tag}.properties", loader: K18nLoader = K18nPropertiesLoader, vararg locales: Locale) {
        loadInternal(namespace, T::class.java, base, loader, *locales)
    }

    fun loadExternal(namespace: String, directory: File, base: String = "/locale-{tag}.properties", loader: K18nLoader = K18nPropertiesLoader, vararg locales: Locale) {
        if(fallbackLocale[namespace] == null) fallbackLocale[namespace] = Locale.ENGLISH

        var useGlobal = false
        if(namespace == "global") useGlobal = true

        locales.forEach {
            val path = base.replace("{tag}", it.toLanguageTag())
            val file = File(directory, path)

            if(!file.isFile || !file.exists()) return@forEach

            val stream = File(directory, path).inputStream()

            var loadedMap = loader.load(stream)
            if(!useGlobal) loadedMap = loadedMap.mapKeys { e -> "$namespace:${e.key}" }

            val targetMap = externalRegistry.getOrPut(it, ::mutableMapOf)

            targetMap.putAll(loadedMap)
        }
    }

    fun extractInternal(targetDirectory: File, clazz: Class<*>, base: String = "/locale-{tag}.properties", vararg locales: Locale): List<K18nExtractResult> {
        if(!targetDirectory.exists()) {
            if(!targetDirectory.mkdirs()) return emptyList()
        }

        val list = mutableListOf<K18nExtractResult>()

        locales.forEach {
            val path = base.replace("{tag}", it.toLanguageTag())
            val file = File(targetDirectory, path)

            val from = clazz.getResource(path)?.path ?: path
            val to = file.path

            if(file.exists()) {
                list.add(K18nExtractResult(from, to, K18nExtractResultType.TargetExist))
                return@forEach
            }

            val stream = clazz.getResourceAsStream(path)
            if(stream == null) {
                list.add(K18nExtractResult(from, to, K18nExtractResultType.NoSource))
                return@forEach
            }

            file.parentFile.mkdirs()
            file.createNewFile()

            file.writeText(stream.reader().readText())

            list.add(K18nExtractResult(from, to, K18nExtractResultType.Success))
        }

        return list.toList()
    }

    inline fun <reified T> extractInternal(targetDirectory: File, base: String = "/locale-{tag}.properties", vararg locales: Locale): List<K18nExtractResult> {
        return extractInternal(targetDirectory, T::class.java, base, *locales)
    }

    fun setFallbackLocale(namespace: String, locale: Locale) {
        fallbackLocale[namespace] = locale
    }

    private fun getFromMap(map: Map<Locale, Map<String, String>>, locale: Locale, key: String): String? {
        val namespace = key.substringBefore(":")

        var useGlobal = false
        if(namespace == key) useGlobal = true

        val locale =
            if(map[locale] == null) fallbackLocale[if(useGlobal) "global" else namespace]
            else locale

        return map[locale]?.get(key)
    }

    /**
     * Find in internal registry
     */
    private fun getInternal(locale: Locale, key: String): String {
        return getFromMap(registry, locale, key) ?: "?$key?"
    }

    /**
     * Find in external registry, then internal registry
     */
    fun get(locale: Locale, key: String): String {
        return getFromMap(externalRegistry, locale, key) ?: getInternal(locale, key)
    }

    fun get(key: String): String {
        return get(Locale.ROOT, key)
    }

}
