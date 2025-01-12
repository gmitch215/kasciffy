package xyz.gmitch215.kasciffy.api

/**
 * Represents an Animated piece of Media.
 */
interface AnimatedMedia : Media, Iterable<Image> {

    /**
     * The frames per second of the animated image.
     */
    val fps: Int

    /**
     * Gets the frames inside this animated image.
     * @return The frames of this animated image
     */
    val frames: List<Image>

    /**
     * Gets the frame at the specified index.
     * @param index The index of the frame.
     * @return The frame at the specified index.
     */
    operator fun get(index: Int): Image

    override fun iterator(): Iterator<Image> = frames.iterator()

}