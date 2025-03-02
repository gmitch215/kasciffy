package dev.gmitch215.kasciffy.api

import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.jvm.Throws

/**
 * The JVM Implementation of the [Image] interface.
 */
class ImageJVM internal constructor(
    override val name: String,
    override val extension: String,
    override val creationDate: Long,
    /**
     * The [BufferedImage] that serves as the backing to this [Image].
     */
    val bufferedImage: BufferedImage
) : Image {
    override val width: Int
        get() = bufferedImage.width

    override val height: Int
        get() = bufferedImage.height

    override operator fun get(x: Int, y: Int): Int = bufferedImage.getRGB(x, y)

    /**
     * Writes this image to the given file.
     * @param file The file to write the image to.
     * @throws IOException If the image could not be written to the file.
     * @see ImageIO.write
     */
    @Throws(IOException::class)
    fun write(file: File) = ImageIO.write(bufferedImage, extension, file)

    /**
     * Writes this image to the given output stream.
     * @param output The output stream to write the image to.
     * @throws IOException If the image could not be written to the output stream.
     * @see ImageIO.write
     */
    @Throws(IOException::class)
    fun write(output: OutputStream) = ImageIO.write(bufferedImage, extension, output)

    /**
     * Asynchronously Asciffies this piece of media.
     * @param map The ASCII map to use for asciffying.
     * @param downScale The downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary.
     * If not specified, this is automatically calculated.
     * @return The asciffied version of this piece of media.
     */
    suspend fun asciffy(map: String, downScale: Int?): Image {
        val asciffied = toPNG(asciffy0(this, map, downScale))
        return ImageJVM(name, extension, creationDate, asciffied)
    }

    override fun asciffySync(map: String, downScale: Int?): Image = runBlocking { asciffy(map, downScale) }
}

/**
 * Creates an Image from the given [File].
 * @param file The file to create the image from.
 * @return The created Image.
 */
fun Image(file: File): ImageJVM {
    val (name, extension) = file.nameWithoutExtension to file.extension
    val creationDate = file.lastModified()
    val bufferedImage = ImageIO.read(file)

    if (bufferedImage == null)
        error("Failed to read image from file: $file")

    return ImageJVM(name, extension, creationDate, bufferedImage)
}

/**
 * Creates an Image from the given [Path].
 * @param path The path to create the image from.
 * @return The created Image.
 */
fun Image(path: Path): ImageJVM = Image(path.toFile())

/**
 * Creates an Image from the given path.
 * @param path The path to create the image from.
 * @return The created Image.
 */
fun Image(path: String): ImageJVM = Image(File(path))

/**
 * Creates an Image from the given [BufferedImage].
 * @param name The name of the image.
 * @param bufferedImage The BufferedImage to create the image from.
 * @return The created Image.
 */
fun Image(name: String, bufferedImage: BufferedImage): ImageJVM {
    return ImageJVM(name, extension(bufferedImage.type), System.currentTimeMillis(), bufferedImage)
}

/**
 * Creates an Image from the given [BufferedImage].
 * @param bufferedImage The BufferedImage to create the image from.
 * @return The created Image.
 */
fun Image(bufferedImage: BufferedImage): ImageJVM = Image("Unknown", bufferedImage)

/**
 * Creates an Image from the given [InputStream].
 * @param input The input stream to create the image from.
 * @return The created Image.
 */
fun Image(input: InputStream): ImageJVM {
    val bufferedImage = ImageIO.read(input)

    if (bufferedImage == null)
        error("Failed to read image from input stream")

    return Image("Unknown", bufferedImage)
}

private fun extension(type: Int): String = when (type) {
    BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_4BYTE_ABGR -> "png"
    BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_3BYTE_BGR -> "jpg"
    BufferedImage.TYPE_BYTE_GRAY -> "bmp"
    else -> "png"
}

internal fun toPNG(output: String, spaced: Boolean = true, fontName: String = "Monospaced", fontSize: Int = 12): BufferedImage {
    val lines = output.split("\n")

    val font = Font(fontName, Font.PLAIN, fontSize)
    val tempImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    val tempGraphics = tempImage.createGraphics()
    tempGraphics.font = font
    val fontMetrics: FontMetrics = tempGraphics.fontMetrics

    val lineHeight = fontMetrics.height
    val imageWidth = (lines.maxOf { fontMetrics.stringWidth(it) } * (if (spaced) 2 else 1)).coerceAtLeast(1)
    val imageHeight = (lineHeight * lines.size).coerceAtLeast(1)
    tempGraphics.dispose()
    tempImage.flush()

    val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()

    graphics.color = Color.BLACK
    graphics.fillRect(0, 0, imageWidth, imageHeight)

    graphics.color = Color.WHITE
    graphics.font = font

    for ((index, line) in lines.withIndex()) {
        val line0 = if (spaced) line.map { "$it " }.joinToString("") else line
        graphics.drawString(line0, 0, (index + 1) * lineHeight - fontMetrics.descent)
    }

    graphics.dispose()
    image.flush()
    return image
}

internal fun toPNG(path: String, output: String, spaced: Boolean = true, fontName: String = "Monospaced", fontSize: Int = 12) {
    val image = toPNG(output, spaced, fontName, fontSize)
    ImageIO.write(image, "png", File(path))
}