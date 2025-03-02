package dev.gmitch215.kasciffy.api

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents an animated image format. This differentiates from [Video] since it does not
 * contain audio.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
interface AnimatedImage : AnimatedMedia {

    override fun asciffySync(map: String, downScale: Int?): AnimatedImage
    
}