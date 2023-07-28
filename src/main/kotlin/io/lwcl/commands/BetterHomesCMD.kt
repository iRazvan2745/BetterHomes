package io.lwcl.commands

import cloud.commandframework.annotations.*
import io.lwcl.BetterHomes
import io.lwcl.api.objects.ModernText
import io.lwcl.menu.ListMenu
import net.william278.huskhomes.user.OnlineUser
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandDescription("Provided plugin by BetterHomesGUI")
class BetterHomesCMD(private val plugin: BetterHomes) {

    @CommandMethod("betterhomes|bhgui reload")
    @CommandPermission("betterhomes.reload")
    fun onReload(commandSender: CommandSender) {
        plugin.reloadConfigYAML()
        plugin.reloadLocaleYML()
        commandSender.sendMessage(plugin.locale.getLocale("messages.plugin.config_reload").toComponent())
        plugin.logger.info("Config.yml was reloaded [!]")
    }

    @CommandMethod("betterhomes|bhgui open")
    @CommandPermission("betterhomes.open")
    fun onOpen(commandSender: CommandSender) {
        if (commandSender is Player) {
            val onlineSender = plugin.huskHomesAPI.adaptUser(commandSender)

            plugin.huskHomesAPI.getUserHomes(onlineSender).thenAccept {
                plugin.syncMethod {
                    val menu = ListMenu.homes(plugin, it.toList(), onlineSender)
                    menu.show(onlineSender)
                }
            }
        } else {
            commandSender.sendMessage(plugin.locale.getLocale(NOT_A_PLAYER).toComponent())
        }
    }

    @CommandMethod("betterhomes|bhgui open <player>")
    @CommandPermission("betterhomes.open.others")
    fun onOpenOthers(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") player: Player,
    ) {
        if (commandSender is Player) {
            val onlineSender = plugin.huskHomesAPI.adaptUser(commandSender)
            val onlineOwner = plugin.huskHomesAPI.adaptUser(player)

            plugin.huskHomesAPI.getUserHomes(onlineOwner).thenAccept {
                plugin.syncMethod {
                    val menu = ListMenu.homes(plugin, it.toList(), onlineOwner)
                    menu.show(onlineSender)
                }
            }
        } else {
            commandSender.sendMessage(plugin.locale.getLocale(NOT_A_PLAYER).toComponent())
        }
    }

    @CommandMethod("betterhomes|bhgui icon <home> <material>")
    @CommandPermission("betterhomes.change.icon")
    fun onMaterial(
        commandSender: CommandSender,
        @Argument(value = "home", suggestions = "homes") homeName: String,
        @Argument(value = "material", suggestions = "materials") materialID: String,
    ) {
        if (commandSender is Player) {
            val onlineSender = plugin.huskHomesAPI.adaptUser(commandSender)
            changeMaterial(commandSender, onlineSender, homeName, materialID)
        } else {
            commandSender.sendMessage(plugin.locale.getLocale(NOT_A_PLAYER).toComponent())
        }
    }

    @CommandMethod("betterhomes|bhgui suffix <home> <suffix>")
    @CommandPermission("betterhomes.change.suffix")
    fun onSuffix(
        commandSender: CommandSender,
        @Argument(value = "home", suggestions = "homes") homeName: String,
        @Argument(value = "suffix") suffix: String,
    ) {
        if (commandSender is Player) {
            val onlineSender = plugin.huskHomesAPI.adaptUser(commandSender)
            changeSuffix(commandSender, onlineSender, homeName, suffix)
        } else {
            commandSender.sendMessage(plugin.locale.getLocale(NOT_A_PLAYER).toComponent())
        }
    }

    private fun changeSuffix(sender: CommandSender, owner: OnlineUser, homeName: String, suffix: String) {
        plugin.huskHomesAPI.editHomeMetaTags(owner, homeName) { tags ->
            tags["betterhomes:suffix"] = ModernText.miniModernText(suffix).toLegacy()
        }
        sender.sendMessage(plugin.locale.getLocale("messages.other.change_material").toComponent())
    }

    private fun changeMaterial(sender: CommandSender, owner: OnlineUser, homeName: String, materialID: String) {
        val material = plugin.settings.getMaterialOrNull(materialID)
        if (material != null) {
            plugin.huskHomesAPI.editHomeMetaTags(owner, homeName) { tags ->
                tags["betterhomes:icon"] = material.toString()
            }
            sender.sendMessage(plugin.locale.getLocale("messages.other.change_material").toComponent())
        } else {
            sender.sendMessage(plugin.locale.getLocale("messages.other.invalid_material").toComponent())
        }
    }

    companion object {
        private const val NOT_A_PLAYER = "messages.plugin.not_player"
    }
}