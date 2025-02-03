package dev.gmitch215.kasciffy.api

/**
 * Represents Audio data attached to a [Video].
 */
interface Audio {

    /**
     * The encoding of the audio data.
     */
    val codec: String

    /**
     * The bit rate of the audio data.
     */
    val bitRate: Int

    /**
     * The bit depth of the audio data.
     */
    val bitDepth: String

    /**
     * The number of audio channels.
     */
    val channels: Int

    /**
     * The sample rate of the audio data.
     */
    val sampleRate: Int

}