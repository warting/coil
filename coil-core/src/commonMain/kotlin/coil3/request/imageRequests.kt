package coil3.request

import coil3.Extras
import coil3.ImageLoader
import coil3.decode.Decoder
import coil3.fetch.Fetcher
import coil3.getExtra
import coil3.size.Dimension
import coil3.size.Size

/**
 * Enable a crossfade animation when a request completes successfully.
 */
fun ImageRequest.Builder.crossfade(enable: Boolean) =
    crossfade(if (enable) DEFAULT_CROSSFADE_MILLIS else 0)

expect fun ImageRequest.Builder.crossfade(durationMillis: Int): ImageRequest.Builder

fun ImageLoader.Builder.crossfade(enable: Boolean) =
    crossfade(if (enable) DEFAULT_CROSSFADE_MILLIS else 0)

expect fun ImageLoader.Builder.crossfade(durationMillis: Int): ImageLoader.Builder

expect val ImageRequest.crossfadeMillis: Int

internal const val DEFAULT_CROSSFADE_MILLIS = 200

/**
 * Set the maximum width and height for a bitmap.
 *
 * This value is cooperative and [Fetcher]s and [Decoder]s should respect the width and height
 * values provided by [maxBitmapSize] and not allocate a bitmap with a width/height larger
 * than those dimensions.
 *
 * To allow a bitmap's size to be unrestricted pass [Dimension.Undefined] for [size]'s width and/or
 * height.
 */
fun ImageRequest.Builder.maxBitmapSize(size: Size) = apply {
    extras[maxBitmapSizeKey] = size
}

fun ImageLoader.Builder.maxBitmapSize(size: Size) = apply {
    extras[maxBitmapSizeKey] = size
}

val ImageRequest.maxBitmapSize: Size
    get() = getExtra(maxBitmapSizeKey)

val Options.maxBitmapSize: Size
    get() = getExtra(maxBitmapSizeKey)

val Extras.Key.Companion.maxBitmapSize: Extras.Key<Size>
    get() = maxBitmapSizeKey

// Use 2^12 as a maximum size as it's supported by all modern devices.
private val maxBitmapSizeKey = Extras.Key(default = Size(4_096, 4_096))

/**
 * Enables adding a file's last modified timestamp to the memory cache key when loading an image
 * from a file.
 *
 * This allows subsequent requests that load the same file to miss the memory cache if the
 * file has been updated. However, if the memory cache check occurs on the main thread
 * (see [ImageLoader.Builder.interceptorCoroutineContext]) calling this will cause a strict mode
 * violation.
 */
fun ImageRequest.Builder.addLastModifiedToFileCacheKey(enable: Boolean) = apply {
    extras[addLastModifiedToFileCacheKeyKey] = enable
}

fun ImageLoader.Builder.addLastModifiedToFileCacheKey(enable: Boolean) = apply {
    extras[addLastModifiedToFileCacheKeyKey] = enable
}

val ImageRequest.addLastModifiedToFileCacheKey: Boolean
    get() = getExtra(addLastModifiedToFileCacheKeyKey)

val Options.addLastModifiedToFileCacheKey: Boolean
    get() = getExtra(addLastModifiedToFileCacheKeyKey)

val Extras.Key.Companion.addLastModifiedToFileCacheKey: Extras.Key<Boolean>
    get() = addLastModifiedToFileCacheKeyKey

private val addLastModifiedToFileCacheKeyKey = Extras.Key(default = false)
