package dev.gmitch215.kasciffy.api

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a video. This format does not include the GIF format.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
interface Video : AnimatedMedia {

    /**
     * The audio data attached to the video.
     */
    val audio: Audio?

    override fun asciffySync(map: String, downScale: Int?): Video
}