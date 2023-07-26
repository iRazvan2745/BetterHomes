package io.lwcl.menu

import de.themoep.inventorygui.InventoryGui
import io.lwcl.BetterHomesGUI
import net.william278.huskhomes.api.HuskHomesAPI
import net.william278.huskhomes.position.Home
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.Material
import java.util.function.Consumer

abstract class Menu(
    protected val plugin: BetterHomesGUI,
    protected val title: String,
    protected val layout: Array<String>
) {
    protected val api: HuskHomesAPI = HuskHomesAPI.getInstance()
    protected val gui: InventoryGui = InventoryGui(plugin, title, layout)

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

    protected fun getPosMaterial(position: Home): Material? {
        val tags = position.meta.tags
        return tags[ICON_TAG_KEY]?.let { Material.matchMaterial(it) }
    }

    protected fun setPosMaterial(position: Home, material: Material) {
        val tags = position.meta.tags
        tags[ICON_TAG_KEY] = material.key.toString()
        api.setHomeMetaTags(position, tags)
    }

    protected fun getPosSuffix(position: Home?): String {
        val tags = position?.meta?.tags ?: return ""
        return tags[SUFFIX_TAG_KEY] ?: ""
    }

    protected fun setPosSuffix(position: Home, suffix: String) {
        val tags = position.meta.tags
        tags[SUFFIX_TAG_KEY] = suffix
        api.setHomeMetaTags(position, tags)
    }

    companion object {
        private const val ICON_TAG_KEY = "betterhomesgui:icon"
        private const val SUFFIX_TAG_KEY = "betterhomesgui:suffix"
    }
}