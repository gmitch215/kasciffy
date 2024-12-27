package xyz.gmitch215.kasciffy.api

/**
 * Represents a piece of Media.
 */
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

}