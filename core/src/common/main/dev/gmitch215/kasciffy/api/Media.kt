package dev.gmitch215.kasciffy.api

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a piece of Media.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
interface Media {

    /**
     * The name of the piece of media.
     */
    val name: String

    /**
     * The file extension used when exporting the piece of media.
     */
    val extension: String

    /**
     * The creation date of this piece of media, in milliseconds past the Unix Epoch.
     */
    val creationDate: Long

    /**
     * The width of the piece of media.
     */
    val width: Int

    /**
     * The height of the piece of media.
     */
    val height: Int

    /**
     * The size of the piece of media, or [width] x [height].
     */
    val size: Int
        get() = width * height

    /**
     * Synchronously Asciffies this piece of media.
     *
     * This is not guaranteed to be available on all platforms.
     * @param map The ASCII map to use for asciffying.
     * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
     * If not specified, this is automatically calculated.
     * @return The asciffied version of this piece of media.
     */
    fun asciffySync(map: String, downScale: Int? = null): Media

    companion object {
        /**
         * The default name used for unnamed media.
         */
        const val UNNAMED_MEDIA_NAME = "media"
    }
}