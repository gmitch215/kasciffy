package dev.gmitch215.kasciffy.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import dev.gmitch215.kasciffy.api.FIVE
import dev.gmitch215.kasciffy.api.Image
import dev.gmitch215.kasciffy.api.asciffyAsString
import korlibs.io.file.VfsOpenMode
import korlibs.io.file.std.localCurrentDirVfs
import korlibs.io.stream.writeBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking

class Kasciffy : SuspendingCliktCommand(name = "kasciffy") {
    override val printHelpOnEmptyArgs: Boolean = true

    val input by argument(help = "Input file to use")

    val map by option(
        "-m", "--map",
        help = "ASCII map to use for asciffying. Will use the FIVE map if not provided."
    ).default(FIVE)
    val downScale by option(
        "-d", "--downscale",
        help = "Downscale factor to use when asciffying. This is used to reduce the size of the media, if necessary. Will be automatically calculated if not provided."
    ).int()
    val output by option(
        "-o", "--output",
        help = "Output file path. If not specified, the output will be printed to the console."
    )

    val verbose by option(
        "-v", "--verbose",
        help = "Enable verbose output."
    ).flag(default = false)
    val toImage by option(
        "-i", "--image",
        help = "Output the ASCII art as an image."
    ).flag(default = false)

    companion object {
        internal const val HELP = "Kasciffy is a Kotlin Multiplatform tool for converting ASCII text to ASCII art."
    }

    override fun help(context: Context): String = HELP

    override suspend fun run() {
        if (verbose) echo("Using file input: $input")

        val image = Image(input)
        if (verbose) echo("Asciffying...")

        if (toImage) {
            val out = output ?: "${localCurrentDirVfs.path}/asciffied.png"

            if (verbose) echo("Converting to image...")

            val asciiImage = image.asciffyAsync(map, downScale)
            if (verbose) echo("Writing to output...")

            asciiImage.writeTo(out)

            if (verbose) echo("Wrote to file: $out")
        } else {
            val ascii = image.asciffyAsString(map, downScale)

            if (output != null) {
                if (verbose) echo("Writing to output...")

                val output = localCurrentDirVfs[output!!]
                output.writeString(ascii)

                if (verbose) echo("Wrote to file: ${output.absolutePath}")
            } else {
                echo(ascii)
            }

            echo("Done!")
        }
    }
}

expect fun Image(file: String): Image
expect suspend fun Image.asciffyAsync(map: String, downScale: Int?): Image
expect fun Image.writeTo(output: String)

fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    Kasciffy().main(args)
}