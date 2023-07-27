package io.lwcl.api.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer


data class ModernComponent(val value: Component) {
    fun toLegacy(): String {
        return ModernText.serializeComponent(value)
    }

    fun toComponent(): Component {
        return value
    }
}

object ModernText {
    @JvmStatic
    val miniMessage: MiniMessage by lazy { initMiniMessage() }
    private val legacySerializer: LegacyComponentSerializer by lazy { initLegacyMessage() }

    fun resolver(message: String, replacements: Map<String, String>): ModernComponent {
        val resolvers = replacements.entries.map {(key, value) -> Placeholder.parsed(key, value)}
        return miniModernText(
            convertVariables(message),
            TagResolver.builder().apply {
                resolvers.forEach { value -> resolver(value) }
            }.build()
        )
    }

    fun serializeComponent(component: Component): String {
        return legacySerializer.serialize(component)
    }

    @JvmStatic
    fun miniModernText(message: String): ModernComponent {
        val component = miniMessage.deserialize(convertVariables(message))
        return ModernComponent(component)
    }

    @JvmStatic
    fun miniModernText(message: String, resolver: TagResolver): ModernComponent {
        val component = miniMessage.deserialize(convertVariables(message), resolver)
        return ModernComponent(component)
    }

    private fun convertVariables(value: String): String {
        val regex = """[%{](\w+)[%}]""".toRegex()
        return regex.replace(value) { matchResult ->
            "<${matchResult.groupValues[1]}>"
        }
    }

    private fun initLegacyMessage(): LegacyComponentSerializer {
        return LegacyComponentSerializer.builder().build()
    }

    private fun initMiniMessage(): MiniMessage {
        return MiniMessage.builder()
            .strict(false)
            .tags(
                TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.clickEvent())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.font())
                    .resolver(StandardTags.hoverEvent())
                    .resolver(StandardTags.insertion())
                    .resolver(StandardTags.rainbow())
                    .resolver(StandardTags.newline())
                    .resolver(StandardTags.transition())
                    .resolver(StandardTags.gradient())
                    .build()
            )
            .build()
    }
}