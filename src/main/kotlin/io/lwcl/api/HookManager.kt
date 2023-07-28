package io.lwcl.api

import io.lwcl.BetterHomes
import net.william278.huskhomes.BukkitHuskHomes
import net.william278.huskhomes.api.HuskHomesAPI
import org.bukkit.plugin.ServicePriority

class HookManager(private val plugin: BetterHomes) {

    /**
     * Method for check if plugin is installed
     * @param pluginName - String name of plugin is CaseSensitive
     * @return Boolean
     */
    private fun isPluginInstalled(pluginName: String): Boolean {
        return plugin.pluginManager.getPlugin(pluginName) != null
    }

    /**
     * Method of registering HuskHomesAPI if plugin HuskHomes is enabled.
     */

    fun hookHuskHomes() {
        if (isPluginInstalled("HuskHomes")) {
            plugin.huskHomes = plugin.server.pluginManager.getPlugin("HuskHomes") as BukkitHuskHomes
            plugin.huskHomesAPI = HuskHomesAPI.getInstance()
            plugin.server.servicesManager.register(HuskHomesAPI::class.java, HuskHomesAPI.getInstance(), plugin, ServicePriority.Highest)
            plugin.logger.info("Successfully hooked to HuskHomes.")
        } else {
            plugin.logger.info("HuskHomes not found, please download HuskHomes.")
        }
    }
}