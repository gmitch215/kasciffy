package xyz.gmitch215.kasciffy.api

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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