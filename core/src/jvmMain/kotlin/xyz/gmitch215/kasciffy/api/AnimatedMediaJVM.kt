package xyz.gmitch215.kasciffy.api

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.Node
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.nio.Buffer
import java.nio.file.Path
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import kotlin.math.roundToInt

/**
 * The JVM Implementation of the [AnimatedImage] interface.
 */
class AnimatedImageJVM internal constructor(
    override val name: String,
    override val extension: String,
    override val creationDate: Long,
    override val fps: Int,
    /**
     * The list of [BufferedImage] that serves as the backing to this [AnimatedImage].
     */
    val bufferedImages: List<BufferedImage>
) : AnimatedImage {

    override val width: Int = bufferedImages.first().width
    override val height: Int = bufferedImages.first().height
    override val frames: List<Image>

    init {
        bufferedImages.forEach {
            require(it.width == width && it.height == height) { "All frames must have the same dimensions." }
        }

        frames = bufferedImages.map { Image(it) }
    }

    override operator fun get(index: Int): Image = Image(bufferedImages[index])
}

/**
 * Creates an AnimatedImage from the given [File].
 * @param file The file to create the image from.
 * @return The created AnimatedImage.
 */
fun AnimatedImage(file: File): AnimatedImageJVM {
    val (name, extension) = file.nameWithoutExtension to file.extension

    val frames = mutableListOf<BufferedImage>()
    var totalDuration = 0.0

    val stream = ImageIO.createImageInputStream(file)
    val readers = ImageIO.getImageReadersByFormatName("gif")

    if (readers.hasNext()) {
        val reader = readers.next()

        try {
            reader.input = stream
            val numFrames = reader.getNumImages(true)

            var canvas: BufferedImage? = null

            for (i in 0 until numFrames) {
                val rawFrame = reader.read(i) as BufferedImage
                val duration = frameDuration(reader.getImageMetadata(i))

                if (canvas == null)
                    canvas = BufferedImage(rawFrame.width, rawFrame.height, BufferedImage.TYPE_INT_ARGB)

                frames.add(composedFrame(rawFrame, canvas))
                totalDuration += duration
            }
        } finally {
            reader.dispose()
            stream.close()
        }
    }

    val fps = if (totalDuration > 0) (frames.size / (totalDuration / 1000.0)).toInt() else 0

    return AnimatedImageJVM(name, extension, file.lastModified(), fps, frames)
}

/**
 * Creates an AnimatedImage from the given [Path].
 * @param path The path to create the image from.
 * @return The created AnimatedImage.
 */
fun AnimatedImage(path: Path): AnimatedImageJVM = AnimatedImage(path.toFile())

/**
 * Creates an AnimatedImage from the given [InputStream].
 * @param input The input stream to create the image from.
 * @return The created AnimatedImage.
 */
fun AnimatedImage(input: InputStream): AnimatedImageJVM {
    val frames = mutableListOf<BufferedImage>()
    var totalDuration = 0.0

    ImageIO.createImageInputStream(input).use { stream ->
        val readers = ImageIO.getImageReadersByFormatName("gif")
        val reader = readers.next() ?: throw IllegalStateException("No GIF reader found")
        reader.input = stream

        var canvas: BufferedImage? = null

        for (i in 0 until reader.getNumImages(true)) {
            val rawFrame = reader.read(i)
            val duration = frameDuration(reader.getImageMetadata(i))

            if (canvas == null)
                canvas = BufferedImage(rawFrame.width, rawFrame.height, BufferedImage.TYPE_INT_ARGB)

            frames.add(composedFrame(rawFrame, canvas))
            totalDuration += duration
        }

        reader.dispose()
    }

    val fps = if (totalDuration > 0) (frames.size / (totalDuration / 1000.0)).toInt() else 0

    return AnimatedImageJVM("Unknown", "gif", System.currentTimeMillis(), fps, frames)
}

/**
 * Creates an AnimatedImage from the given path.
 * @param path The path to create the image from.
 * @return The created AnimatedImage.
 */
fun AnimatedImage(path: String): AnimatedImageJVM = AnimatedImage(File(path))

private fun composedFrame(rawFrame: BufferedImage, canvas: BufferedImage): BufferedImage {
    val g = canvas.createGraphics()
    g.drawImage(rawFrame, 0, 0, null)
    g.dispose()

    val composedFrame = BufferedImage(canvas.width, canvas.height, BufferedImage.TYPE_INT_ARGB)
    val g2 = composedFrame.createGraphics()
    g2.drawImage(canvas, 0, 0, null)
    g2.dispose()

    return composedFrame
}

private fun frameDuration(metadata: IIOMetadata): Double {
    val rootNode = metadata.getAsTree("javax_imageio_gif_image_1.0") as Node
    val children = rootNode.childNodes

    for (i in 0 until children.length) {
        val child = children.item(i)
        if (child.nodeName == "GraphicControlExtension") {
            val userObject = child.attributes.getNamedItem("delayTime")?.nodeValue
            if (userObject != null) {
                val delayTime = userObject.toInt()
                return delayTime * 10.0
            }
        }
    }

    return 0.0
}

internal suspend fun toGIF(frames: List<String>, outputPath: String, fps: Int, loopContinuously: Boolean = true) {
    val bufferedImages = MutableList(frames.size) { BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB) }

    coroutineScope {
        frames.forEachIndexed { i, it ->
            launch {
                val image = toPNG(it)
                bufferedImages[i] = image

                println("Frame $i Done")
            }
        }
    }

    toGIF(bufferedImages, outputPath, fps, loopContinuously)
}

internal fun toGIF(frames: List<BufferedImage>, outputPath: String, fps: Int, loopContinuously: Boolean = true) {
    require(frames.isNotEmpty()) { "The frame list cannot be empty." }
    require(fps > 0) { "FPS must be greater than 0." }

    val delayTime = (100.0 / fps).roundToInt()

    val writer = ImageIO.getImageWritersByFormatName("gif").next()
    val output = ImageIO.createImageOutputStream(File(outputPath))
    writer.output = output

    try {
        val params = writer.defaultWriteParam
        val metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), params)

        configureGifMetadata(metadata, delayTime, loopContinuously)

        writer.prepareWriteSequence(null)
        frames.forEach { frame ->
            writer.writeToSequence(IIOImage(frame, null, metadata), params)
        }
        writer.endWriteSequence()
    } finally {
        writer.dispose()
        output.close()
    }
}

private fun configureGifMetadata(metadata: IIOMetadata, delayTime: Int, loopContinuously: Boolean) {
    val root = metadata.getAsTree("javax_imageio_gif_image_1.0") as IIOMetadataNode

    val control = root.getElementsByTagName("GraphicControlExtension").item(0) as IIOMetadataNode
    control.setAttribute("delayTime", delayTime.toString())
    control.setAttribute("disposalMethod", "none")

    if (loopContinuously) {
        val extensions = IIOMetadataNode("ApplicationExtensions")
        val extension = IIOMetadataNode("ApplicationExtension")
        extension.setAttribute("applicationID", "NETSCAPE")
        extension.setAttribute("authenticationCode", "2.0")
        extension.userObject = byteArrayOf(0x1, 0x0, 0x0) // Loop forever
        extensions.appendChild(extension)
        root.appendChild(extensions)
    }

    metadata.setFromTree("javax_imageio_gif_image_1.0", root)
}