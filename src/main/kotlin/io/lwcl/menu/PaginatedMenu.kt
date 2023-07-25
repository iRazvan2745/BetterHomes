package io.lwcl.menu

import de.themoep.inventorygui.*
import io.lwcl.BetterHomesGUI
import io.lwcl.api.enums.ButtonType
import io.lwcl.api.enums.PosType
import io.lwcl.api.objects.ModernText
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
            group.addElement(getHomeButton(plugin, position, positions.size, getPosType(position)))
        }
        return group
    }

    private fun getControlGroup(plugin: BetterHomesGUI, positions: List<T>): GuiElementGroup {
        val group = GuiElementGroup('c')
        repeat(itemsPerPage) { index ->
            val position = positions.getOrNull(index)
            group.addElement(getControlButton(plugin, position, positions.size, getPosType(position)))
        }
        return group
    }

    private fun getHomeButton(plugin: BetterHomesGUI, position: Home?, posAmount: Int, posType: PosType): StaticGuiElement {
        return StaticGuiElement('e',
            settings.getHomeIcon(posType),
            { click -> handleClickForHome(click, plugin, position, posAmount, posType) },
            plugin.translation.getKey("homes.items.$posType.name")
        )
    }

    private fun getControlButton(plugin: BetterHomesGUI, position: Home?, posAmount: Int, posType: PosType): StaticGuiElement {
        return StaticGuiElement('e',
            settings.getControlIcon(posType),
            { click -> handleClickForControl(click, plugin, position, posAmount, posType) },
            plugin.translation.getKey("homes.items.$posType.name")
        )
    }

    override fun buildMenu(): Consumer<InventoryGui> {
        return Consumer { menu ->

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
                        plugin.translation.getKey("pagination.first_page")
                    )
                )
                menu.addElement(
                    GuiPageElement(
                        'l',
                        settings.getPaginatorIcon(ButtonType.PREVIOUS),
                        GuiPageElement.PageAction.PREVIOUS,
                        plugin.translation.getKey("pagination.previous_page")
                    )
                )
                menu.addElement(
                    GuiPageElement(
                        'n',
                        settings.getPaginatorIcon(ButtonType.NEXT),
                        GuiPageElement.PageAction.NEXT,
                        plugin.translation.getKey("pagination.next_page")
                    )
                )
                menu.addElement(
                    GuiPageElement(
                        'e',
                        settings.getPaginatorIcon(ButtonType.LAST),
                        GuiPageElement.PageAction.LAST,
                        plugin.translation.getKey("pagination.last_page")
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
                                player.sendMessage(ModernText.miniModernText("<green>Home has been set $name</green>"))
                            } catch (ignored: ValidationException) {
                                player.sendMessage(ModernText.miniModernText("<red>Could not create home $name</red>"))
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
                                } catch (ignored: TeleportationException) {
                                    player.sendMessage(ModernText.miniModernText("<red>Could not teleport to ${it.name}</red>"))
                                }
                            } ?: run {
                                player.sendMessage(ModernText.miniModernText("<red>Could not teleport to null home</red>"))
                            }
                            this.close(user)
                            this.destroy()
                            result = true
                        }
                        PosType.LOCKED -> {
                            player.sendMessage(ModernText.miniModernText("<red>You can't do that</red>"))
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
                            } catch (ignored: ValidationException) {
                                player.sendMessage(ModernText.miniModernText("<red>Could not create home $name</red>"))
                            }
                            this.close(user)
                            this.destroy()
                            result = true
                        }
                        PosType.CLAIMED -> {
                            position?.let {
                                try {
                                    api.deleteHome(user, it.name)
                                } catch (ignored: ValidationException) {
                                    player.sendMessage(ModernText.miniModernText("<red>Could not remove home ${it.name}</red>"))
                                }
                                this.close(user)
                                this.destroy()
                                result = true
                            } ?: run {
                                player.sendMessage(ModernText.miniModernText("<red>Could not remove null home</red>"))
                            }
                        }
                        PosType.LOCKED -> {
                            player.sendMessage(ModernText.miniModernText("<red>You can't do that</red>"))
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
            return ListMenu(plugin, owner, homes, plugin.translation.getKey("homes.title"))
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