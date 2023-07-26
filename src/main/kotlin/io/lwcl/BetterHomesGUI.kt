package io.lwcl

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import io.lwcl.api.HookManager
import io.lwcl.commands.BetterHomesCMD
import io.lwcl.config.Locale
import io.lwcl.config.Settings
import io.lwcl.listeners.CreateListener
import io.lwcl.listeners.DeleteListener
import io.lwcl.listeners.ViewListener
import net.william278.huskhomes.api.HuskHomesAPI
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class BetterHomesGUI : JavaPlugin() {
    private lateinit var metrics: Metrics
    val pluginManager: PluginManager = server.pluginManager
    val locale: Locale by lazy { Locale(this) }
    val settings: Settings by lazy { Settings(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }
    var huskHomesAPI: HuskHomesAPI? = null

    private fun setupMetrics() {
        metrics = Metrics(this, 15144)
    }

    override fun onLoad() {
        settings.createConfig("config.yml", "1.0.0")
        locale.loadTranslation()
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()
        hookRegistration()
        setupMetrics()
        registerCommands()
        registerListener()
        logger.info("Plugin enabled in time ${System.currentTimeMillis() - start} ms")
    }

    override fun onDisable() {
        logger.info("Plugin is disabled")
    }

    private fun registerCommands() {
        logger.info("Registering commands with Cloud Command Framework !")

        val commandManager = createCommandManager()

        registerSuggestionProviders(commandManager)

        val annotationParser = createAnnotationParser(commandManager)
        annotationParser.parse(BetterHomesCMD(this))
    }

    private fun createCommandManager(): PaperCommandManager<CommandSender> {
        val executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.builder<CommandSender>().build()
        val mapperFunction = Function.identity<CommandSender>()
        val commandManager = PaperCommandManager(
                this,
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

    private fun registerSuggestionProviders(commandManager: PaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { commandSender, input ->
            Bukkit.getOfflinePlayers().toList()
                .filter { p ->
                    commandSender.hasPermission(".suggestion.players") && (p.name?.startsWith(input) ?: false)
                }
                .mapNotNull { it.name }
        }
    }

    private fun createAnnotationParser(commandManager: PaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        val commandMetaFunction = Function<ParserParameters, CommandMeta> { p: ParserParameters ->
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

    private fun hookRegistration() {
        hookManager.hookHuskHomes()
    }

    private fun registerListener() {
        val start = System.currentTimeMillis()
        val listeners = listOf(
            ViewListener(this),
            CreateListener(this),
            DeleteListener(this)
        )
        listeners.forEach { listener ->
            pluginManager.registerEvents(listener, this)
            logger.info("Bukkit Listener ${listener::class.simpleName} registered -> ok")
        }
        logger.info("Listeners registered(${listeners.size}) in time ${System.currentTimeMillis() - start} ms -> ok")
    }
}