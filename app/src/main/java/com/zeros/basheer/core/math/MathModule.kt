package com.zeros.basheer.core.math


import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MathModule {

    @Provides
    @Singleton
    fun provideKatexRenderer(
        @ApplicationContext context: Context
    ): KatexRenderer {
        return KatexRenderer(context).also {
            // Pre-warm on the main thread immediately after injection so
            // katex.min.js is loaded before the user opens a lesson.
            it.prewarm()
        }
    }
}