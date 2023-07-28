package io.lwcl.menu

import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import io.lwcl.BetterHomes
import io.lwcl.api.enums.ButtonType
import net.william278.huskhomes.position.Home
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class PromptMenu<T : Home>(
    plugin: BetterHomes,
    private val owner: OnlineUser,
    private val position: T,
    private val parentMenu: ListMenu<T>,
    private val pageNumber: Int,
    title: String,
) : Menu(plugin, title, getConfirmMenuLayout()) {
    private val settings = plugin.settings

    override fun buildMenu(): Consumer<InventoryGui> {
        return Consumer { menu ->
            // Add filler items
            menu.setFiller(ItemStack(settings.getMaterial(settings.menuFillerItem)))
            menu.addElement(
                StaticGuiElement(
                    'f',
                    settings.getPromptIcon(ButtonType.CONFIRM),
                    { click ->
                        if (click.whoClicked is Player) {
                            api.deleteHome(owner, position.name)
                            val player = click.whoClicked as Player
                            player.sendMessage(
                                plugin.locale.getLocale(
                                    "messages.delete_home.success", mutableMapOf(
                                        "owner" to owner.username,
                                        "name" to position.name
                                    )
                                ).toComponent()
                            )
                            val user = api.adaptUser(player)
                            this.close(user)
                            parentMenu.show(user)
                            parentMenu.setPageNumber(user, pageNumber)
                            this.destroy()
                        }
                        return@StaticGuiElement true
                    },
                    """
                    ${plugin.locale.getLocale("menu.prompt.confirm.name").toLegacy()}
                    ${plugin.locale.getLocale("menu.prompt.confirm.details").toLegacy()}
                    """.trimIndent()
                )
            )
            menu.addElement(
                StaticGuiElement(
                    'c',
                    settings.getPromptIcon(ButtonType.CANCEL),
                    { click ->
                        if (click.whoClicked is Player) {
                            val player = click.whoClicked as Player
                            val user = api.adaptUser(player)
                            this.close(user)
                            parentMenu.show(user)
                            parentMenu.setPageNumber(user, pageNumber)
                            this.destroy()
                        }
                        return@StaticGuiElement true
                    },
                    """
                    ${plugin.locale.getLocale("menu.prompt.cancel.name").toLegacy()}
                    ${plugin.locale.getLocale("menu.prompt.cancel.details").toLegacy()}
                    """.trimIndent()
                )
            )
        }
    }


    companion object {
        fun <T : Home> create(plugin: BetterHomes, owner: OnlineUser, position: T, parentMenu: ListMenu<T>, pageNumber: Int): PromptMenu<T> {
            return PromptMenu(plugin, owner, position, parentMenu, pageNumber, plugin.locale.getLocale(
                    "menu.prompt.title", mapOf(
                        "action" to "deletion",
                        "object" to position.name
                    )
            ).toLegacy())
        }

        private fun getConfirmMenuLayout(): Array<String> {
            return arrayOf(
                "         ",
                "  f   c  ",
                "         "
            )
        }
    }
}