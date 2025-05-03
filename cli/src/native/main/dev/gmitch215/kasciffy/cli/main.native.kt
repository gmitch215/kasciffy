package dev.gmitch215.kasciffy.cli

import dev.gmitch215.kasciffy.api.Image
import dev.gmitch215.kasciffy.api.ImageNative

@Suppress("RemoveRedundantQualifierName")
actual fun Image(file: String): Image = dev.gmitch215.kasciffy.api.Image(file)

actual suspend fun Image.asciffyAsync(map: String, downScale: Int?): Image {
    require(this is ImageNative)
    return asciffy(map, downScale)
}

actual fun Image.writeTo(output: String) {
    require(this is ImageNative)
    writeToFile(output)
}