package dev.gmitch215.kasciffy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main

class Kasciffy : CliktCommand(name = "kasciffy") {
    override fun run() = Unit
}

fun main(args: Array<String>) = Kasciffy()
    .main(args)