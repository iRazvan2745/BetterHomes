package io.lwcl.listeners

import io.lwcl.BetterHomes
import net.william278.huskhomes.api.HuskHomesAPI
import net.william278.huskhomes.event.HomeDeleteEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DeleteListener(private val plugin: BetterHomes) : Listener {

    private val api: HuskHomesAPI = HuskHomesAPI.getInstance()

    //TODO: IMPLEMENT
    @EventHandler
    fun onHomeDelete(event: HomeDeleteEvent) {
        return
    }
}