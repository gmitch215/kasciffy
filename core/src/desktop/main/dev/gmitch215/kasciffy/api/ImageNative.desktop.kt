package dev.gmitch215.kasciffy.api

import kotlinx.cinterop.*
import platform.posix.fopen
import spng.*
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
actual fun Image(file: String): ImageNative = memScoped {
    val name = file.substringAfterLast('/').substringBeforeLast('.')
    val extension = file.substringAfterLast('.')
    if (extension != "png") throw IllegalArgumentException("Only PNG files are supported.")

    val png = fopen(file, "rb")!!
    val ctx = spng_ctx_new(0)!!

    spng_set_crc_action(ctx, SPNG_CRC_USE.toInt(), SPNG_CRC_USE.toInt())
    spng_set_chunk_limits(ctx, CHUNK_LIMITS, CHUNK_LIMITS)
    spng_set_png_file(ctx, png)

    val currentTimeMillis = TimeSource.Monotonic.markNow().elapsedNow().toLong(DurationUnit.MILLISECONDS)
    return Image(name, extension, currentTimeMillis, ctx) ?: throw IllegalStateException("Failed to create Image.")
}

@Suppress("UnusedReceiverParameter")
@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
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


    return ImageNative(name, extension, creationDate, ctx, ihdr, rgbValues)
}