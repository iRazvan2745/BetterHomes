package io.lwcl.config

import io.lwcl.BetterHomesGUI
import io.lwcl.api.objects.ModernComponent
import io.lwcl.api.objects.ModernText
import net.william278.annotaml.YamlFile
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

@YamlFile(header = """
    ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
    ┃    BetterHomesGUI Messages    ┃
    ┃      Developed by LcyDev      ┃
    ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    ┣╸ https://github.com/LcyDev/BetterHomes-GUI
    ┃
    ┗━ Formatted in MiniMessage: https://docs.advntr.dev/minimessage/format.html
    
    Variables can use <>
""")

class Locale(private val plugin: BetterHomesGUI) {

    private var localeConfig: FileConfiguration? = null

    private fun getRawString(id: String, default: String = "Translation missing error"): String {
        val key = localeConfig?.getString(id) ?: localeConfig?.getString("messages.translation_missing")?.replace("<key>", id)
        return key ?: default
    }

    private fun getRawList(id: String): MutableList<*>? {
        return localeConfig?.getList(id)
    }

    fun getLocale(id: String, default: String = ""): ModernComponent {
        return ModernText.miniModernText(getRawString(id, default))
    }

    fun getLocale(id: String, replacements: Map<String, String>, default: String = ""): ModernComponent {
        return ModernText.resolver(getRawString(id, default), replacements)
    }


    fun getLocaleList(id: String): List<String>? {
        val rawList = getRawList(id)
        return rawList?.mapNotNull {
            if (it is String) getLocale(it).toSerialized()
            else null
        }
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
        localeConfig = YamlConfiguration.loadConfiguration(file)
    }
}