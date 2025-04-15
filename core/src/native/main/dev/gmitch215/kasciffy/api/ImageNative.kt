@file:OptIn(ExperimentalForeignApi::class)

package dev.gmitch215.kasciffy.api

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import spng.spng_ctx
import spng.spng_ctx_free
import spng.spng_ihdr

/**
 * The Native implementation of the [Image] interface.
 */
class ImageNative internal constructor(
    override val name: String,
    override val extension: String,
    override val creationDate: Long,
    /**
     * The pointer to the [spng_ctx] object.
     */
    val ctxPointer: CPointer<spng_ctx>,
    /**
     * The [spng_ihdr] object.
     */
    val ihdr: spng_ihdr,
    /**
     * The RGB values of the image.
     */
    val rgbValues: Array<IntArray>
) : Image {

    override val width: Int
        get() = ihdr.width.toInt()

    override val height: Int
        get() = ihdr.height.toInt()

    /**
     * Frees the [spng_ctx] pointer.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun free() = spng_ctx_free(ctxPointer)

    override fun get(x: Int, y: Int): Int = rgbValues[y][x]

    /**
     * Asynchronously Asciffies this piece of media.
     * @param map The ASCII map to use for asciffying.
     * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
     * If not specified, this is automatically calculated.
     * @return The asciffied version of this piece of media.
     */
    suspend fun asciffy(map: String, downScale: Int?): Image {
        TODO("Not yet implemented")
    }

    override fun asciffySync(map: String, downScale: Int?): Image {
        TODO("Not yet implemented")
    }

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