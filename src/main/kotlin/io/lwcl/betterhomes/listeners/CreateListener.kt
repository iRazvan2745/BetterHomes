package io.lwcl.betterhomes.listeners

import io.lwcl.betterhomes.BetterHomes
import io.lwcl.betterhomes.menu.ListMenu
import net.william278.huskhomes.event.HomeCreateEvent
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CreateListener(private val plugin: BetterHomes) : Listener {

    @EventHandler
    fun onHomeCreate(event: HomeCreateEvent) {
        val onlineViewer = event.creator as OnlineUser
        val offlineOwner = event.owner
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