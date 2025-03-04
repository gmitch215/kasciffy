package dev.gmitch215.kasciffy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main

class Kasciffy : CliktCommand(name = "kasciffy") {
    override val printHelpOnEmptyArgs: Boolean = true
    override fun run() = Unit

    companion object {
        internal val HELP = """
            |Kasciffy is a Kotlin Multiplatform tool for converting ASCII text to ASCII art.
            |
            |Usage:
            |  kasciffy [options] [command]
        """.trimIndent()
    }

    override fun help(context: Context): String = HELP
}

fun main(args: Array<String>) = Kasciffy()
    .main(args)