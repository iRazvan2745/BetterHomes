package io.lwcl.menu

import de.themoep.inventorygui.*
import io.lwcl.BetterHomesGUI
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
    plugin: BetterHomesGUI,
    private val owner: OnlineUser,
    positions: List<T>,
    title: String,
) : Menu(plugin, title, getMenuLayout()) {
    private val settings = plugin.settings

    // Internal stuff
    private val positions: List<T> = ArrayList(positions)
    private val pageNumber: Int = 1
    private val itemsPerPage: Int = 14
    private val maxPages: Int = api.getMaxHomeSlots(owner)/itemsPerPage

    private fun getPosType(position: Home?): PosType {
        val maxHomes = api.getMaxHomeSlots(owner)
        val currentHomes = positions.size
        return when {
            currentHomes >= maxHomes -> PosType.LOCKED
            position == null -> PosType.UNSET
            else -> PosType.CLAIMED
        }
    }

    private fun getHomeGroup(plugin: BetterHomesGUI, positions: List<T>): GuiElementGroup {
        val group = GuiElementGroup('p')
        repeat(itemsPerPage) { index ->
            val position = positions.getOrNull(index)
            group.addElement(getHomeButton(plugin, position, positions.size, index, getPosType(position)))
        }
        return group
    }

    private fun getControlGroup(plugin: BetterHomesGUI, positions: List<T>): GuiElementGroup {
        val group = GuiElementGroup('c')
        repeat(itemsPerPage) { index ->
            val position = positions.getOrNull(index)
            group.addElement(getControlButton(plugin, position, positions.size, index, getPosType(position)))
        }
        return group
    }

    private fun getHomeButton(plugin: BetterHomesGUI, position: Home?, posNumber: Int, posAmount: Int, posType: PosType): StaticGuiElement {
        val icon = settings.getHomeIcon(posType)
        return getButton(plugin, position, icon, posType, posNumber) { click ->
            handleClickForHome(click, plugin, position, posAmount, posType)
        }
    }

    private fun getControlButton(plugin: BetterHomesGUI, position: Home?, posNumber: Int, posAmount: Int, posType: PosType): StaticGuiElement {
        val icon = settings.getHomeIcon(posType)
        return getButton(plugin, position, icon, posType, posNumber) { click ->
            handleClickForControl(click, plugin, position, posAmount, posType)
        }
    }

    private fun getButton(
        plugin: BetterHomesGUI,
        position: Home?,
        icon: ItemStack,
        posType: PosType,
        posNumber: Int,
        clickHandler: (GuiElement.Click) -> Boolean
    ): StaticGuiElement {
        return StaticGuiElement('e',
            icon,
            clickHandler,
            plugin.locale.getLocale(
                "homes.items.$posType.name", mapOf(
                    "suffix" to getPosSuffix(position),
                    "number" to posNumber.toString()
                )
            ).toSerialized(),
            plugin.locale.getLocale("homes.items.$posType.details").toSerialized(),
            plugin.locale.getLocale(
                "homes.owner", mapOf(
                    "owner" to owner.username
                )
            ).toSerialized()
        )
    }

    override fun buildMenu(): Consumer<InventoryGui> {
        return Consumer { menu ->
            val paginationMap = mapOf(
                "page" to pageNumber.toString(),
                "pages" to maxPages.toString(),
                "nextpage" to (pageNumber + 1).toString(),
                "prevpage" to (pageNumber - 1).toString()
            )

            // Add filler items
            menu.setFiller(ItemStack(settings.getMaterial(settings.menuFillerItem)))

            // Add pagination handling
            menu.addElement(getHomeGroup(plugin, positions))
            menu.addElement(getControlGroup(plugin, positions))
            if (maxPages > 1) {
                menu.addElement(
                    GuiPageElement(
                        'b',
                        settings.getPaginatorIcon(ButtonType.FIRST),
                        GuiPageElement.PageAction.FIRST,
                        plugin.locale.getLocale("pagination.first_page", paginationMap).toSerialized()
                    )
                )
                menu.addElement(
                    GuiPageElement(
                        'l',
                        settings.getPaginatorIcon(ButtonType.PREVIOUS),
                        GuiPageElement.PageAction.PREVIOUS,
                        plugin.locale.getLocale("pagination.previous_page", paginationMap).toSerialized()
                    )
                )
                menu.addElement(
                    GuiPageElement(
                        'n',
                        settings.getPaginatorIcon(ButtonType.NEXT),
                        GuiPageElement.PageAction.NEXT,
                        plugin.locale.getLocale("pagination.next_page", paginationMap).toSerialized()
                    )
                )
                menu.addElement(
                    GuiPageElement(
                        'e',
                        settings.getPaginatorIcon(ButtonType.LAST),
                        GuiPageElement.PageAction.LAST,
                        plugin.locale.getLocale("pagination.last_page", paginationMap).toSerialized()
                    )
                )
            }
            menu.setPageNumber(pageNumber)
        }
    }

    private fun handleClickForHome(click: GuiElement.Click, plugin: BetterHomesGUI, position: Home?, posAmount: Int, posType: PosType): Boolean {
        var result = false

        if (click.whoClicked is Player) {
            val player = click.whoClicked as Player
            val user = api.adaptUser(player)
            when (click.type) {
                ClickType.LEFT, ClickType.RIGHT, ClickType.DROP -> {
                    when (posType) {
                        PosType.UNSET -> {
                            val name = "bh$posAmount"
                            try {
                                api.createHome(user, name, user.position)
                                player.sendMessage(plugin.locale.getLocale(
                                    "messages.create.success", mapOf(
                                        "name" to name)
                                ).toComponent())
                            } catch (ignored: ValidationException) {
                                player.sendMessage(plugin.locale.getLocale(
                                    "messages.create.error", mapOf(
                                        "name" to name)
                                ).toComponent())
                            }
                            this.close(user)
                            this.destroy()
                            result = true
                        }
                        PosType.CLAIMED -> {
                            position?.let {
                                try {
                                    api.teleportBuilder(user)
                                        .target(it)
                                        .toTimedTeleport()
                                        .execute()
                                    player.sendMessage(plugin.locale.getLocale(
                                        "messages.teleport.success", mapOf(
                                            "name" to it.name)
                                    ).toComponent())
                                } catch (ignored: TeleportationException) {
                                    player.sendMessage(plugin.locale.getLocale(
                                        "messages.teleport.error", mapOf(
                                            "name" to it.name)
                                    ).toComponent())
                                }
                            } ?: run {
                                player.sendMessage(plugin.locale.getLocale(
                                    "messages.teleport.error", mapOf(
                                        "name" to "null")
                                ).toComponent())
                            }
                            this.close(user)
                            this.destroy()
                            result = true
                        }
                        PosType.LOCKED -> {
                            player.sendMessage(plugin.locale.getLocale("messages.other.error").toComponent())
                        }
                    }
                }
                ClickType.SHIFT_LEFT -> {
                    this.close(user)
                    this.destroy()
                    result = true
                }
                else -> {
                    // Ignore any other click types (No handling required)
                }
            }
        }
        return result
    }

    private fun handleClickForControl(click: GuiElement.Click, plugin: BetterHomesGUI, position: Home?, posAmount: Int, posType: PosType): Boolean {
        var result = false

        if (click.whoClicked is Player) {
            val player = click.whoClicked as Player
            val user = api.adaptUser(player)
            when (click.type) {
                ClickType.LEFT, ClickType.RIGHT, ClickType.DROP -> {
                    when (posType) {
                        PosType.UNSET -> {
                            val name = "bh$posAmount"
                            try {
                                api.createHome(user, name, user.position)
                                player.sendMessage(plugin.locale.getLocale(
                                    "messages.create_home.success", mapOf(
                                        "name" to name)
                                ).toComponent())
                            } catch (ignored: ValidationException) {
                                player.sendMessage(plugin.locale.getLocale(
                                    "messages.create_home.error", mapOf(
                                        "name" to name)
                                ).toComponent())
                            }
                            this.close(user)
                            this.destroy()
                            result = true
                        }
                        PosType.CLAIMED -> {
                            position?.let {
                                try {
                                    api.deleteHome(user, it.name)
                                    player.sendMessage(plugin.locale.getLocale(
                                        "messages.remove_home.success", mapOf(
                                            "name" to it.name)
                                    ).toComponent())
                                } catch (ignored: ValidationException) {
                                    player.sendMessage(plugin.locale.getLocale(
                                        "messages.remove_home.error", mapOf(
                                            "name" to it.name)
                                    ).toComponent())
                                }
                                this.close(user)
                                this.destroy()
                                result = true
                            } ?: run {
                                player.sendMessage(plugin.locale.getLocale(
                                    "messages.remove_home.error", mapOf(
                                        "name" to "null")
                                ).toComponent())
                            }
                        }
                        PosType.LOCKED -> {
                            player.sendMessage(plugin.locale.getLocale("messages.other.error").toComponent())
                        }
                    }
                }
                else -> {
                    // Ignore any other click types (No handling required)
                }
            }
        }
        return result
    }

    companion object {

        fun homes(plugin: BetterHomesGUI, homes: List<Home>, owner: OnlineUser): ListMenu<Home> {
            return ListMenu(plugin, owner, homes, plugin.locale.getLocale(
                "homes.title", mapOf(
                    "owner" to owner.username
                )
            ).toSerialized())
        }

        private fun getMenuLayout(): Array<String> {
            return arrayOf(
                "         ",
                " ppppppp ",
                " ccccccc ",
                " ppppppp ",
                " ccccccc ",
                "bl     ne"
            ).drop(0).toTypedArray()
        }
    }
}