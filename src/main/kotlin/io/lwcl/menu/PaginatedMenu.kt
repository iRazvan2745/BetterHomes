package io.lwcl.menu

import de.themoep.inventorygui.GuiElementGroup
import de.themoep.inventorygui.GuiPageElement
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import io.lwcl.BetterHomes
import io.lwcl.api.enums.ButtonType
import io.lwcl.api.enums.PageButton
import io.lwcl.api.enums.PosType
import net.william278.huskhomes.position.Home
import net.william278.huskhomes.teleport.TeleportationException
import net.william278.huskhomes.user.OnlineUser
import net.william278.huskhomes.util.ValidationException
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class ListMenu<T : Home>(
    plugin: BetterHomes,
    private val owner: OnlineUser,
    positions: List<T>,
    title: String,
) : Menu(plugin, title, getMenuLayout()) {
    private val settings = plugin.settings

    // Internal stuff
    private val positions: List<T> = ArrayList(positions)
    private val positionMap: Map<Int, T> = mapPositions(positions)
    private val pageNumber: Int = 1
    private val itemsPerPage: Int = 14
    private val maxHomes = plugin.huskHomesAPI.getMaxHomeSlots(owner)
    private val maxPages: Int = (maxHomes/itemsPerPage) + 1
    
    private fun mapPositions(positions: List<T>): Map<Int, T> {
        val positionMap = mutableMapOf<Int, T>()
        val usedIndices = mutableSetOf<Int>()

        plugin.logger.fine("Mapping positions to their index:")
        for (position in positions) {
            val index = position.meta.tags[HOME_INDEX_TAG_KEY]?.toIntOrNull() ?: continue
            // Handle indexed positions
            if (!usedIndices.contains(index)) {
                usedIndices.add(index)
                positionMap[index] = position
                plugin.logger.fine(" * ${position.name} : $index ")
                continue
            }
            // Remove duplicate indices and reset to null
            plugin.huskHomesAPI.editHomeMetaTags(owner, position.name) { tags ->
                tags.remove(HOME_INDEX_TAG_KEY)
            }
            plugin.logger.warning(" * Home ${position.name} had a duplicate index. Resetting to null.")
        }
        plugin.logger.fine("Assigning positions to new indexes:")
        var index = 1
        for (position in positions) {
            if (position.meta.tags[HOME_INDEX_TAG_KEY] != null) continue
            // Handle non-indexed positions and assign new unused indices
            while (usedIndices.contains(index)) {
                index++
            }
            usedIndices.add(index)
            plugin.huskHomesAPI.editHomeMetaTags(owner, position.name) { tags ->
                tags[HOME_INDEX_TAG_KEY] = index.toString()
            }
            positionMap[index] = position
            plugin.logger.fine(" * ${position.name} : $index ")
        }
        return positionMap.toMap()
    }

    //TODO: IMPLEMENT
    private fun reload(player: Player) {
        // needs lot's of changes
    }

    override fun buildMenu(): Consumer<InventoryGui> {
        return Consumer { menu ->
            plugin.logger.fine("Building menu ...")
            // Add filler items
            menu.setFiller(ItemStack(settings.getMaterial(settings.menuFillerItem)))

            // Add pagination handling
            menu.addElement(createElementGroup(ButtonType.HOME))
            menu.addElement(createElementGroup(ButtonType.CONTROL))
            if (settings.pagesEnabled && maxPages > 1) {
                plugin.logger.fine("Loaded Pagination")
                menu.addElement(createPageElement(PageButton.FIRST))
                menu.addElement(createPageElement(PageButton.PREVIOUS))
                menu.addElement(createPageElement(PageButton.NEXT))
                menu.addElement(createPageElement(PageButton.LAST))            }
            menu.setPageNumber(pageNumber)
        }
    }

    private fun createPageElement(pageButton: PageButton): GuiPageElement {
        val paginationMap = mapOf(
            "page" to pageNumber.toString(),
            "pages" to maxPages.toString(),
            "next_page" to (pageNumber + 1).toString(),
            "prev_page" to (pageNumber - 1).toString()
        )

        return GuiPageElement(
            pageButton.slotChar,
            settings.getPaginateIcon(pageButton),
            GuiPageElement.PageAction.valueOf(pageButton.name),
            plugin.locale.getLocale("menu.pagination.${pageButton.name.lowercase()}", paginationMap).toLegacy()
        )
    }

    private fun createElementGroup(buttonType: ButtonType): GuiElementGroup {
        val group = GuiElementGroup(buttonType.slotChar)

        val startIndex = (pageNumber - 1) * itemsPerPage + 1
        val endIndex = startIndex + itemsPerPage
        for (index in startIndex until endIndex) {
            val position = positionMap[index]
            group.addElement(createElement(position, index, buttonType))
        }
        return group
    }

    private fun createElement(position: Home?, posIndex: Int, buttonType: ButtonType): StaticGuiElement {
        val posType = position?.let { PosType.CLAIMED } ?: if (positions.size >= maxHomes) PosType.LOCKED else PosType.UNSET
        val icon = if (buttonType == ButtonType.HOME) {
            getPosMaterial(position)?.let { ItemStack(it) } ?: settings.getHomeIcon(posType)
        } else {
            settings.getControlIcon(posType)
        }
        return StaticGuiElement(buttonType.slotChar, icon, { click ->
            if (click.whoClicked !is Player) return@StaticGuiElement true
            val player = click.whoClicked as Player
            when (click.type) {
                ClickType.LEFT, ClickType.RIGHT, ClickType.DROP -> {
                    if (buttonType == ButtonType.HOME) {
                        handleMainForHome(player, position, posType)
                    } else {
                        handleMainForControl(player, position, posIndex, posType)
                    }
                }
                ClickType.SHIFT_LEFT -> {
                    if (buttonType == ButtonType.HOME) {
                        handleAltForHome(player, position, posType)
                    }
                }
                else -> {
                    // Ignore any other click types (No handling required)
                }
            }
            return@StaticGuiElement true
        },
            getButtonText(position, "${buttonType.name.lowercase()}.${posType.name.lowercase()}", posIndex)
        )
    }

    private fun getButtonText(position: Home?, id: String, posIndex: Int): String {
        return listOf(
            plugin.locale.getLocale("menu.$id.name", mapOf(
                "suffix" to getPosSuffix(position),
                "number" to posIndex.toString()
            )).toLegacy(),
            plugin.locale.getLocale("menu.$id.details").toLegacy(),
        ).joinToString("\n")
    }

    private fun handleMainForHome(player: Player, position: Home?, posType: PosType) {
        val user = plugin.huskHomesAPI.adaptUser(player)

        if (posType == PosType.CLAIMED) {
            this.close(user)
            if (user != owner && !user.hasPermission("betterhomes.tp.others")) {
                player.sendMessage(plugin.locale.getLocale(PLUGIN_ERROR_MESSAGE).toComponent())
                this.destroy()
                return
            }
            if (position == null) {
                player.sendMessage(
                    plugin.locale.getExpanded(
                        "messages.teleport.error", mutableMapOf(
                            "owner" to owner.username,
                            "name" to "null"
                        )
                    ).toComponent()
                )
                this.destroy()
                return
            }
            try {
                plugin.huskHomesAPI.teleportBuilder(user)
                    .target(position)
                    .toTimedTeleport()
                    .execute()
                player.sendMessage(plugin.locale.getExpanded(
                    "messages.teleport.success", mutableMapOf(
                        "owner" to owner.username,
                        "name" to position.name)
                ).toComponent())
                reload(player)
            } catch (e: TeleportationException) {
                plugin.logger.fine("Error while teleporting ${player.name} to home ${position.name} \n  ${e.message}")
                player.sendMessage(plugin.locale.getExpanded(
                    "messages.teleport.error", mutableMapOf(
                        "owner" to owner.username,
                        "name" to position.name)
                ).toComponent())
            }
            this.destroy()
        }
        return
    }

    // TODO IMPLEMENTATION:
    private fun handleAltForHome(player: Player, position: Home?, posType: PosType) {
        val user = plugin.huskHomesAPI.adaptUser(player)
        if (posType == PosType.CLAIMED) {
            position?.let {
                this.close(user)
                setPosMaterial(it, plugin.settings.getMaterial("stone"))
                setPosSuffix(it, "OwO")
                this.destroy()
            }
        }
    }

    private fun handleMainForControl(player: Player, position: Home?, posIndex: Int, posType: PosType) {
        val user = plugin.huskHomesAPI.adaptUser(player)

        if (posType == PosType.UNSET) {
            this.close(user)
            if (user != owner && !user.hasPermission("betterhomes.create.others")) {
                player.sendMessage(plugin.locale.getLocale(PLUGIN_ERROR_MESSAGE).toComponent())
                this.destroy()
                return
            }
            val name = "bh$posIndex"
            try {
                plugin.syncMethod {
                    plugin.huskHomes.manager.homes().createHome(user, name, user.position)
                    plugin.huskHomesAPI.editHomeMetaTags(owner, name) { tags ->
                        tags[HOME_INDEX_TAG_KEY] = posIndex.toString()
                    }
                }
                player.sendMessage(
                    plugin.locale.getExpanded(
                        "messages.create_home.success", mutableMapOf(
                            "name" to name,
                            "owner" to owner.username
                        )
                    ).toComponent()
                )
            } catch (e: ValidationException) {
                plugin.logger.fine("Error while handling control for home ${position?.name} \n ${e.message}")
                player.sendMessage(
                    plugin.locale.getExpanded(
                        "messages.create_home.error", mutableMapOf(
                            "name" to name,
                            "owner" to owner.username
                        )
                    ).toComponent()
                )
            }
            this.destroy()
        }
        if (posType == PosType.CLAIMED) {
            this.close(user)
            if (user != owner && !user.hasPermission("betterhomes.delete.others")) {
                player.sendMessage(plugin.locale.getLocale(PLUGIN_ERROR_MESSAGE).toComponent())
                this.destroy()
                return
            }
            if (position == null) {
                player.sendMessage(
                    plugin.locale.getExpanded(
                        "messages.delete_home.error", mutableMapOf(
                            "owner" to owner.username,
                            "name" to "null"
                        )
                    ).toComponent()
                )
                this.destroy()
                return
            }
            try {
                position.let {
                    val menu = PromptMenu.create(plugin, owner, it, getPageNumber(user))
                    menu.show(user)
                }
            } catch (ignored: ValidationException) {
                player.sendMessage(
                    plugin.locale.getExpanded(
                        "messages.delete_home.error", mutableMapOf(
                            "owner" to owner.username,
                            "name" to position.name
                        )
                    ).toComponent()
                )
            }
            this.destroy()
        }
        return
    }

    companion object {
        private const val HOME_INDEX_TAG_KEY = "betterhomes:index"
        private const val PLUGIN_ERROR_MESSAGE = "messages.plugin.error"

        fun homes(plugin: BetterHomes, homes: List<Home>, owner: OnlineUser): ListMenu<Home> {
            return ListMenu(plugin, owner, homes, plugin.locale.getLocale(
                "menu.main.title", mapOf(
                    "owner" to owner.username
                )
            ).toLegacy())
        }

        @Suppress("SpellCheckingInspection")
        private fun getMenuLayout(): Array<String> {
            return arrayOf(
                "         ",
                " hhhhhhh ",
                " ccccccc ",
                " hhhhhhh ",
                " ccccccc ",
                "ip     nl"
            )
        }
    }
}