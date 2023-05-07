package com.github.devlaq.loader

import java.io.File
import java.io.InputStream

interface K18nLoader {

    fun load(stream: InputStream): Map<String, String>

}
