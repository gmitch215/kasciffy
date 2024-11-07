package xyz.gmitch215.kasciffy.api

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

internal suspend fun asciffy(image: Image, map: String): String = coroutineScope {
    val width = image.width
    val height = image.height

    val ascii = Array(width) { CharArray(height) }

    for (x in 0 until width)
        launch {
            for (y in 0 until height) {
                launch {
                    val red = image.red(x, y)
                    val green = image.green(x, y)
                    val blue = image.blue(x, y)
                    val gray = (red * RED_GRAYSCALE + green * GREEN_GRAYSCALE + blue * BLUE_GRAYSCALE).toInt()

                    val index = (gray * (map.length - 1)) / 255
                    ascii[x][y] = map[index]
                }
            }
        }

    return@coroutineScope ascii.joinToString("\n") {
        it.joinToString("")
    }
}