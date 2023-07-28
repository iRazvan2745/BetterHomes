package io.lwcl.commands

import cloud.commandframework.annotations.*
import io.lwcl.BetterHomes
import io.lwcl.menu.ListMenu
import net.william278.huskhomes.api.HuskHomesAPI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandDescription("Provided plugin by BetterHomesGUI")
class BetterHomesCMD(private val plugin: BetterHomes) {

    private val api: HuskHomesAPI = HuskHomesAPI.getInstance()


    @CommandMethod("bhgui reload")
    @CommandPermission("betterhomes.reload")
    fun onReload(commandSender: CommandSender) {
        plugin.reloadConfigYAML()
        plugin.reloadLocaleYML()
        commandSender.sendMessage(plugin.locale.getLocale("messages.other.config_reload").toComponent())
        plugin.logger.info("Config.yml was reloaded [!]")
    }

    //TODO: FIX
    @CommandMethod("bhgui open")
    @CommandPermission("betterhomes.open")
    fun onOpen(commandSender: CommandSender) {
        if (commandSender is Player) {
            val onlineSender = api.adaptUser(commandSender)

            api.getUserHomes(onlineSender).thenAccept {
                val menu = ListMenu.homes(plugin, it.toList(), onlineSender)
                plugin.syncMethod {
                    menu.show(onlineSender)
                }
            }
        } else {
            commandSender.sendMessage(plugin.locale.getLocale("messages.other.not_player").toComponent())
        }
    }

    //TODO: FIX
    @CommandMethod("bhgui open <player>")
    @CommandPermission("betterhomes.open.others")
    fun onOpenOthers(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") player: Player,
    ) {
        if (commandSender is Player) {
            val onlineSender = api.adaptUser(commandSender)
            val onlineOwner = api.adaptUser(player)

            api.getUserHomes(onlineOwner).thenAccept {
                val menu = ListMenu.homes(plugin, it.toList(), onlineOwner)
                plugin.syncMethod {
                    menu.show(onlineSender)
                }
            }
        } else {
            commandSender.sendMessage(plugin.locale.getLocale("messages.other.not_player").toComponent())
        }
    }
}