package io.lwcl.menu

import de.themoep.inventorygui.InventoryGui
import io.lwcl.BetterHomes
import net.william278.huskhomes.api.HuskHomesAPI
import net.william278.huskhomes.position.Home
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.Material
import java.util.function.Consumer

abstract class Menu(
    protected val plugin: BetterHomes,
    title: String,
    layout: Array<String>
) {
    internal val api: HuskHomesAPI = HuskHomesAPI.getInstance()
    private val gui: InventoryGui = InventoryGui(plugin, title, layout)

    protected abstract fun buildMenu(): Consumer<InventoryGui>

    fun show(user: OnlineUser) {
        buildMenu().accept(gui)
        gui.show(api.getPlayer(user))
    }

    fun setPageNumber(user: OnlineUser, pageNumber: Int) {
        gui.setPageNumber(api.getPlayer(user), pageNumber)
    }

    fun getPageNumber(user: OnlineUser): Int {
        return gui.getPageNumber(api.getPlayer(user))
    }

    fun close(user: OnlineUser) {
        gui.close(api.getPlayer(user))
    }

    fun destroy() {
        gui.destroy()
    }

    protected fun getPosMaterial(position: Home?): Material? {
        return position?.meta?.tags?.get(ICON_TAG_KEY)?.let {
            Material.matchMaterial(it)
        }
    }

    protected fun setPosMaterial(position: Home, material: Material) {
        api.editHomeMetaTags(position.owner, position.name) { tags ->
            tags[ICON_TAG_KEY] = material.key.toString()
        }
    }

    protected fun getPosSuffix(position: Home?): String {
        return position?.meta?.tags?.get(SUFFIX_TAG_KEY) ?: ""
    }

    protected fun setPosSuffix(position: Home, suffix: String) {
        api.editHomeMetaTags(position.owner, position.name) { tags ->
            tags[SUFFIX_TAG_KEY] = suffix
        }
    }

    companion object {
        private const val ICON_TAG_KEY = "betterhomesgui:icon"
        private const val SUFFIX_TAG_KEY = "betterhomesgui:suffix"
    }
}