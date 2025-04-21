@file:OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)

package dev.gmitch215.kasciffy.api

import com.github.randy408.libspng.*
import com.github.tsoding.olive_c.*
import kotlinx.cinterop.*
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSDataReadingUncached
import platform.Foundation.dataWithContentsOfFile
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val imageToPointer = mutableMapOf<String, CPointer<spng_ctx>>()

actual fun Image(file: String): ImageNative = memScoped {
    val name = file.substringAfterLast('/').substringBeforeLast('.')
    val extension = file.substringAfterLast('.')
    if (extension != "png") throw IllegalArgumentException("Only PNG files are supported.")

    val path = NSBundle.mainBundle.pathForResource(file.substringBeforeLast('.'), file.substringAfterLast('.'))
    val data = NSData.dataWithContentsOfFile(path, NSDataReadingUncached, null)
    val png = data?.bytes

    val ctx = spng_ctx_new(0)!!

    spng_set_crc_action(ctx, SPNG_CRC_USE.toInt(), SPNG_CRC_USE.toInt())
    spng_set_chunk_limits(ctx, CHUNK_LIMITS, CHUNK_LIMITS)
    spng_set_png_buffer(ctx, png, data?.length ?: 0u)

    val currentTimeMillis = TimeSource.Monotonic.markNow().elapsedNow().toLong(DurationUnit.MILLISECONDS)

    imageToPointer["$name-$extension-${currentTimeMillis}"] = ctx
    return Image(name, extension, currentTimeMillis, ctx) ?: throw IllegalStateException("Failed to create Image.")
}

@Suppress("UnusedReceiverParameter")
internal fun MemScope.Image(name: String, extension: String, creationDate: Long, ctx: CPointer<spng_ctx>): ImageNative? {
    val ihdr = nativeHeap.alloc<spng_ihdr>()
    if (spng_get_ihdr(ctx, ihdr.ptr) != 0) return null

    val size = nativeHeap.alloc<ULongVar>()
    if (spng_decoded_image_size(ctx, SPNG_FMT_RGBA8.toInt(), size.ptr) != 0) return null

    val data = ByteArray(size.value.toInt())
    data.usePinned { pinned ->
        if (spng_decode_image(ctx, pinned.addressOf(0), size.value, SPNG_FMT_RGBA8.toInt(), 0) != 0) return null
    }

    val rgbValues = Array(ihdr.height.toInt()) { IntArray(ihdr.width.toInt()) }
    for (y in 0 until ihdr.height.toInt())
        for (x in 0 until ihdr.width.toInt()) {
            val index = (y * ihdr.width.toInt() + x) * 4
            val r = data[index].toInt()
            val g = data[index + 1].toInt()
            val b = data[index + 2].toInt()
            rgbValues[y][x] = (r shl 16) or (g shl 8) or b
        }

    return ImageNative(name, extension, creationDate, ihdr.width.toInt(), ihdr.height.toInt(), rgbValues)
}

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
        olivec_text(oc, line0, 0, index * OLIVEC_DEFAULT_FONT_HEIGHT, olivec_default_font.readValue(), fontSize.toULong(), 0xFFFFFFFF.toUInt())
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
        val rowStride = width * 4

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

// Implementation

actual fun ImageNative.free() {
    imageToPointer[name]?.let { ctx ->
        spng_ctx_free(ctx)
        imageToPointer.remove(name)
    }
}

actual fun ImageNative.write(file: String): Unit = memScoped {
    val ctx = imageToPointer["$name-$extension-$creationDate"] ?: throw IllegalStateException("Image not found in pointer map")

    val file = fopen(file, "wb")
    val len = alloc<ULongVar>()
    val buffer = spng_get_png_buffer(ctx, len.ptr, null)

    fwrite(buffer, len.value, 1.toULong(), file)
    fclose(file)
}

actual suspend fun ImageNative.asciffy0(map: String, downScale: Int?): ImageNative {
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

    imageToPointer["$name-$extension-$creationDate"] = ctx
    return ImageNative(name, extension, creationDate, ihdr.width.toInt(), ihdr.height.toInt(), rgbValues)
}