package io.lwcl.betterhomes.menu

import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import io.lwcl.betterhomes.BetterHomes
import io.lwcl.betterhomes.api.enums.ButtonType
import net.william278.huskhomes.position.Home
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class PromptMenu<T : Home>(
    plugin: BetterHomes,
    private val owner: OnlineUser,
    private val position: T,
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
                    ButtonType.CONFIRM.slotChar,
                    settings.getPromptIcon(ButtonType.CONFIRM),
                    { click ->
                        if (click.whoClicked is Player) {
                            plugin.syncMethod {
                                plugin.huskHomes.manager.homes().deleteHome(owner, position.name)
                                val player = click.whoClicked as Player
                                player.sendMessage(
                                    plugin.locale.getExpanded(
                                        "messages.delete_home.success", mutableMapOf(
                                            "owner" to owner.username,
                                            "name" to position.name
                                        )
                                    ).toComponent()
                                )
                                recreateParent(player)
                            }
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
                    ButtonType.CANCEL.slotChar,
                    settings.getPromptIcon(ButtonType.CANCEL),
                    { click ->
                        if (click.whoClicked is Player) {
                            val player = click.whoClicked as Player
                            recreateParent(player)
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

    private fun recreateParent(player: Player) {
        val user = plugin.huskHomesAPI.adaptUser(player)
        this.close(user)
        plugin.huskHomesAPI.getUserHomes(owner).thenAccept {
            plugin.syncMethod {
                val newParent = ListMenu.homes(plugin, it, owner)
                newParent.show(user)
                newParent.setPageNumber(user, pageNumber)
            }
        }
        this.destroy()
    }

    companion object {
        fun <T : Home> create(plugin: BetterHomes, owner: OnlineUser, position: T, pageNumber: Int): PromptMenu<T> {
            return PromptMenu(plugin, owner, position, pageNumber, plugin.locale.getLocale(
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