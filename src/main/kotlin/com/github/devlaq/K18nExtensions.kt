package com.github.devlaq

import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.truncate

fun CommandSender.getLocale(): Locale {
    val locale = when(this) {
        is Player -> {
            Locale.forLanguageTag(locale)
        }
        else -> {
            K18n.fallbackLocale["global"]!!
        }
    }
    return locale
}

fun CommandSender.translate(key: String) = K18n.get(getLocale(), key)

fun CommandSender.sendTranslated(key: String) = sendMessage(translate(key))
fun CommandSender.sendTranslated(vararg keys: String) = sendMessage(*( keys.map(this::translate).toTypedArray() ))
fun CommandSender.sendTranslated(sender: UUID?, key: String) = sendMessage(sender, translate(key))
fun CommandSender.sendTranslated(sender: UUID?, vararg keys: String) = sendMessage(sender, *( keys.map(this::translate).toTypedArray() ))
