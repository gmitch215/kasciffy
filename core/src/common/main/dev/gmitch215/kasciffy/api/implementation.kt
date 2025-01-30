package dev.gmitch215.kasciffy.api

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * The scale value for the red channel in the RGB color space.
 */
const val RED_GRAYSCALE = 0.34

/**
 * The scale value for the blue channel in the RGB color space.
 */
const val BLUE_GRAYSCALE = 0.5

/**
 * The scale value for the green channel in the RGB color space.
 */
const val GREEN_GRAYSCALE = 0.16

internal suspend fun asciffy(image: Image, map: String, downScale: Int? = null): String = coroutineScope {
    val downScale0 = downScale ?: ((image.width * image.height) / (64 * 2))

    val width = image.width / downScale0
    val height = image.height / downScale0

    if (width == 0 || height == 0) return@coroutineScope ""

    val ascii = Array(height) { CharArray(width) }

    coroutineScope {
        for (x in 0 until width)
            launch {
                for (y in 0 until height) {
                    launch {
                        val red = image.red(x * downScale0, y * downScale0)
                        val green = image.green(x * downScale0, y * downScale0)
                        val blue = image.blue(x * downScale0, y * downScale0)
                        val gray = (red * RED_GRAYSCALE + green * GREEN_GRAYSCALE + blue * BLUE_GRAYSCALE).toInt()

                        val index = (gray * (map.length - 1)) / 255
                        ascii[y][x] = map[index]
                    }
                }
            }
    }

    return@coroutineScope ascii.joinToString("\n") {
        it.joinToString("")
    }
}

internal suspend fun asciffy(animation: AnimatedMedia, map: String, downScale: Int? = null): List<String> = coroutineScope {
    val frames = animation.frames
    val asciiFrames = Array(frames.size) { "" }
    println("Frames: ${frames.size}")

    coroutineScope {
        for (i in frames.indices)
            launch {
                asciiFrames[i] = asciffy(frames[i], map, downScale)
                println("${asciiFrames[i]}\n")
            }
    }

    return@coroutineScope asciiFrames.toList()
}