package com.asensiodev.rickandmortycharacters

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import okio.Path.Companion.toOkioPath

private const val IMAGE_MEMORY_FRACTION = 0.20
private const val IMAGE_DISK_CACHE_BYTES = 32L * 1024 * 1024

class RickAndMortyApplication :
    Application(),
    SingletonImageLoader.Factory {
    override fun newImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, IMAGE_MEMORY_FRACTION)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("character_images").toOkioPath())
                .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                .build()
        }
        .crossfade(true)
        .build()
}
