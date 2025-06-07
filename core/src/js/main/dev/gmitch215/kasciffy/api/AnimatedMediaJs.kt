@file:OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)

package dev.gmitch215.kasciffy.api

import dev.gmitch215.kasciffy.api.Media.Companion.UNNAMED_MEDIA_NAME
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.Date
import kotlin.js.Promise
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.files.Blob
import org.w3c.files.FileReader

/**
 * The JS Implementation of the [AnimatedImage] interface.
 */
@JsExport
class AnimatedImageJS internal constructor(
    override val name: String,
    override val extension: String,
    @Deprecated(message = "JS does not support 64-bit creation dates as an integer", replaceWith = ReplaceWith("creationDateDouble"))
    override val creationDate: Long = -1,
    val creationDateDouble: Double,
    override val fps: Int,
    private val imageFrames: List<ImageJS>
) : AnimatedImage {

    override val frames: List<Image>
        get() = imageFrames

    override val width: Int = imageFrames.firstOrNull()?.width ?: 0
    override val height: Int = imageFrames.firstOrNull()?.height ?: 0

    override fun get(index: Int): Image = imageFrames[index]

    override fun asciffySync(map: String, downScale: Int?): AnimatedImage {
        throw UnsupportedOperationException("asciffySync is not supported on JS. Use asciffy instead.")
    }

    /**
     * Converts this animated image to GIF format a Data URL.
     * @param loop The number of times the GIF should loop. 0 means infinite looping.
     * @param quality The quality of the GIF, from 1 (lowest) to 20 (highest).
     * @param delayMs The delay between frames in milliseconds. Default is calculated based on the fps.
     * @return A promise that resolves to the data URL representation of this animated media in GIF format.
     */
    fun toDataURL(loop: Int = 0, quality: Int = 10, delayMs: Int = (1000 / fps)): Promise<String> = GlobalScope.promise {
        val blob = toBlob(loop, quality, delayMs).await()

        val reader = FileReader()
        val result = CompletableDeferred<String>()

        reader.onloadend = { result.complete(reader.result as String) }
        reader.readAsDataURL(blob)

        result.await()
    }

    /**
     * Converts this animated image to GIF format as a [Blob].
     * @param loop The number of times the GIF should loop. 0 means infinite looping.
     * @param quality The quality of the GIF, from 1 (lowest) to 20 (highest).
     * @param delayMs The delay between frames in milliseconds. Default is calculated based on the fps.
     * @return A promise that resolves to the [Blob] representation of this animated media in GIF format.
     */
    fun toBlob(loop: Int = 0, quality: Int = 10, delayMs: Int = (1000 / fps)): Promise<Blob> = GlobalScope.promise {
        val gif = GIF(jsObject {
            workers = 2
            this.quality = quality
            workerScript = "https://cdn.jsdelivr.net/npm/gif.js.optimized/dist/gif.worker.js"
            repeat = loop
        })

        for (frame in imageFrames)
            gif.addFrame(frame.canvas, jsObject { delay = delayMs })

        val result = CompletableDeferred<Blob>()
        gif.on("finished") { blob -> result.complete(blob as Blob) }

        gif.render()
        result.await()
    }

    /**
     * Asynchronously asciffies this animated media.
     * @param map The ASCII map to use for asciffying.
     * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
     * If not specified, this is automatically calculated. On JavaScript, it is recommended to use a value of 2 or higher compared to
     * on the JVM.
     * @return A promise that resolves to the asciffied version of this animated media.
     */
    fun asciffy(map: String, downScale: Int? = 2): Promise<AnimatedImageJS> = GlobalScope.promise {
        val asciffiedFrames = imageFrames.map { frame ->
            val asciffied = asciffy0(frame, map, downScale)
            val canvas = toCanvas(asciffied, spaced = true, fontName = "Monospace", fontSize = 12)
            ImageJS(frame.name, frame.extension, creationDateDouble = frame.creationDateDouble, canvas = canvas)
        }

        AnimatedImageJS(name, extension, creationDateDouble = creationDateDouble, fps = fps, imageFrames = asciffiedFrames)
    }
}

/**
 * Creates an [AnimatedImageJS] from a list of frames.
 * @param name The name of the animated media.
 * @param extension The file extension of the animated media.
 * @param creationDateDouble The creation date as a double (milliseconds since epoch).
 * @param fps The frames per second of the animated media.
 * @param frames The list of image frames that make up the animated media.
 * @return An instance of [AnimatedImageJS].
 */
@JsExport
@JsName("AnimatedMediaFromFrames")
fun AnimatedImage(
    name: String,
    extension: String,
    creationDateDouble: Double,
    fps: Int,
    frames: List<ImageJS>
): AnimatedImageJS = AnimatedImageJS(name, extension, creationDateDouble = creationDateDouble, fps = fps, imageFrames = frames)

/**
 * Creates an [AnimatedImageJS] from a list of image URLs.
 * @param urls The list of image URLs that make up the animated media.
 * @param fps The frames per second of the animated media.
 * @return A promise that resolves to an instance of [AnimatedImageJS].
 */
@JsExport
@JsName("AnimatedMediaFromImageUrls")
fun AnimatedImage(
    urls: List<String>,
    fps: Int
): Promise<AnimatedImageJS> = GlobalScope.promise {
    require(urls.isNotEmpty()) { "Frame URL list cannot be empty." }

    val frames = urls.map { url -> Image(url).await() }
    val name = UNNAMED_MEDIA_NAME
    val extension = "png"
    val creationDateDouble = Date.now()
    AnimatedImageJS(name, extension, creationDateDouble = creationDateDouble, fps = fps, imageFrames = frames)
}

/**
 * Creates an [AnimatedImageJS] from a GIF URL.
 * @param gif The URL of the GIF to be parsed.
 * @param fps The frames per second of the animated media. Default is 10.
 * @return A promise that resolves to an instance of [AnimatedImageJS].
 */
fun AnimatedImage(
    gif: String,
    fps: Int = 10
): Promise<AnimatedImageJS> = GlobalScope.promise {
    val response = window.fetch(gif).await()
    val arrayBuffer = response.arrayBuffer().await()
    val buf = Uint8Array(arrayBuffer)

    val gif = GIFuctJs.parseGIF(buf)
    val frames = GIFuctJs.decompressFrames(gif, true)

    val imageFrames = mutableListOf<ImageJS>()

    for (i in 0 until frames.size) {
        val frame = frames[i]
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = frame.dims.width.toInt()
        canvas.height = frame.dims.height.toInt()

        val ctx = canvas.getContext("2d")!! as CanvasRenderingContext2D
        val imageData = ctx.createImageData(canvas.width.toDouble(), canvas.height.toDouble())
        val patch = frame.patch.unsafeCast<Uint8Array>()

        for (j in 0 until patch.length)
            imageData.data[j] = patch[j]

        ctx.putImageData(imageData, 0.0, 0.0)
        imageFrames.add(Image(canvas, "image/png"))
    }

    AnimatedImage(UNNAMED_MEDIA_NAME, "image/gif", Date.now(), fps, imageFrames)
}

@JsModule("gif.js.optimized")
@JsNonModule
internal external class GIF(options: GIFOptions) {
    fun addFrame(canvas: HTMLCanvasElement, options: GIFFrameOptions = definedExternally)
    fun on(event: String, callback: (Any?) -> Unit)
    fun render()
}

internal interface GIFOptions {
    var workers: Int?
    var quality: Int?
    var workerScript: String?
    var repeat: Int?
}

internal interface GIFFrameOptions {
    var delay: Int?
}

fun jsObject(builder: dynamic.() -> Unit): dynamic {
    val obj = js("{}")
    builder(obj)
    return obj
}

@JsModule("gifuct-js")
@JsNonModule
internal external class GIFuctJs {
    companion object {
        fun parseGIF(data: Uint8Array): dynamic
        fun decompressFrames(gif: dynamic, ignoreLocalColorTable: Boolean): Array<ParsedFrame>
    }
}

internal external interface ParsedFrame {
    val dims: FrameDimensions
    val delay: Double
    val patch: Uint8ClampedArray
    val pixels: DoubleArray
    val transparentIndex: Int?
}

internal external interface FrameDimensions {
    val width: Double
    val height: Double
    val top: Double
    val left: Double
}