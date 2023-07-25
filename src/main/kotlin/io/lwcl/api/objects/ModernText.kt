package io.lwcl.api.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

object ModernText {

    @JvmStatic
    val miniMessage: MiniMessage by lazy { initMiniMessage() }

    fun buildResolver(message: String, replacements: Map<String, String>): Component {
        val resolvers = replacements.entries.map {(key, value) -> Placeholder.parsed(key, value)}
        return miniModernText(
            message,
            TagResolver.builder().apply {
                resolvers.forEach { value -> resolver(value) }
            }.build()
        )
    }

    @JvmStatic
    fun miniModernText(message: String): Component {
        return miniMessage.deserialize(message)
    }

    @JvmStatic
    fun miniModernText(message: String, resolver: TagResolver): Component {
        return miniMessage.deserialize(message, resolver)
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