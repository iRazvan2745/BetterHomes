package io.lwcl.listeners

import io.lwcl.BetterHomesGUI
import net.william278.huskhomes.api.HuskHomesAPI
import net.william278.huskhomes.event.HomeDeleteEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DeleteListener(private val plugin: BetterHomesGUI) : Listener {

    private val api: HuskHomesAPI = HuskHomesAPI.getInstance()

    @EventHandler
    fun onHomeDelete(event: HomeDeleteEvent) {
        return
    }
}