package io.lwcl.menu

import de.themoep.inventorygui.*
import io.lwcl.BetterHomes
import io.lwcl.api.enums.ButtonType
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

        plugin.logger.finer("[${owner.username}] Mapping positions to their index")
        for (position in positions) {
            val index = position.meta.tags[INDEX_TAG_KEY]?.toIntOrNull() ?: continue
            // Handle indexed positions
            if (!usedIndices.contains(index)) {
                usedIndices.add(index)
                positionMap[index] = position
                continue
            }
            // Remove duplicate indices and reset to null
            plugin.huskHomesAPI.editHomeMetaTags(owner, position.name) { tags ->
                tags.remove(INDEX_TAG_KEY)
            }
        }
        plugin.logger.finer("[${owner.username}] Assigning positions to new indexes")
        var index = 1
        for (position in positions) {
            if (position.meta.tags[INDEX_TAG_KEY] != null) continue
            // Handle non-indexed positions and assign new unused indices
            while (usedIndices.contains(index)) {
                index++
            }
            usedIndices.add(index)
            plugin.huskHomesAPI.editHomeMetaTags(owner, position.name) { tags ->
                tags[INDEX_TAG_KEY] = index.toString()
            }
            positionMap[index] = position
        }
        return positionMap.toMap()
    }

    private fun getPosType(position: Home?): PosType {
        return when {
            position != null -> PosType.CLAIMED
            positions.size >= maxHomes -> PosType.LOCKED
            else -> PosType.UNSET
        }
    }

    private fun recreate(player: Player) {
        val user = plugin.huskHomesAPI.adaptUser(player)
        plugin.huskHomesAPI.getUserHomes(owner).thenApply {
            plugin.syncMethod {
                val menu = homes(plugin, it, owner)
                menu.show(user)
                menu.setPageNumber(user, pageNumber)
            }
        }
    }

    override fun buildMenu(): Consumer<InventoryGui> {
        return Consumer { menu ->
            plugin.logger.finer("[${owner.username}] Building menu ...")
            // Add filler items
            menu.setFiller(ItemStack(settings.getMaterial(settings.menuFillerItem)))

            // Add pagination handling
            menu.addElement(getGroup(ButtonType.HOME))
            menu.addElement(getGroup(ButtonType.CONTROL))
            if (settings.pagesEnabled && maxPages > 1) {
                menu.addElement(getPaginationGroup())
            }
            menu.setPageNumber(pageNumber)
        }
    }

    private fun getPaginationGroup(): GuiElementGroup {
        val paginationMap = mapOf(
            "page" to pageNumber.toString(),
            "pages" to maxPages.toString(),
            "next_page" to (pageNumber + 1).toString(),
            "prev_page" to (pageNumber - 1).toString()
        )
        val group = GuiElementGroup('Q')

        group.addElement(
            GuiPageElement(
                ButtonType.FIRST.slotChar,
                settings.getPaginateIcon(ButtonType.FIRST),
                GuiPageElement.PageAction.FIRST,
                plugin.locale.getLocale("menu.pagination.first_page", paginationMap).toLegacy()
            )
        )
        group.addElement(
            GuiPageElement(
                ButtonType.PREVIOUS.slotChar,
                settings.getPaginateIcon(ButtonType.PREVIOUS),
                GuiPageElement.PageAction.PREVIOUS,
                plugin.locale.getLocale("group.pagination.previous_page", paginationMap).toLegacy()
            )
        )
        group.addElement(
            GuiPageElement(
                ButtonType.NEXT.slotChar,
                settings.getPaginateIcon(ButtonType.NEXT),
                GuiPageElement.PageAction.NEXT,
                plugin.locale.getLocale("group.pagination.next_page", paginationMap).toLegacy()
            )
        )
        group.addElement(
            GuiPageElement(
                ButtonType.LAST.slotChar,
                settings.getPaginateIcon(ButtonType.LAST),
                GuiPageElement.PageAction.LAST,
                plugin.locale.getLocale("menu.pagination.last_page", paginationMap).toLegacy()
            )
        )
        return group
    }

    private fun getGroup(buttonType: ButtonType): GuiElementGroup {
        val group = GuiElementGroup(buttonType.slotChar)

        val startIndex = (pageNumber - 1) * itemsPerPage + 1
        val endIndex = startIndex + itemsPerPage
        for (index in startIndex until endIndex) {
            val position = positionMap[index]
            // Null positions are handled later
            if (buttonType == ButtonType.HOME) {
                group.addElement(createHomeButton(position, index, getPosType(position)))
            } else {
                group.addElement(createControlButton(position, index, getPosType(position)))
            }
        }
        return group
    }

    private fun createHomeButton(position: Home?, posIndex: Int, posType: PosType): StaticGuiElement {
        val icon = getPosMaterial(position)?.let {
            ItemStack(it)
        }  ?: settings.getHomeIcon(posType)

        return getButton(position, icon, ButtonType.HOME.slotChar, "home", posType.name, posIndex) { click ->
            if (click.whoClicked is Player) {
                val player = click.whoClicked as Player
                when (click.type) {
                    ClickType.LEFT, ClickType.RIGHT, ClickType.DROP -> {
                        handleMainForHome(player, position, posType)
                    }
                    ClickType.SHIFT_LEFT -> {
                        handleAltForHome(player, position, posType)
                    }
                    else -> {
                        // Ignore any other click types (No handling required)
                    }
                }
            }
            return@getButton true
        }
    }

    private fun createControlButton(position: Home?, posIndex: Int, posType: PosType): StaticGuiElement {
        val icon = settings.getControlIcon(posType)
        return getButton(position, icon, ButtonType.CONTROL.slotChar, "control", posType.name, posIndex) { click ->
            if (click.whoClicked is Player) {
                val player = click.whoClicked as Player
                when (click.type) {
                    ClickType.LEFT, ClickType.RIGHT, ClickType.DROP -> {
                        handleMainForControl(player, position, posIndex, posType)
                    }
                    else -> {
                        // Ignore any other click types (No handling required)
                    }
                }
            }
            return@getButton true
        }
    }

    private fun getButton(
        position: Home?,
        icon: ItemStack,
        slotChar: Char,
        buttonType: String,
        posType: String,
        posIndex: Int,
        clickHandler: (GuiElement.Click) -> Boolean,
    ): StaticGuiElement {
        val buttonText = listOf(
            plugin.locale.getLocale(
                "menu.${buttonType.lowercase()}.${posType.lowercase()}.name", mapOf(
                    "suffix" to getPosSuffix(position),
                    "number" to posIndex.toString()
                )
            ).toLegacy(),
            plugin.locale.getLocale("menu.${buttonType.lowercase()}.${posType.lowercase()}.details").toLegacy(),
        ).joinToString("\n")
        return StaticGuiElement(slotChar,
            icon,
            clickHandler,
            buttonText
        )
    }

    private fun handleMainForHome(player: Player, position: Home?, posType: PosType) {
        val user = plugin.huskHomesAPI.adaptUser(player)

        if (posType == PosType.CLAIMED) {
            this.close(user)
            if (user != owner && !user.hasPermission("betterhomes.tp.others")) {
                player.sendMessage(plugin.locale.getLocale(PLUGIN_ERROR).toComponent())
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
                recreate(player)
            } catch (e: TeleportationException) {
                plugin.logger.finer("Error while teleporting ${player.name} to home ${position.name} \n  ${e.message}")
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
                player.sendMessage(plugin.locale.getLocale(PLUGIN_ERROR).toComponent())
                this.destroy()
                return
            }
            val name = "bh$posIndex"
            try {
                plugin.syncMethod {
                    plugin.huskHomes.manager.homes().createHome(user, name, user.position)
                    plugin.huskHomesAPI.editHomeMetaTags(owner, name) { tags ->
                        tags[INDEX_TAG_KEY] = posIndex.toString()
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
                plugin.logger.finer("Error while handling control for home ${position?.name} \n ${e.message}")
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
                player.sendMessage(plugin.locale.getLocale(PLUGIN_ERROR).toComponent())
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
        private const val INDEX_TAG_KEY = "betterhomes:index"
        private const val PLUGIN_ERROR = "messages.plugin.error"

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