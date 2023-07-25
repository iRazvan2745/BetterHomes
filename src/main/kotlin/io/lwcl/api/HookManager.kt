package io.lwcl.api

import io.lwcl.BetterHomesGUI
import net.william278.huskhomes.api.HuskHomesAPI
import org.bukkit.plugin.ServicePriority

class HookManager(private val plugin: BetterHomesGUI) {

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
            plugin.huskHomesAPI = HuskHomesAPI.getInstance()
            plugin.server.servicesManager.register(HuskHomesAPI::class.java, HuskHomesAPI.getInstance(), plugin, ServicePriority.Highest)
            plugin.logger.info("Successfully hooked to HuskHomes.")
        } else {
            plugin.logger.info("HuskHomes not found, please download HuskHomes.")
        }
    }
}