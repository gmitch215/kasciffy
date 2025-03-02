@file:OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)

package dev.gmitch215.kasciffy.api

import kotlinx.browser.document
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.js.Promise

@JsModule("canvas")
@JsNonModule
external class Canvas(width: Int, height: Int) {
    val width: Int
    val height: Int
    fun getContext(type: String): dynamic
    fun toDataURL(type: String = definedExternally): String
}

@JsModule("canvas")
@JsNonModule
external fun loadImage(url: String): Promise<dynamic>

/**
 * The JS Implementation of the [Image] interface.
 */
@JsExport
class ImageJS internal constructor(
    override val name: String,
    override val extension: String,
    override val creationDate: Long,
    private val canvas: dynamic
) : Image {
    override val width: Int
        get() = canvas.width
    override val height: Int
        get() = canvas.height

    override fun get(x: Int, y: Int): Int {
        val ctx = canvas.getContext("2d")
        val imageData = ctx.getImageData(x, y, 1, 1).data as ByteArray

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
    fun asciffy(map: String, downScale: Int? = null): Promise<Image> = GlobalScope.promise {
        val asciffiedCanvas = asciffy0(this@ImageJS, map, downScale)
        return@promise ImageJS(name, extension, creationDate, asciffiedCanvas)
    }

    @Deprecated("Use asciffy instead.")
    override fun asciffySync(map: String, downScale: Int?): Image
        = throw UnsupportedOperationException("asciffySync is not supported on JS. Use asciffy instead.")
}

/**
 * Creates an Image from the given URL.
 * @param url The URL to create the image from.
 * @return The created Image as a JavaScript promise.
 */
@JsExport
@JsName("ImageFromUrl")
fun Image(url: String): Promise<ImageJS> {
    val isNode = js("typeof window === 'undefined'")
    return if (isNode) {
        loadImage(url).then<dynamic> { img ->
            val canvas = Canvas(img.width, img.height)
            val ctx = canvas.getContext("2d")
            ctx.drawImage(img, 0, 0)
            ImageJS("Unknown", "png", js("Date.now()"), canvas)
        }
    } else {
        Promise { resolve, reject ->
            val img = document.createElement("img") as HTMLImageElement
            img.src = url
            img.onload = {
                val canvas = document.createElement("canvas") as HTMLCanvasElement
                val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
                canvas.width = img.width
                canvas.height = img.height
                ctx.drawImage(img, 0.0, 0.0)
                resolve(ImageJS("Unknown", "png", js("Date.now()"), canvas))
            }
            img.onerror = { _, _, _, _, _ -> reject(Error("Failed to load image: $url")) }
        }
    }
}

/**
 * Creates an Image from the given [Blob].
 * @param blob The blob to create the image from.
 * @return The created Image as a JavaScript promise.
 */
@JsExport
@JsName("ImageFromBlob")
fun Image(blob: Blob): Promise<ImageJS> {
    val reader = FileReader()
    val promise = Promise<String> { resolve, _ ->
        reader.onload = { resolve(reader.result as String) }
    }
    reader.readAsDataURL(blob)

    return Promise { resolve, reject ->
        promise.then { url -> Image(url).then { resolve(it) } }
    }
}

internal fun toCanvas(output: String, spaced: Boolean = true, fontName: String = "Monospace", fontSize: Int = 12): dynamic {
    val isNode = js("typeof window === 'undefined'")
    val canvas: dynamic = if (isNode) Canvas(1, 1) else document.createElement("canvas") as HTMLCanvasElement
    val ctx = canvas.getContext("2d")
    ctx.font = "$fontSize}px $fontName"

    val lines = output.split("\n")
    val lineHeight = fontSize * 1.2
    val imageWidth = lines.maxOf { ctx.measureText(it).width.toInt() as Int } * (if (spaced) 2 else 1)
    val imageHeight = (lineHeight * lines.size).toInt()

    canvas.width = imageWidth
    canvas.height = imageHeight

    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, imageWidth.toDouble(), imageHeight.toDouble())
    ctx.fillStyle = "white"
    ctx.font = "$fontSize}px $fontName"

    for ((index, line) in lines.withIndex()) {
        val line0 = if (spaced) line.map { "$it " }.joinToString("") else line
        ctx.fillText(line0, 0.0, (index + 1) * lineHeight)
    }

    return canvas
}
