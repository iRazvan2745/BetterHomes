package io.lwcl.menu

import de.themoep.inventorygui.GuiPageElement
import de.themoep.inventorygui.InventoryGui
import io.lwcl.BetterHomesGUI
import io.lwcl.api.enums.ButtonType
import net.william278.huskhomes.position.Home
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class ConfirmMenu<T : Home>(
    plugin: BetterHomesGUI,
    private val position: T,
    private val owner: OnlineUser,
    title: String
) : Menu(plugin, title, getConfirmMenuLayout()) {
    private val settings = plugin.settings

    private fun deleteTemp() {
        api.deleteHome(owner, position.name)
//        player.sendMessage(
//            plugin.locale.getLocale(
//                "messages.remove_home.success", mutableMapOf(
//                    "owner" to owner.username,
//                    "name" to position.name
//                )
//            ).toComponent()
//        )
    }

    override fun buildMenu(): Consumer<InventoryGui> {
        return Consumer { menu ->
            // Add filler items
            menu.setFiller(ItemStack(settings.getMaterial(settings.menuFillerItem)))
            menu.addElement(
                GuiPageElement(
                    'f',
                    settings.getPaginateIcon(ButtonType.FIRST),
                    GuiPageElement.PageAction.FIRST,
                    plugin.locale.getLocale("prompt.items.confirm.name").toLegacy(),
                    plugin.locale.getLocale("prompt.items.confirm.details").toLegacy()
                )
            )
            menu.addElement(
                GuiPageElement(
                    'c',
                    settings.getOtherIcon(ButtonType.CANCEL),
                    GuiPageElement.PageAction.PREVIOUS,
                    plugin.locale.getLocale("prompt.items.cancel.name").toLegacy(),
                    plugin.locale.getLocale("prompt.items.cancel.details").toLegacy()
                )
            )
            menu.addElement(
                GuiPageElement(
                    'x',
                    settings.getPaginateIcon(ButtonType.BACK),
                    GuiPageElement.PageAction.NEXT,
                    plugin.locale.getLocale("prompt.items.back.name").toLegacy(),
                    plugin.locale.getLocale("prompt.items.back.details").toLegacy()
                )
            )
        }
    }


    companion object {
        fun delete(plugin: BetterHomesGUI, position: Home, owner: OnlineUser): ConfirmMenu<Home> {
            return ConfirmMenu(plugin, position, owner, plugin.locale.getLocale(
                "confirm.title", mutableMapOf(
                    "action" to "delete",
                    "object" to position.name
                )
            ).toLegacy())
        }

        private fun getConfirmMenuLayout(): Array<String> {
            return arrayOf(
                "         ",
                "  f   c  ",
                "    x    "
            ).drop(0).toTypedArray()
        }
    }
}