package io.lwcl

import io.lwcl.api.HookManager
import io.lwcl.commands.BetterHomesCMD
import io.lwcl.config.Locales
import io.lwcl.config.Settings
import io.lwcl.listeners.CreateListener
import io.lwcl.listeners.DeleteListener
import io.lwcl.listeners.ViewListener
import net.william278.annotaml.Annotaml
import net.william278.huskhomes.api.HuskHomesAPI
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class BetterHomes : JavaPlugin() {
    val pluginManager: PluginManager = server.pluginManager
    private val hookManager: HookManager by lazy { HookManager(this) }
    private val manager: Manager by lazy { Manager(this) }

    val locale: Locales by lazy { Locales(this) }
    private lateinit var configYML: Annotaml<Settings>
    lateinit var settings: Settings

    var huskHomesAPI: HuskHomesAPI? = null

    private lateinit var metrics: Metrics

    private fun setupMetrics() {
        metrics = Metrics(this, 15144)
    }

    override fun onLoad() {
        settings = Settings()
        reloadConfigYAML()
        locale.setLanguageFile(settings.language)
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

    private fun hookRegistration() {
        hookManager.hookHuskHomes()
    }

    private fun registerCommands() {
        logger.info("Registering commands with Cloud Command Framework !")

        val commandManager = manager.createCommandManager()

        manager.registerSuggestionProviders(commandManager)

        val annotationParser = manager.createAnnotationParser(commandManager)
        annotationParser.parse(BetterHomesCMD(this))
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

    fun saveConfigYAML() {
        val file = File(dataFolder, "config.yml")
        configYML.save(file)
    }

    fun reloadConfigYAML() {
        configYML = manager.loadSettings()
        settings = configYML.get()
        if (settings.version == "1.0.0") {
            logger.info("Configuration config.yml is the latest [!]")
        } else {
            val file = File(dataFolder, "config.yml")
            file.copyTo(File(dataFolder, "old_config.yml"))
            configYML.save(file)
            logger.info("Configuration config.yml was outdated [!]")
        }
    }
}