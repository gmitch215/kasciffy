@file:OptIn(ExperimentalJsFileName::class, ExperimentalJsExport::class)

@file:JvmName("Kasciffy")
@file:JsFileName("kasciffy")

package dev.gmitch215.kasciffy.api

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.ExperimentalJsFileName
import kotlin.js.JsExport
import kotlin.js.JsFileName
import kotlin.js.JsName
import kotlin.jvm.JvmName

/**
 * The scale value for the red channel in the RGB color space.
 */
@JsExport
const val RED_GRAYSCALE = 0.34

/**
 * The scale value for the blue channel in the RGB color space.
 */
@JsExport
const val BLUE_GRAYSCALE = 0.5

/**
 * The scale value for the green channel in the RGB color space.
 */
@JsExport
const val GREEN_GRAYSCALE = 0.16

/**
 * Asynchronously asciffies this image.
 * @param map The ASCII map to use for asciffying.
 * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
 * If not specified, this is automatically calculated.
 * @return The asciffied version of this image.
 */
suspend fun Image.asciffyAsString(map: String, downScale: Int?): String = asciffy0(this, map, downScale)

@JsName("asciffy0_image")
internal suspend fun asciffy0(image: Image, map: String, downScale: Int?? = null): String = coroutineScope {
    val downScale0 = downScale ?: ((image.width * image.height) / (64 * 2))

    val width = image.width / downScale0
    val height = image.height / downScale0

    if (width == 0 || height == 0) return@coroutineScope ""

    val ascii = Array(height) { CharArray(width) }
    val map0 = map.reversed()

    coroutineScope {
        for (x in 0 until width)
            launch {
                for (y in 0 until height) {
                    launch {
                        val red = image.red(x * downScale0, y * downScale0)
                        val green = image.green(x * downScale0, y * downScale0)
                        val blue = image.blue(x * downScale0, y * downScale0)
                        val gray = (red * RED_GRAYSCALE + green * GREEN_GRAYSCALE + blue * BLUE_GRAYSCALE).toInt()

                        val index = (gray * (map0.length - 1)) / 255
                        ascii[y][x] = map0[index]
                    }
                }
            }
    }

    return@coroutineScope ascii.joinToString("\n") {
        it.joinToString("")
    }
}

/**
 * Asynchronously asciffies this animated media.
 *
 * This is not guaranteed to be available on all platforms.
 * @param map The ASCII map to use for asciffying.
 * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
 * If not specified, this is automatically calculated.
 * @return The asciffied version of this animated media.
 */
suspend fun AnimatedMedia.asciffyAsString(map: String, downScale: Int?): List<String> = asciffy0(this, map, downScale)

@JsName("asciffy0_animated")
internal suspend fun asciffy0(animation: AnimatedMedia, map: String, downScale: Int?? = null): List<String> = coroutineScope {
    val frames = animation.frames
    val asciiFrames = Array(frames.size) { "" }

    coroutineScope {
        for (i in frames.indices)
            launch {
                asciiFrames[i] = asciffy0(frames[i], map, downScale)
            }
    }

    return@coroutineScope asciiFrames.toList()
}