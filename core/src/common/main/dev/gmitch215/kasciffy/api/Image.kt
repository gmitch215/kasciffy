package dev.gmitch215.kasciffy.api

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a picture.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
interface Image : Media, Iterable<Int> {

    /**
     * Gets the pixel color at specified coordinates in RGBA form.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The pixel color in RGBA form.
     */
    operator fun get(x: Int, y: Int): Int

    /**
     * Converts this image as a 2D array of pixel colors in RGBA format.
     * @return This image as a 2D Integer Array
     */
    fun to2DArray(): Array<IntArray> {
        val array = Array(width) { IntArray(height) }

        for (x in 0..width)
            for (y in 0..height)
                array[x][y] = get(x, y)

        return array
    }

    /**
     * Converts this image as a 1D array of pixel colors in RGBA format.
     * @return This image as a 1D Integer Array
     */
    fun toArray(): IntArray {
        val array = IntArray(width * height)

        for (x in 0 until width)
            for (y in 0 until height)
                array[x * height + y] = get(x, y)

        return array
    }

    /**
     * Gets the red portion for the pixel color at the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The red portion from the RGBA pixel color
     */
    fun red(x: Int, y: Int): Int {
        val pixel = get(x, y)
        return (pixel shr 24) and 0xFF
    }

    /**
     * Gets the blue portion for the pixel at the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The blue portion from the RGBA pixel color
     */
    fun blue(x: Int, y: Int): Int {
        val pixel = get(x, y)
        return (pixel shr 16) and 0xFF
    }

    /**
     * Gets the green portion for the pixel at the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The green portion from the RGBA pixel color
     */
    fun green(x: Int, y: Int): Int {
        val pixel = get(x, y)
        return (pixel shr 8) and 0xFF
    }

    /**
     * Gets the alpha portion for the pixel at the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The alpha portion from the RGBA pixel color
     */
    fun alpha(x: Int, y: Int): Int {
        val pixel = get(x, y)
        return pixel and 0xFF
    }

    override fun iterator(): Iterator<Int> = toArray().iterator()

    override fun asciffySync(map: String, downScale: Int?): Image
}