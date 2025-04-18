@file:OptIn(ExperimentalJsExport::class)

import dev.gmitch215.kasciffy.api.Image
import dev.gmitch215.kasciffy.api.ImageJS
import org.w3c.files.Blob
import kotlin.js.Promise

private fun resolveImage(image: ImageJS, map: String, downScale: Int?, resolve: (String) -> Unit, reject: (Throwable) -> Unit) {
    val asciffied = image.asciffy(map, downScale)
    asciffied.then { res ->
        val dataUrl = res.toDataURL()
        resolve(dataUrl)
    }.catch { error ->
        reject(error)
    }
}

/**
 * Asciffies the given image URL using the specified ASCII map.
 * This function is asynchronous and returns a Promise that resolves to the data URL of the asciffied image.
 *
 * @param url The URL of the image to be asciffied.
 * @param map The ASCII map to use for asciffying.
 * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
 * If not specified, this is automatically calculated.
 * @return A Promise that resolves to the data URL of the asciffied image.
 */
@JsExport
@JsName("asciffyUrl")
fun asciffy(url: String, extension: String, map: String, downScale: Int? = null): Promise<String> = Promise { resolve, reject ->
    Image(url, extension).then { resolveImage(it, map, downScale, resolve, reject) }
}

/**
 * Asciffies the given Blob using the specified ASCII map.
 * This function is asynchronous and returns a Promise that resolves to the data URL of the asciffied image.
 *
 * @param blob The Blob to be asciffied.
 * @param map The ASCII map to use for asciffying.
 * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
 * If not specified, this is automatically calculated.
 * @return A Promise that resolves to the data URL of the asciffied image.
 */
@JsExport
@JsName("asciffyBlob")
fun asciffy(blob: Blob, extension: String, map: String, downScale: Int? = null): Promise<String> = Promise { resolve, reject ->
    Image(blob, extension).then { resolveImage(it, map, downScale, resolve, reject) }
}

/**
 * Asciffies the given ByteArray using the specified ASCII map.
 * This function is asynchronous and returns a Promise that resolves to the data URL of the asciffied image.
 *
 * @param binary The ByteArray to be asciffied.
 * @param map The ASCII map to use for asciffying.
 * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
 * If not specified, this is automatically calculated.
 * @return A Promise that resolves to the data URL of the asciffied image.
 */
@JsExport
@JsName("asciffyBinary")
fun asciffy(binary: ByteArray, extension: String, map: String, downScale: Int? = null): Promise<String> = Promise { resolve, reject ->
    Image(binary, extension).then { resolveImage(it, map, downScale, resolve, reject) }
}