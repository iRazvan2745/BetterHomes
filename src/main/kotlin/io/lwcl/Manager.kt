package io.lwcl

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import io.lwcl.config.Settings
import net.william278.annotaml.Annotaml
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException

class Manager(private val plugin: BetterHomesGUI) {

    internal fun loadSettings(): Annotaml<Settings> {
        val configFile = File(plugin.dataFolder, "config.yml")
        return try {
            Annotaml.create(configFile, Settings::class.java)
        } catch (e: Exception) {
            when (e) {
                is InvocationTargetException, is InstantiationException, is IllegalAccessException, is IOException -> {
                    throw IllegalStateException("Failed to load file: ${configFile.name}", e)
                }
                else -> {
                    throw e
                }
            }
        }
    }

    internal fun registerSuggestionProviders(commandManager: PaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { commandSender, input ->
            Bukkit.getOfflinePlayers().toList()
                .filter { p ->
                    commandSender.hasPermission(".suggestion.players") && (p.name?.startsWith(input) ?: false)
                }
                .mapNotNull { it.name }
        }
    }

    internal fun createAnnotationParser(commandManager: PaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        val commandMetaFunction = java.util.function.Function<ParserParameters, CommandMeta> { p: ParserParameters ->
            CommandMeta.simple() // Decorate commands with descriptions
                .with(CommandMeta.DESCRIPTION, p[StandardParameters.DESCRIPTION, "No Description"])
                .build()
        }

        return AnnotationParser( /* Manager */
            commandManager,  /* Command sender type */
            CommandSender::class.java,  /* Mapper for command meta instances */
            commandMetaFunction
        )
    }

    internal fun createCommandManager(): PaperCommandManager<CommandSender> {
        val executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.builder<CommandSender>().build()
        val mapperFunction = java.util.function.Function.identity<CommandSender>()
        val commandManager = PaperCommandManager(
            plugin,
            executionCoordinatorFunction,
            mapperFunction,
            mapperFunction
        )

        if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager()?.setNativeNumberSuggestions(false)
        }

        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as PaperCommandManager<*>).registerAsynchronousCompletions()
        }

        return commandManager
    }
}