package io.lwcl.commands

import cloud.commandframework.annotations.*
import io.lwcl.BetterHomesGUI
import io.lwcl.api.objects.ModernText
import io.lwcl.menu.ListMenu
import net.william278.huskhomes.api.HuskHomesAPI
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandDescription("Provided plugin by BetterHomesGUI")
class BetterHomesCMD(private val plugin: BetterHomesGUI) {

    private val api: HuskHomesAPI = HuskHomesAPI.getInstance()


    @CommandMethod("bhgui reload")
    @CommandPermission("betterhomes.gui.reload")
    fun onReload(commandSender: CommandSender) {
        plugin.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(plugin.translation.getKey("messages.config_reload")))
        plugin.logger.info("Config.yml was reloaded !")
        plugin.saveConfig()
        plugin.translation.reloadTranslation()
    }

    @CommandMethod("bhgui open")
    @CommandPermission("betterhomes.gui.open")
    fun onOpen(commandSender: CommandSender) {
        if (commandSender is Player) {
            val onlineSender = api.adaptUser(commandSender)

            api.getUserHomes(onlineSender).thenApply {
                val menu = ListMenu.homes(plugin, it, onlineSender)
                menu.show(onlineSender)
            }
        } else {
            commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command.</red>"))
        }
    }

    @CommandMethod("bhgui open <player>")
    @CommandPermission("betterhomes.gui.open.others")
    fun onOpenOthers(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
    ) {
        if (commandSender is Player) {
            val onlineSender = api.adaptUser(commandSender)
            val onlineOwner = api.adaptUser(offlinePlayer.player)

            api.getUserHomes(onlineOwner).thenApply {
                val menu = ListMenu.homes(plugin, it, onlineOwner)
                menu.show(onlineSender)
            }
        } else {
            commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command.</red>"))
        }
    }
}