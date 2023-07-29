package io.lwcl.betterhomes.listeners

import io.lwcl.betterhomes.BetterHomes
import io.lwcl.betterhomes.menu.ListMenu
import net.william278.huskhomes.event.HomeDeleteEvent
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DeleteListener(private val plugin: BetterHomes) : Listener {

    @EventHandler
    fun onHomeDelete(event: HomeDeleteEvent) {
        val onlineViewer = event.deleter as OnlineUser
        val offlineOwner = event.home.owner
        val owner = plugin.server.getPlayer(offlineOwner.uuid) ?: return
        val onlineOwner = plugin.huskHomesAPI.adaptUser(owner)
        event.isCancelled = true

        plugin.huskHomesAPI.getUserHomes(offlineOwner).thenAccept {
            plugin.syncMethod {
                val menu = ListMenu.homes(plugin, it, onlineOwner)
                menu.show(onlineViewer)
            }
        }
    }
}