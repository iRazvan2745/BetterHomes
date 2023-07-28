package io.lwcl.config

import io.lwcl.api.enums.ButtonType
import io.lwcl.api.enums.PosType
import net.william278.annotaml.YamlComment
import net.william278.annotaml.YamlFile
import net.william278.annotaml.YamlKey
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@YamlFile(header = """
    ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
    ┃    BetterHomesGUI Config    ┃
    ┃     Developed by LcyDev     ┃
    ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
    ┣╸ https://github.com/LcyDev/BetterHomes-GUI
    ┃
    ┗━ Formatted in MiniMessage: https://docs.advntr.dev/minimessage/format.html
    """)

class Settings {
    @YamlKey("plugin.prefix")
    var prefix: String = "<dark_gray>[<aqua>BetterHomes</aqua>]</dark_gray>"
    @YamlKey("plugin.language")
    var language: String = "en_us"
    @YamlKey("plugin.pages")
    var pagesEnabled: Boolean = true

    @YamlKey("menu.filler")
    var menuFillerItem: String = "minecraft:white_stained_glass_pane"
    @YamlKey("menu.home.unset")
    var unsetHomeItem: String = "minecraft:gray_bed"
    @YamlKey("menu.home.claimed")
    var claimedHomeItem: String = "minecraft:white_bed"
    @YamlKey("menu.home.locked")
    var lockedHomeItem: String = "minecraft:red_bed"

    @YamlKey("menu.control.unset")
    var unsetControlItem: String = "minecraft:gray_dye"
    @YamlKey("menu.control.claimed")
    var claimedControlItem: String = "minecraft:lime_dye"
    @YamlKey("menu.control.locked")
    var lockedControlItem: String = "minecraft:red_dye"

    @YamlKey("menu.prompt.confirm")
    var confirmItem: String = "minecraft:lime_concrete"
    @YamlKey("menu.prompt.cancel")
    var cancelItem: String = "minecraft:red_concrete"

    @YamlKey("menu.paginate.first_page")
    var firstPageItem: String = "minecraft:spectral_arrow"
    @YamlKey("menu.paginate.last_page")
    var lastPageItem: String = "minecraft:spectral_arrow"
    @YamlKey("menu.paginate.next_page")
    var nextPageItem: String = "minecraft:arrow"
    @YamlKey("menu.paginate.previous_page")
    var prevPageItem: String = "minecraft:arrow"

    @YamlComment("Please don't remove this version settings")
    @YamlKey("version")
    val version: String = "1.0.0"

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

    fun getPaginateIcon(buttonType: ButtonType): ItemStack {
        return when (buttonType) {
            ButtonType.FIRST -> ItemStack(getMaterial(firstPageItem))
            ButtonType.PREVIOUS -> ItemStack(getMaterial(prevPageItem))
            ButtonType.NEXT -> ItemStack(getMaterial(nextPageItem))
            ButtonType.LAST -> ItemStack(getMaterial(lastPageItem))
            else -> ItemStack(getMaterial("air"))
        }
    }

    fun getPromptIcon(buttonType: ButtonType): ItemStack {
        return when (buttonType) {
            ButtonType.CANCEL -> ItemStack(getMaterial(cancelItem))
            ButtonType.CONFIRM -> ItemStack(getMaterial(confirmItem))
            else -> ItemStack(getMaterial("air"))
        }
    }
    
    fun getMaterial(id: String): Material {
        return Material.matchMaterial(id.replace("minecraft:", "")) ?: Material.STONE
    }
}