package io.lwcl.listeners

import io.lwcl.BetterHomes
import net.william278.huskhomes.api.HuskHomesAPI
import net.william278.huskhomes.event.HomeCreateEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CreateListener(private val plugin: BetterHomes) : Listener {

    private val api: HuskHomesAPI = HuskHomesAPI.getInstance()

    //TODO: IMPLEMENT
    @EventHandler
    fun onHomeCreate(event: HomeCreateEvent) {
        return
//        val onlineViewer = event.creator as? OnlineUser ?: return
//        val offlineOwner = event.owner ?: return
//        val owner = plugin.server.getPlayer(offlineOwner.uuid) ?: return
//        val onlineOwner = api.adaptUser(owner)
//        event.isCancelled = true
//
//        api.getUserHomes(offlineOwner).thenAccept {
//            val menu = ListMenu.homes(plugin, it, onlineOwner)
//            menu.show(onlineViewer)
//        }
    }
}