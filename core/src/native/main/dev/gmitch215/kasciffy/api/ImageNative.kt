@file:OptIn(ExperimentalForeignApi::class)

package dev.gmitch215.kasciffy.api

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import olivec.*
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import spng.SPNG_COLOR_TYPE_TRUECOLOR_ALPHA
import spng.SPNG_CTX_ENCODER
import spng.SPNG_ENCODE_FINALIZE
import spng.SPNG_FMT_RGBA8
import spng.spng_ctx
import spng.spng_ctx_free
import spng.spng_ctx_new
import spng.spng_encode_image
import spng.spng_get_png_buffer
import spng.spng_ihdr
import spng.spng_set_ihdr
import spng.spng_set_png_buffer

/**
 * The Native implementation of the [Image] interface.
 */
class ImageNative internal constructor(
    override val name: String,
    override val extension: String,
    override val creationDate: Long,
    /**
     * The pointer to the [spng_ctx] object.
     */
    val ctx: CPointer<spng_ctx>,
    /**
     * The [spng_ihdr] object.
     */
    val ihdr: spng_ihdr,
    /**
     * The RGB values of the image.
     */
    val rgbValues: Array<IntArray>
) : Image {

    override val width: Int
        get() = ihdr.width.toInt()

    override val height: Int
        get() = ihdr.height.toInt()

    /**
     * Frees the [spng_ctx] pointer.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun free() = spng_ctx_free(ctx)

    /**
     * Writes this image to the given file.
     * @param file The file to write the image to.
     */
    fun write(file: String): Unit = memScoped {
        val file = fopen(file, "wb")
        val len = alloc<ULongVar>()
        val buffer = spng_get_png_buffer(ctx, len.ptr, null)

        fwrite(buffer, len.value, 1.toULong(), file)
        fclose(file)
    }

    override fun get(x: Int, y: Int): Int = rgbValues[y][x]

    /**
     * Asynchronously Asciffies this piece of media.
     * @param map The ASCII map to use for asciffying.
     * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
     * If not specified, this is automatically calculated.
     * @return The asciffied version of this piece of media.
     */
    suspend fun asciffy(map: String, downScale: Int?): ImageNative {
        val canvas = toCanvas(asciffy0(this, map, downScale))
        val (ctx, ihdr) = toSpngContents(canvas) ?: throw IllegalStateException("Failed to convert canvas to SPNG contents")

        val rgbValues = canvas.useContents {
            val height0 = height.toInt()
            val width0 = width.toInt()
            val pixels0 = pixels ?: return@useContents null

            val arr = Array(height0) { IntArray(width0) }
            for (y in 0 until height0) {
                for (x in 0 until width0) {
                    val argb = pixels0[y * (width0 * 4) + x]
                    arr[y][x] = argb.toInt()
                }
            }

            arr
        } ?: throw IllegalStateException("Failed to convert canvas to RGB values")

        return ImageNative(name, extension, creationDate, ctx, ihdr, rgbValues)
    }

    override fun asciffySync(map: String, downScale: Int?): ImageNative = runBlocking { asciffy(map, downScale) }

}

/**
 * The chunk limits for the image.
 */
val CHUNK_LIMITS = (1024 * 1024 * 64).toULong()

/**
 * Creates a new [ImageNative] object from the specified file.
 * @param file The file to create the image from.
 * @return The created [ImageNative] object.
 */
expect fun Image(file: String): ImageNative

internal fun toCanvas(output: String, spaced: Boolean = true, fontSize: Int = 12): CValue<Olivec_Canvas> = memScoped {
    val lines = output.split("\n")
    val width = lines[0].toInt() * (if (spaced) 2 else 1) * OLIVEC_DEFAULT_FONT_HEIGHT
    val height = lines.size * OLIVEC_DEFAULT_FONT_HEIGHT

    val pixels = UIntArray(width * height)
    val oc = pixels.usePinned { pinned ->
        olivec_canvas(pinned.addressOf(0), width.toULong(), height.toULong(), width.toULong())
    }
    olivec_fill(oc, 0xFF000000.toUInt())

    for ((index, line) in lines.withIndex()) {
        val line0 = if (spaced) line.map { "$it " }.joinToString("") else line
        olivec_text(oc, line0, 0, index * OLIVEC_DEFAULT_FONT_HEIGHT.toInt(), olivec_default_font.readValue(), fontSize.toULong(), 0xFFFFFFFF.toUInt())
    }

    return@memScoped oc
}

internal fun toSpngContents(canvas: CValue<Olivec_Canvas>): Pair<CPointer<spng_ctx>, spng_ihdr>? = memScoped {
    return@memScoped canvas.useContents {
        val pixels0 = pixels ?: return@memScoped null
        val width = width.toInt()
        val height = height.toInt()

        val size = width * height * 4
        val buffer = allocArray<ByteVar>(size)
        val rowStride = width.toInt() * 4

        for (y in 0 until height) {
            for (x in 0 until width) {
                val argb = pixels0[y * rowStride + x]

                buffer[y * rowStride + x] = (argb shr 16).toByte() // R
                buffer[y * rowStride + x + 1] = (argb shr 8).toByte() // G
                buffer[y * rowStride + x + 2] = argb.toByte() // B
                buffer[y * rowStride + x + 3] = (argb shr 24).toByte() // A
            }
        }

        val ctx = spng_ctx_new(SPNG_CTX_ENCODER.toInt())!!
        spng_set_png_buffer(ctx, null, 0.toULong())

        val ihdr = cValue<spng_ihdr> {
            this.width = width.toUInt()
            this.height = height.toUInt()

            bit_depth = 8.toUByte()
            color_type = SPNG_COLOR_TYPE_TRUECOLOR_ALPHA.toUByte()
            compression_method = 0.toUByte()
            filter_method = 0.toUByte()
            interlace_method = 0.toUByte()
        }
        spng_set_ihdr(ctx, ihdr)
        spng_encode_image(ctx, buffer, size.toULong(), SPNG_FMT_RGBA8.toInt(), SPNG_ENCODE_FINALIZE.toInt())

        ihdr.useContents { ctx to this }
    }
}