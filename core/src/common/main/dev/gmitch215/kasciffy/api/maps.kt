@file:Suppress("SpellCheckingInspection")
@file:OptIn(ExperimentalJsFileName::class, ExperimentalJsExport::class)

@file:JvmName("AsciiMaps")
@file:JsExport
@file:JsFileName("asciiMaps.js")

package dev.gmitch215.kasciffy.api

import kotlin.js.ExperimentalJsExport
import kotlin.js.ExperimentalJsFileName
import kotlin.js.JsExport
import kotlin.js.JsFileName
import kotlin.jvm.JvmName

/**
 * An ASCII options map with thirty levels of detail.
 */
const val THIRTY = "@$#%[]{}()&?/\\|<>o+=x~;:-,`._ "

/**
 * An ASCII options map with twenty levels of detail.
 */
const val TWENTY = "@&$%#?!o+=x^~;:-,.` "

/**
 * An ASCII options map with fifteen levels of detail.
 */
const val FIFTEEN = "@&$%#0?ol+=-:. "

/**
 * An ASCII options map with twelve levels of detail.
 */
const val TWELVE = "@&$%#=+~-_. "

/**
 * An ASCII options map with ten levels of detail.
 */
const val TEN = "@&$%#=+_. "

/**
 * An ASCII options map with ten levels of detail in numeric form.
 */
const val TEN_NUMERIC = "098654271 "

/**
 * An ASCII options map with five levels of detail.
 */
const val FIVE = "@#$. "

/**
 * An ASCII options map with five levels of detail in block form.
 */
const val FIVE_BLOCK = "█▓▒░ "

/**
 * An ASCII options map with four levels of detail in block form.
 */
const val FOUR_BLOCK = "█▓▒░"

/**
 * An ASCII options map with two levels of detail.
 */
const val TWO = "@ "

/**
 * An ASCII options map with two levels of detail in block form.
 */
const val TWO_BLOCK = "█ "