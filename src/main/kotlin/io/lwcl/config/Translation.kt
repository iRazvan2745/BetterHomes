package io.lwcl.config

import io.lwcl.BetterHomesGUI
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class Translation(private val plugin: BetterHomesGUI) {
    private var langConfiguration: FileConfiguration? = null

    private fun convertVariables(value: String): String {
        val regex = "%(\\w+)%".toRegex()
        return regex.replace(value) { matchResult ->
            "<${matchResult.groupValues[1]}>"
        }
    }

    fun getKey(value: String): String {
        val key = langConfiguration?.getString(value)
                ?: langConfiguration?.getString("messages.translation_missing")?.replace("<key>", value)
        return convertVariables(key ?: "Translation missing error")
    }

    fun getList(value: String): MutableList<*>? {
        return langConfiguration?.getList(value)
    }

    fun reloadTranslation() {
        loadTranslation()
    }

    fun loadTranslation() {
        val file = File("${plugin.dataFolder}/locale/", "messages.yml")
        if (!file.exists() && file.parentFile.mkdirs()) {
            plugin.saveResource("locale/messages.yml", false)
        }
        try {
            file.createNewFile()
            plugin.saveConfig()
            plugin.reloadConfig()
        } catch (e: IOException) {
            plugin.logger.info("Messages file doesn't exist!")
        }
        langConfiguration = YamlConfiguration.loadConfiguration(file)
    }
}