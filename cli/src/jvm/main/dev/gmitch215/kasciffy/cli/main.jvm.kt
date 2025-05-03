package dev.gmitch215.kasciffy.cli

import dev.gmitch215.kasciffy.api.Image
import dev.gmitch215.kasciffy.api.ImageJVM
import java.io.File

@Suppress("RemoveRedundantQualifierName")
actual fun Image(file: String): Image = dev.gmitch215.kasciffy.api.Image(file)

actual suspend fun Image.asciffyAsync(map: String, downScale: Int?): Image {
    require(this is ImageJVM)
    return asciffy(map, downScale)
}

actual fun Image.writeTo(output: String) {
    require(this is ImageJVM)

    val file = File(output)
    if (file.exists()) file.delete()

    file.createNewFile()
    write(file)
}