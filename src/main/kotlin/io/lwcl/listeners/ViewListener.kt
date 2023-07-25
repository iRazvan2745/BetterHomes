package io.lwcl.listeners

import io.lwcl.BetterHomesGUI
import io.lwcl.menu.ListMenu
import net.william278.huskhomes.api.HuskHomesAPI
import net.william278.huskhomes.event.HomeListEvent
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ViewListener(private val plugin: BetterHomesGUI) : Listener {

    private val api: HuskHomesAPI = HuskHomesAPI.getInstance()

    @EventHandler
    fun onHomeListView(event: HomeListEvent) {
        if (event.isPublicHomeList) return
        val onlineViewer = event.listViewer as? OnlineUser ?: return
        val offlineOwner = event.homes.firstOrNull()?.owner ?: return
        val owner = plugin.server.getPlayer(offlineOwner.uuid) ?: return
        val onlineOwner = api.adaptUser(owner)
        event.isCancelled = true

        val menu = ListMenu.homes(plugin, event.homes, onlineOwner)
        menu.show(onlineViewer)
    }
}