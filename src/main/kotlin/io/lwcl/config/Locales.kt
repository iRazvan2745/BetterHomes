package io.lwcl.config

import io.lwcl.BetterHomes
import io.lwcl.api.objects.ModernComponent
import io.lwcl.api.objects.ModernText
import net.william278.huskhomes.libraries.boostedyaml.YamlDocument
import java.io.File
import java.io.IOException

class Locales(private val plugin: BetterHomes) {

    private var localeYML: YamlDocument? = null

    private fun getRawString(id: String): String {
        val key = localeYML?.getString(id) ?: localeYML?.getString("messages.translation_missing")?.replace("<key>", id)
        return key ?: "Translation missing error: $id"
    }

    fun getLocale(id: String): ModernComponent {
        val replacements = mapOf(
            "prefix" to plugin.settings.prefix
        )
        return ModernText.resolver(getRawString(id), replacements)
    }

    fun getLocale(id: String, replacements: Map<String, String>): ModernComponent {
        return ModernText.resolver(getRawString(id), replacements)
    }

    fun getExpanded(id: String, replacements: MutableMap<String, String>): ModernComponent {
        replacements["prefix"] = plugin.settings.prefix
        return ModernText.resolver(getRawString(id), replacements)
    }

    fun setLanguageFile(langKey: String) {
        val fileName = "messages-$langKey.yml"
        val file = File("${plugin.dataFolder}/locale/", fileName)
        try {
            if (!file.exists() && file.parentFile.mkdirs()) {
                plugin.saveResource("locale/$fileName", false)
            }
            file.createNewFile()
            plugin.settings.language = langKey
            plugin.saveConfigYAML()
            localeYML = YamlDocument.create(file, plugin.getResource(fileName))
            plugin.logger.info("Loaded translation $fileName [!]")
        } catch (e: IllegalArgumentException) {
            plugin.logger.warning("Unsupported language, lang file doesn't exist [!]")
        } catch (e: IOException) {
            plugin.logger.warning("Invalid language file name [!]")
        }
    }
}