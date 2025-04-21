@file:OptIn(ExperimentalForeignApi::class)

package dev.gmitch215.kasciffy.api

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking

/**
 * The Native implementation of the [Image] interface.
 */
class ImageNative internal constructor(
    override val name: String,
    override val extension: String,
    override val creationDate: Long,
    override val width: Int,
    override val height: Int,
    /**
     * The RGB values of the image.
     */
    val rgbValues: Array<IntArray>
) : Image {

    override fun get(x: Int, y: Int): Int = rgbValues[y][x]

    /**
     * Asynchronously Asciffies this piece of media.
     * @param map The ASCII map to use for asciffying.
     * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
     * If not specified, this is automatically calculated.
     * @return The asciffied version of this piece of media.
     */
    suspend fun asciffy(map: String, downScale: Int?): ImageNative = asciffy0(map, downScale)

    override fun asciffySync(map: String, downScale: Int?): ImageNative = runBlocking { asciffy(map, downScale) }

}

/**
 * The chunk limits for the image.
 */
val CHUNK_LIMITS = (1024 * 1024 * 64).toULong()

/**
 * Creates a new [ImageNative] object from the specified file.
 * @param file The file to create the image from.
 * @return The created [ImageNative] object.
 */
expect fun Image(file: String): ImageNative

/**
 * Frees the resources used by this image.
 * This is called when the image is no longer needed.
 */
expect fun ImageNative.free()

/**
 * Writes this image to the given file.
 * @param file The file to write the image to.
 */
expect fun ImageNative.write(file: String)

internal expect suspend fun ImageNative.asciffy0(map: String, downScale: Int?): ImageNative