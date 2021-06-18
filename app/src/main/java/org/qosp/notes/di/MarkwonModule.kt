package org.qosp.notes.di

import android.content.Context
import android.text.util.Linkify
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import io.noties.markwon.*
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.handler.EmphasisEditHandler
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.qosp.notes.ui.editor.markdown.*
import javax.inject.Named

@Module
@InstallIn(FragmentComponent::class)
object MarkwonModule {
    const val SUPPORTS_IMAGES = "SUPPORTS_IMAGES"

    @Provides
    @FragmentScoped
    fun provideBaseMarkwonBuilder(@ApplicationContext context: Context): Markwon.Builder {
        return Markwon.builder(context)
            .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS))
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .usePlugin(MovementMethodPlugin.create(BetterLinkMovementMethod.getInstance()))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver(LinkResolverDef())
                }
            })
    }

    @Provides
    @Named(SUPPORTS_IMAGES)
    @FragmentScoped
    fun provideMarkwonInstanceWithImageSupport(
        @ApplicationContext context: Context,
        builder: Markwon.Builder
    ): Markwon {
        return builder
            .usePlugin(CoilImagesPlugin.create(context))
            .build()
    }

    @Provides
    @FragmentScoped
    fun provideBaseMarkwonInstance(builder: Markwon.Builder): Markwon {
        return builder.build()
    }

    @Provides
    @FragmentScoped
    fun provideMarkwonEditor(markwon: Markwon): MarkwonEditor {
        return MarkwonEditor.builder(markwon)
            .useEditHandler(EmphasisEditHandler())
            .useEditHandler(StrongEmphasisEditHandler())
            .useEditHandler(CodeHandler())
            .useEditHandler(CodeBlockHandler())
            .useEditHandler(BlockQuoteHandler())
            .useEditHandler(StrikethroughHandler())
            .useEditHandler(HeadingHandler())
            .build()
    }
}
