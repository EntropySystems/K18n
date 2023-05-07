package com.github.devlaq.type

data class K18nExtractResult(
    val from: String,
    val to: String,
    val type: K18nExtractResultType
)

enum class K18nExtractResultType {
    Success,
    TargetExist,
    NoSource
}
