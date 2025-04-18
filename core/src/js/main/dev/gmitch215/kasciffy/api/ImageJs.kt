@file:OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)

package dev.gmitch215.kasciffy.api

import kotlinx.browser.document
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.khronos.webgl.get
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import kotlin.js.Date
import kotlin.js.Promise

/**
 * The JS Implementation of the [Image] interface.
 */
@JsExport
class ImageJS internal constructor(
    override val name: String,
    override val extension: String,
    @Deprecated(message = "JS does not support 64-bit creation dates as an integer", replaceWith = ReplaceWith("creationDateDouble"))
    override val creationDate: Long = -1,
    val creationDateDouble: Double,
    private val canvas: HTMLCanvasElement
) : Image {
    override val width: Int
        get() = canvas.width
    override val height: Int
        get() = canvas.height

    override fun get(x: Int, y: Int): Int {
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val imageData = ctx.getImageData(x.toDouble(), y.toDouble(), 1.0, 1.0).data

        return (imageData[0].toInt() shl 16) or (imageData[1].toInt() shl 8) or imageData[2].toInt()
    }

    /**
     * Converts this image to a data URL.
     * @return The data URL of this image.
     */
    fun toDataURL(): String = canvas.toDataURL("image/$extension")

    /**
     * Asynchronously Asciffies this piece of media.
     * @param map The ASCII map to use for asciffying.
     * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
     * If not specified, this is automatically calculated.
     * @return The asciffied version of this piece of media as a JavaScript promise.
     */
    fun asciffy(map: String, downScale: Int? = null): Promise<ImageJS> = GlobalScope.promise {
        val asciffied = asciffy0(this@ImageJS, map, downScale)
        val canvas = toCanvas(asciffied, spaced = true, fontName = "Monospace", fontSize = 12)
        return@promise ImageJS(name, extension, creationDateDouble = creationDateDouble, canvas = canvas)
    }

    @Deprecated("Use asciffy instead.", replaceWith = ReplaceWith("asciffy(map, downScale)"))
    override fun asciffySync(map: String, downScale: Int?): Nothing
        = throw UnsupportedOperationException("asciffySync is not supported on JS. Use asciffy instead.")
}

val SUPPORTED_TYPES = listOf(
    "png"
)

/**
 * Creates an Image from the given URL.
 * @param url The URL to create the image from.
 * @return The created Image as a JavaScript promise.
 */
@JsExport
@JsName("TryImageFromUrl")
fun Image(url: String): Promise<ImageJS> {
    val extension = url.substringAfterLast(".")
    if (extension !in SUPPORTED_TYPES) {
        throw IllegalArgumentException("Unsupported image type: $extension")
    }

    return Image(url, extension)
}

/**
 * Creates an Image from the given URL.
 * @param url The URL to create the image from.
 * @param extension The extension of the image (e.g. "png").
 * @return The created Image as a JavaScript promise.
 */
@JsExport
@JsName("ImageFromUrl")
fun Image(url: String, extension: String): Promise<ImageJS> {
    return Promise { resolve, reject ->
        val img = document.createElement("img") as HTMLImageElement
        img.src = url
        img.onload = {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            canvas.width = img.width
            canvas.height = img.height
            ctx.drawImage(img, 0.0, 0.0)
            resolve(ImageJS("Unknown", extension, creationDateDouble = Date.now(), canvas = canvas))
        }
        img.onerror = { _, _, _, _, _ -> reject(Error("Failed to load image: $url")) }
    }
}

/**
 * Creates an Image from the given [Blob].
 * @param blob The blob to create the image from.
 * @param extension The extension of the image (e.g. "png").
 * @return The created Image as a JavaScript promise.
 */
@JsExport
@JsName("ImageFromBlob")
fun Image(blob: Blob, extension: String): Promise<ImageJS> {
    val reader = FileReader()
    val promise = Promise { resolve, _ ->
        reader.onload = { resolve(reader.result as String) }
    }
    reader.readAsDataURL(blob)

    return Promise { resolve, _ ->
        promise.then { url -> Image(url, extension).then { resolve(it) } }
    }
}

/**
 * Creates an Image from the given binary data.
 * @param binary The binary data to create the image from.
 * @param type The MIME type of the image (e.g. "image/png").
 * @return The created Image as a JavaScript promise.
 */
@JsExport
@JsName("ImageFromBinary")
fun Image(binary: ByteArray, type: String): Promise<ImageJS> {
    val blob = Blob(arrayOf(binary.asDynamic()), BlobPropertyBag(type))
    return Image(blob, type.substringAfter("/"))
}

internal fun toCanvas(output: String, spaced: Boolean = true, fontName: String = "Monospace", fontSize: Int = 12): HTMLCanvasElement {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    ctx.font = "$fontSize}px $fontName"

    val lines = output.split("\n")
    val lineHeight = fontSize * 1.2
    val imageWidth = lines.maxOf { ctx.measureText(it).width.toInt() } * (if (spaced) 2 else 1)
    val imageHeight = (lineHeight * lines.size).toInt()

    canvas.width = imageWidth
    canvas.height = imageHeight

    ctx.fillStyle = "black"
    ctx.fillRect(0.0, 0.0, imageWidth.toDouble(), imageHeight.toDouble())
    ctx.fillStyle = "white"
    ctx.font = "$fontSize}px $fontName"

    for ((index, line) in lines.withIndex()) {
        val line0 = if (spaced) line.map { "$it " }.joinToString("") else line
        ctx.fillText(line0, 0.0, (index + 1) * lineHeight)
    }

    return canvas
}
