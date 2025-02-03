package dev.gmitch215.kasciffy.api

/**
 * Represents a video. This format does not include the GIF format.
 */
interface Video : AnimatedMedia {

    /**
     * The audio data attached to the video.
     */
    val audio: Audio?

}