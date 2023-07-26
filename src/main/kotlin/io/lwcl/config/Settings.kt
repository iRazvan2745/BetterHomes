package io.lwcl.config

import io.lwcl.BetterHomesGUI
import io.lwcl.api.enums.ButtonType
import io.lwcl.api.enums.PosType
import net.william278.annotaml.YamlFile
import net.william278.annotaml.YamlKey
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.io.File

@YamlFile(header = """
    ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
    ┃    BetterHomesGUI Config    ┃
    ┃     Developed by LcyDev     ┃
    ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    ┣╸ https://github.com/LcyDev/BetterHomes-GUI
    ┃
    ┗━ Formatted in MiniMessage: https://docs.advntr.dev/minimessage/format.html
    """)

class Settings(private val plugin: BetterHomesGUI) {
    @YamlKey("plugin.prefix")
    var prefix: String = "[BetterHomes]"
    @YamlKey("plugin.language")
    var language: String = "en_us"
    @YamlKey("plugin.pages")
    var pages: Boolean = true

    @YamlKey("menu.filler")
    var menuFillerItem: String = "minecraft:white_stained_glass_pane"
    @YamlKey("menu.home.unset")
    var unsetHomeItem: String = "minecraft:gray_bed"
    @YamlKey("menu.home.claimed")
    var claimedHomeItem: String = "minecraft:white_bed"
    @YamlKey("menu.home.locked")
    var lockedHomeItem: String = "minecraft:red_bed"

    @YamlKey("menu.control.unset")
    var unsetControlItem: String = "minecraft:gray_bed"
    @YamlKey("menu.control.claimed")
    var claimedControlItem: String = "minecraft:white_bed"
    @YamlKey("menu.control.locked")
    var lockedControlItem: String = "minecraft:red_bed"

    @YamlKey("menu.other.confirm")
    var confirmItem: String = "minecraft:lime_concrete"
    @YamlKey("menu.other.cancel")
    var cancelItem: String = "minecraft:red_concrete"
    @YamlKey("menu.other.back")
    var backItem: String = "minecraft:barrier"

    @YamlKey("menu.paginate.first_page")
    var firstPageItem: String = "minecraft:spectral_arrow"
    @YamlKey("menu.paginate.last_page")
    var lastPageItem: String = "minecraft:spectral_arrow"
    @YamlKey("menu.paginate.next_page")
    var nextPageItem: String = "minecraft:arrow"
    @YamlKey("menu.paginate.previous_page")
    var prevPageItem: String = "minecraft:arrow"

    fun getHomeIcon(posType: PosType): ItemStack {
        return when (posType) {
            PosType.CLAIMED -> ItemStack(getMaterial(claimedHomeItem))
            PosType.LOCKED -> ItemStack(getMaterial(lockedHomeItem))
            PosType.UNSET -> ItemStack(getMaterial(unsetHomeItem))
        }
    }

    fun getControlIcon(posType: PosType): ItemStack {
        return when (posType) {
            PosType.CLAIMED -> ItemStack(getMaterial(claimedControlItem))
            PosType.LOCKED -> ItemStack(getMaterial(lockedControlItem))
            PosType.UNSET -> ItemStack(getMaterial(unsetControlItem))
        }
    }

    fun getPaginatorIcon(buttonType: ButtonType): ItemStack {
        return when (buttonType) {
            ButtonType.FIRST -> ItemStack(getMaterial(firstPageItem))
            ButtonType.PREVIOUS -> ItemStack(getMaterial(prevPageItem))
            ButtonType.NEXT -> ItemStack(getMaterial(nextPageItem))
            ButtonType.LAST -> ItemStack(getMaterial(lastPageItem))
            else -> ItemStack(getMaterial("air"))
        }
    }

    fun getOtherIcon(buttonType: ButtonType): ItemStack {
        return when (buttonType) {
            ButtonType.CANCEL -> ItemStack(getMaterial(cancelItem))
            ButtonType.CONFIRM -> ItemStack(getMaterial(confirmItem))
            ButtonType.BACK -> ItemStack(getMaterial(backItem))
            else -> ItemStack(getMaterial("air"))
        }
    }
    
    fun getMaterial(id: String): Material {
        return Material.matchMaterial(id.replace("minecraft:", "")) ?: Material.STONE
    }

    fun getValue(key: String, default: Any): Any {
        return plugin.config[key] ?: default
    }

    fun create(fileName: String) : Settings {
        val file = File(plugin.dataFolder, fileName)
        if (!file.exists()) {
            plugin.saveResource(fileName, false)
        } else {
            plugin.logger.info("Configuration $fileName exist !")
        }
        return this
    }

    fun createConfig(configName: String, version: String): Settings {
        val file = File(plugin.dataFolder, configName)
        if (!file.exists()) {
            plugin.saveResource(configName, false)
            plugin.logger.info("Configuration $configName was successfully created !")
        } else {
            val currentVersion = plugin.config.getString("version")

            if (currentVersion.isNullOrEmpty() || currentVersion != version) {
                file.copyTo(File(plugin.dataFolder, "old_config.yml"), true)
                plugin.saveResource(configName, true)
                plugin.config["version"] = version
                plugin.saveConfig()
                plugin.logger.info("Configuration $configName is updated !")
            } else {
                plugin.logger.info("Configuration $configName is latest !")
            }
        }
        return this
    }
}